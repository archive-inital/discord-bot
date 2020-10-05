package org.spectral.discordbot.bot.data.credential

/**
 * The various credential secrets for the discord bot.
 */
enum class Credential {
    DISCORD_TOKEN,
    DATABASE_USERNAME,
    DATABASE_PASSWORD,
    DATABASE_HOST,
    DATABASE_PORT;

    companion object {
        /**
         * Cached list of values.
         */
        val values = enumValues<Credential>()
    }
}