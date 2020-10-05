package org.spectral.discordbot.bot.core.command

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import org.spectral.discordbot.bot.data.Config
import org.spectral.discordbot.bot.util.DiscordUtils
import org.spectral.logger.logger
import reactor.bool.BooleanUtils
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Responsible for processing a message.
 */
object MessageProcessor {

    private val DM_TEXT = "Hello!" +
            "\nCommands only work in the Spectral server but you can see help using `${Config.DEFAULT_PREFIX}help`." +
            "\nIf you have a question, a suggestion or if you just want to talk, don't hesitate to ask in the appropriate channel " +
            "in the Spectral Discord."

    /**
     * Processes a message creation event.
     *
     * @param event MessageCreateEvent
     * @return Mono<Void>
     */
    fun processEvent(event: MessageCreateEvent): Mono<Void> {
        /*
         * If the message is a webhook or a bot
         * We want to ignore it.
         */
        if(event.message.author.map { it.isBot }.orElse(true)) {
            return Mono.empty()
        }

        return Mono.justOrEmpty(event.guildId)
                /*
                 * This is a private channel, no guild ID found.
                 */
                .switchIfEmpty(processPrivateMessage(event).then(Mono.empty()))
                /*
                 * Otherwise, process the guild message.
                 */
                .flatMap { processGuildMessage(it, event) }
    }

    private fun processPrivateMessage(event: MessageCreateEvent): Mono<Void> {
        /*
         * If the message was the help command, we can display the help message.
         */
        if(event.message.content.startsWith("${Config.DEFAULT_PREFIX}help")) {
            return Mono.empty()
        }

        /*
         * Send the [DM_TEXT] to the user.
         */
        return event.message.channel
                .flatMap { DiscordUtils.sendMessage(DM_TEXT, it) }
                .then()
    }

    private fun processGuildMessage(guildId: Snowflake, event: MessageCreateEvent): Mono<Void> {
        return Mono.empty()
    }
}