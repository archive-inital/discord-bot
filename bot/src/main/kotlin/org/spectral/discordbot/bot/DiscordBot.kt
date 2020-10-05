package org.spectral.discordbot.bot

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.discordjson.json.ApplicationInfoData
import discord4j.discordjson.json.UserData
import discord4j.rest.response.ResponseFunction
import org.spectral.discordbot.bot.data.Config
import org.spectral.discordbot.bot.data.credential.Credential
import org.spectral.discordbot.bot.data.credential.CredentialManager
import org.spectral.discordbot.bot.listener.EventListener
import org.spectral.discordbot.bot.listener.MessageCreateListener
import org.spectral.discordbot.bot.util.BotUtils
import org.spectral.logger.logger
import reactor.core.publisher.Mono
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.exitProcess

/**
 * The main discord bot instance.
 */
class DiscordBot {

    /**
     * Starts the discord bot.
     */
    fun start() {
        logger.info("Initializing...")

        /*
         * Start the discord bot.
         */
        logger.info("Starting Discord Bot v${Config.VERSION}")

        /*
         * The discord client connection.
         */
        val client = DiscordClient.builder(CredentialManager[Credential.DISCORD_TOKEN])
                .onClientResponse(ResponseFunction.emptyIfNotFound())
                .build()

        /*
         * Acquire the discord server information running this bot process.
         */
        client.applicationInfo
                .map(ApplicationInfoData::owner)
                .map(UserData::id)
                .map(Snowflake::asLong)
                .doOnNext {
                    logger.info("Acquired owner ID: $it")
                    OWNER_ID.set(it)
                }
                .block()

        /*
         * Connect to discord API.
         */
        logger.info("Connecting to Discord API.")

        client.gateway()
                .setAwaitConnections(false)
                .setInitialStatus { BotUtils.getStatus() }
                .withGateway { gateway ->
                    DiscordBot.gateway = gateway

                    /*
                     * Initialize bot startup tasks.
                     */
                    taskManager = TaskManager()

                    /*
                     * Register event listeners
                     */
                    logger.info("Registering event listeners")
                    register(MessageCreateListener())

                    logger.info("Discord Bot is ready")

                    /*
                     * Await until the gateway connection disconnects.
                     */
                    gateway.onDisconnect()
                }
                .block()

        logger.info("Discord Bot has disconnected. Exiting process.")
        exitProcess(0)
    }

    /**
     * Register an event listener with the current gateway instance.
     *
     * @param listener EventListener<T>
     */
    private inline fun <reified T : Event> register(listener: EventListener<T>) {
        gateway.eventDispatcher
                .on(T::class.java)
                .flatMap { event ->
                    listener.execute(event)
                            .thenReturn(T::class.java.simpleName)
                            .onErrorResume { return@onErrorResume Mono.empty() }
                }.subscribe()
    }

    companion object {

        /**
         * The Discord API gateway instance.
         */
        private lateinit var gateway: GatewayDiscordClient

        /**
         * The task manager instance for this process.
         */
        private lateinit var taskManager: TaskManager

        /**
         * The Owner discord server ID running this bot process.
         */
        private val OWNER_ID = AtomicLong()

        /**
         * The jvm static main method.
         *
         * @param args Array<String>
         */
        @JvmStatic
        fun main(args: Array<String>) {
            /*
             * Initialize the file logger.
             */
            val logDirectory = File("logs/")

            if(!logDirectory.exists()) {
                logDirectory.mkdirs()
            }

            //logger.provider.enableFileLogging(logDirectory.toPath(), "discordbot")

            /*
             * Start the bot on a new instance of [DiscordBot]
             */
            DiscordBot().start()
        }
    }
}