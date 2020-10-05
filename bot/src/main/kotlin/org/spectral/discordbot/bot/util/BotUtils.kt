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
        val presence = "!help | Spectral"
        return Presence.online(Activity.streaming(presence, "https://www.twitch.tv/monstercat"))
    }
}