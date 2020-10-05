package org.spectral.discordbot.bot.data

import org.spectral.logger.logger
import java.io.IOException
import java.util.*

/**
 * The config information loaded from env variables and setting properties.
 */
object Config {

    /**
     * The properties from bot.properties.
     */
    private val PROPERTIES = loadProperties()

    /**
     * Config elements.
     */
    val VERSION = PROPERTIES.getProperty("version")
    val DATABASE_NAME = PROPERTIES.getProperty("database.name")
    val DEFAULT_PREFIX = PROPERTIES.getProperty("default.prefix")

    /**
     * Loads settings from the bot.properties file.
     */
    private fun loadProperties(): Properties {
        val properties = Properties()

        try {
            val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("bot.properties")
                    ?: throw RuntimeException("Configuration file 'bot.properties' not found. Exiting.")

            properties.load(inputStream)
        } catch(e : IOException) {
            logger.error(e) { "An error occurred while loading the 'bot.properties' configuration file. Exiting." }
            throw RuntimeException(e)
        }

        return properties
    }
}