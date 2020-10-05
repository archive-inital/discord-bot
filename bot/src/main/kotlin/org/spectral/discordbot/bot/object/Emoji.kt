package org.spectral.discordbot.bot.`object`

enum class Emoji(val discordNotation: String) {

    CHECK_MARK("white_check_mark"),
    WARNING("warning"),
    ACCESS_DENIED("no_entry_sign"),
    RED_CROSS("x"),
    LOCK("lock");

    override fun toString(): String {
        return this.discordNotation
    }
}