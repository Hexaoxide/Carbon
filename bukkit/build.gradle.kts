plugins {
  id("carbon.shadow-platform")
  id("net.minecrell.plugin-yml.bukkit")
  id("xyz.jpenilla.run-paper")
}

dependencies {
  implementation(projects.carbonchatCommon)

  // Server
  compileOnly(libs.paperApi)
  implementation(libs.paperLib)

  // Commands
  implementation(libs.cloudPaper)

  // Misc
  implementation(libs.bstatsBukkit)

  // Plugins
  compileOnly("me.clip:placeholderapi:2.10.9") // TODO: move this to libs.versions.yml
  compileOnly(libs.essentialsXDiscord)
}

carbonShadowPlatform {
  relocateGuice.set(true)
}

tasks {
  shadowJar {
    relocateDependency("io.papermc.lib")
    relocateDependency("io.leangen.geantyref")
  }
  runServer {
    minecraftVersion("1.18.1")
  }
}

// Generates plugin.yml automatically
bukkit {
  name = rootProject.name
  version = project.version as String
  main = "net.draycia.carbon.bukkit.CarbonChatBukkit"
  apiVersion = "1.16"
  author = "Draycia"
  depend = listOf("LuckPerms")
  softDepend = listOf("PlaceholderAPI", "EssentialsDiscord")
  website = GITHUB_REPO_URL
  permissions {
    register("carbon.clearchat") {
      description = "Clears the chat for all players except those with carbon.chearchat.exempt."
      childrenMap = mapOf("carbon.clearchat.clear" to true)
    }
    register("carbon.clearchat.clear") {
      description = "Clears the chat for all players except those with carbon.chearchat.exempt."
    }
    register("carbon.clearchat.exempt") {
      description = "Exempts the player from having their chat cleared when /clearchat is executed."
    }
    register("carbon.debug") {
      description = "Allows the sender to quickly check what carbon think's the player's primary and non-primary groups are."
    }
    register("carbon.help") {
      description = "Shows Carbon's help menu, detailing each part of Carbon's commands."
    }
    register("carbon.hideidentity") {
      description = "Prevents messages from the player from being blocked clientside."
    }
    register("carbon.ignore") {
      description = "Ignores the player, hiding messages they send in chat and in whispers."
    }
    register("carbon.ignore.exempt") {
      description = "Prevents the player from being ignored."
    }
    register("carbon.ignore.unignore") {
      description = "Removes the player from the sender's ignore list."
    }
    register("carbon.itemlink") {
      description = "Shows the player's held or equipped item in chat."
    }
    register("carbon.mute") {
      description = "Mutes the player, preventing them from sending messages or whispers."
    }
    register("carbon.mute.exempt") {
      description = "Prevents the player from being muted."
    }
    register("carbon.mute.info") {
      description = "Shows if the player is muted or now."
    }
    register("carbon.mute.notify") {
      description = "Notifies the player when someone else has been mute."
    }
    register("carbon.mute.unmute") {
      description = "Unmutes the player, allowing them to use chat and send whispers."
    }
    register("carbon.nickname") {
      description = "The nickname command, by default shows your nickname."
    }
    register("carbon.nickname.others") {
      description = "Checks/sets other player's nicknames."
    }
    register("carbon.nickname.see") {
      description = "Checks your/other player's nicknames."
    }
    register("carbon.nickname.self") {
      description = "Checks/sets your nickname."
    }
    register("carbon.nickname.set") {
      description = "Sets your/other player's nicknames."
    }
    register("carbon.reload") {
      description = "Reloads Carbon's config, channel settings, and translations."
    }
    register("carbon.whisper") {
      description = "Sends private messages to other players."
    }
    register("carbon.whisper.continue") {
      description = "Sends a message to the last player you whispered."
    }
    register("carbon.whisper.reply") {
      description = "Sends a message to the last player who messaged you."
    }
    register("carbon.whisper.vanished") {
      description = "Allows the player to send messages to vanished players."
    }
  }
}
