package org.spectral.discordbot.bot.listener

import discord4j.core.event.domain.Event
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

/**
 * Represents a discord event.
 */
interface EventListener<T : Event> {

    /**
     * Executes the listener logic.
     *
     * @param event T
     * @return Mono<Void>
     */
    fun execute(event: T): Mono<Void>
}