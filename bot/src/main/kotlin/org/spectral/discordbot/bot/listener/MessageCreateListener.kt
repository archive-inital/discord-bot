package org.spectral.discordbot.bot.listener

import discord4j.core.event.domain.message.MessageCreateEvent
import org.spectral.discordbot.bot.core.command.MessageProcessor
import reactor.core.publisher.Mono

/**
 * Fires when a message is sent in any channel this
 * bot is apart of.
 */
class MessageCreateListener : EventListener<MessageCreateEvent> {

    /**
     * Process the message creation event.
     *
     * @param event MessageCreateEvent
     * @return Mono<Void>
     */
    override fun execute(event: MessageCreateEvent): Mono<Void> {
        return MessageProcessor.processEvent(event)
    }
}