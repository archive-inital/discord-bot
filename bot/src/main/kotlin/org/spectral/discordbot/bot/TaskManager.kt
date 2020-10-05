package org.spectral.discordbot.bot

import org.spectral.logger.logger
import reactor.core.Disposable
import reactor.core.scheduler.Schedulers

/**
 * The bot scheduled task manager instance.
 */
class TaskManager {

    /**
     * Backing storage of tasks this object is managing.
     */
    private val tasks = mutableListOf<Disposable>()

    /**
     * Stops and destroys the scheduled tasks.
     */
    fun stop() {
        logger.info("Stopping task manager.")

        /*
         * Dispose each task.
         */
        this.tasks.forEach { task ->
            task.dispose()
        }
    }

    companion object {

        /**
         * The default scheduler for the bot.
         */
        private val DEFAULT_SCHEDULER = Schedulers.boundedElastic()
    }
}