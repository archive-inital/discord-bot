package org.spectral.discordbot.bot.util

import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.discordjson.json.gateway.StatusUpdate

/**
 * Bot related utility methods.
 */
object BotUtils {

    /**
     * Gets a random status update.
     *
     * @return StatusUpdate
     */
    fun getStatus(): StatusUpdate {
        val presence = "!help | Spectral Powered"
        return Presence.online(Activity.streaming(presence, "https://github.com/spectral-powered/"))
    }
}