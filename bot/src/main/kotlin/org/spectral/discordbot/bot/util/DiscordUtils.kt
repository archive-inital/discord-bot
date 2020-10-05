package org.spectral.discordbot.bot.util

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.PrivateChannel
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import discord4j.rest.http.client.ClientException
import discord4j.rest.util.AllowedMentions
import discord4j.rest.util.Permission
import io.netty.channel.unix.Errors.NativeIoException
import io.netty.handler.codec.http.HttpResponseStatus
import org.spectral.discordbot.bot.`object`.Emoji
import org.spectral.logger.logger
import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import reactor.netty.http.client.PrematureCloseException
import reactor.util.retry.Retry
import java.time.Duration
import java.util.function.Consumer


/**
 * Discord related utility methods
 */
object DiscordUtils {

    /**
     * Gets whether a given [userId] has permission in a channel.
     *
     * @param channel Channel
     * @param userId Snowflake
     * @param permission Permission
     * @return Mono<Boolean>
     */
    fun hasPermission(channel: Channel, userId: Snowflake, permission: Permission): Mono<Boolean> {
       if(channel is PrivateChannel) {
           return Mono.just(true)
       }

        return (channel as GuildChannel).getEffectivePermissions(userId)
                .map { it.contains(permission) }
    }

    /**
     * Sends a normal text message to a channel.
     *
     * @param content String
     * @param channel MessageChannel
     * @return Mono<Message>
     */
    open fun sendMessage(content: String, channel: MessageChannel): Mono<Message> {
        return sendMessage({ spec: MessageCreateSpec -> spec.setContent(content) }, channel, false)
    }

    /**
     * Sends a message embed to a channel.
     *
     * @param embed Consumer<EmbedCreateSpec>
     * @param channel MessageChannel
     * @return Mono<Message>
     */
    fun sendMessage(embed: Consumer<EmbedCreateSpec>, channel: MessageChannel): Mono<Message> {
        return sendMessage({ spec: MessageCreateSpec -> spec.setEmbed(embed) }, channel, true)
    }

    /**
     * Sends a message with an embed to a channel.
     *
     * @param content String
     * @param embed Consumer<EmbedCreateSpec>
     * @param channel MessageChannel
     * @return Mono<Message>
     */
    fun sendMessage(content: String, embed: Consumer<EmbedCreateSpec>, channel: MessageChannel): Mono<Message> {
        return sendMessage({ spec: MessageCreateSpec -> spec.setContent(content).setEmbed(embed) }, channel, true)
    }

    fun sendMessage(spec: Consumer<MessageCreateSpec>, channel: MessageChannel, hasEmbed: Boolean): Mono<Message> {
        return Mono.zip(
                hasPermission(channel, channel.client.selfId, Permission.SEND_MESSAGES),
                hasPermission(channel, channel.client.selfId, Permission.EMBED_LINKS))
                .flatMap(TupleUtils.function { canSendMessage: Boolean, canSendEmbed: Boolean ->
                    if (!canSendMessage) {
                        logger.info("{Channel ID: ${channel.id.asLong()}} Missing permission: ${Permission.SEND_MESSAGES}")
                        return@function Mono.empty<Message>()
                    }
                    if (!canSendEmbed && hasEmbed) {
                        logger.info("{Channel ID: ${channel.id.asLong()}} Missing permission: ${Permission.EMBED_LINKS}")
                        return@function sendMessage(java.lang.String.format(Emoji.ACCESS_DENIED.toString() + " I cannot send embed" +
                                " links.%nPlease, check my permissions "
                                + "and channel-specific ones to verify that **%s** is checked.", Permission.EMBED_LINKS), channel)
                    }
                    channel.createMessage(spec
                            .andThen { messageSpec: MessageCreateSpec ->
                                messageSpec.setAllowedMentions(AllowedMentions.builder()
                                        .parseType(AllowedMentions.Type.ROLE, AllowedMentions.Type.USER)
                                        .build())
                            })
                }) // 403 Forbidden means that the bot is not in the guild
                .onErrorResume(ClientException.isStatusCode(HttpResponseStatus.FORBIDDEN.code()), { err: Throwable? -> Mono.empty() })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter { err: Throwable? -> err is PrematureCloseException || err is NativeIoException })
    }

    /*/**
     * Sends a message spec to a message channel.
     *
     * @param spec Consumer<MessageCreateSpec>
     * @param channel MessageChannel
     * @param hasEmbed Boolean
     * @return Mono<Message>
     */
    fun sendMessage(spec: Consumer<MessageCreateSpec>, channel: MessageChannel, hasEmbed: Boolean): Mono<Message> {
        return Mono.zip(
                hasPermission(channel, channel.client.selfId, Permission.SEND_MESSAGES),
                hasPermission(channel, channel.client.selfId, Permission.EMBED_LINKS)
        ).flatMap(TupleUtils.function { canSendMessage, canSendEmbed ->
            /*
             * Log an error if the bot does not have permission to send messages
             * in this channel.
             */
            if(!canSendMessage) {
                logger.info("{Channel ID: ${channel.id.asLong()}} Missing permission: ${Permission.SEND_MESSAGES}")
                return@function Mono.empty()
            }

            /*
             * Log error if the bot does not have permission to send embed messages in this
             * channel and the message spec contains an embed.
             */
            if(!canSendEmbed && hasEmbed) {
                logger.info("{Channel ID: ${channel.id.asLong()}} Missing permission: ${Permission.EMBED_LINKS}")

                /*
                 * Send message in the channel alerting to the permission issue
                 * of sending embeds.
                 */
                return@function sendMessage("${Emoji.ACCESS_DENIED} I cannot send embed" +
                        " links.\nPlease, check my permission and channel-specific roles" +
                        " to verify that **${Permission.EMBED_LINKS}** is checked.", channel)
            }

            /*
             * Create the message in the channel.
             */
            return@function channel.createMessage(spec)
        })
    }*/
}