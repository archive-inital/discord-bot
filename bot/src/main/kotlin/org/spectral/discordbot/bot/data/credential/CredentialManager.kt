package org.spectral.discordbot.bot.data.credential

import org.spectral.logger.logger
import java.io.File
import java.nio.file.Files
import java.util.*

/**
 * Responsible for managing, loading, and providing credentials.
 */
object CredentialManager {

    private val properties: Properties

    /**
     * Initialize and load credentials
     */
    init {
        val file = File("credentials.properties")

        if(!file.exists()) {
            throw RuntimeException("'credentials.properties' was not found. Exiting.")
        }

        this.properties = Properties()

        /*
         * Load the properties from the file buffer.
         */
        this.properties.load(Files.newBufferedReader(file.toPath()))

        /*
         * Verify all credentials are present.
         */
        Credential.values.forEach { credential ->
            if(this.properties.getProperty(credential.toString()) == null) {
                logger.warn("Credential $credential not found. The associated command/service may not work.")
            }
        }
    }

    /**
     * Gets a credential value.
     *
     * @param key Credential
     * @return String
     */
    operator fun get(key: Credential): String = this.properties.getProperty(key.toString()) ?: throw IllegalArgumentException("No $key credential value found.")

}