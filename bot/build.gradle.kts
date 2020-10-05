plugins {
    application
}

description = "Discord Bot Core"

dependencies {
    implementation(Library.discord4j)
}

tasks.withType<JavaExec> {
    main = "org.spectral.discordbot.bot.DiscordBot"
    workingDir = rootProject.projectDir
}