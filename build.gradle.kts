plugins {
    `java-library`

    id("com.github.johnrengelman.shadow") version "8.1.1" // Shades and relocates dependencies, See https://imperceptiblethoughts.com/shadow/introduction/
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3" // Automatic plugin.yml generation
}

group = "me.ShermansWorld.raidsperregion"
version = "3.0.3"
description = ""

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17)) // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")

    maven("https://repo.glaremasters.me/repository/towny/") {
        content { includeGroup("com.palmergames.bukkit.towny") }
    }

    maven("https://mvn.lumine.io/repository/maven-public/") {
        content { includeGroup("io.lumine") }
    }

    maven("https://maven.enginehub.org/repo/")

    maven("https://jitpack.io/") {
        content {
            includeGroup("com.palmergames.bukkit.towny")
            includeGroup("fr.mrmicky")
        }
    }

    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    compileOnly("com.palmergames.bukkit.towny:towny:0.98.6.0")

    compileOnly("io.lumine:Mythic-Dist:5.2.1")

    implementation("fr.mrmicky:fastboard:2.1.0")

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
        options.compilerArgs.add("-Xlint:-deprecation")
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    shadowJar {
        archiveBaseName.set(project.name)
        archiveClassifier.set("")

        // Shadow classes
        // helper function to relocate a package into our package
        fun reloc(originPkg: String, targetPkg: String) = relocate(originPkg, "${project.group}.${targetPkg}")

        reloc("fr.mrmicky.fastboard", "fastboard")
    }

    runServer {
        // Configure the Minecraft version for our task.
        minecraftVersion("1.20.4")
    }
}

bukkit {
    // Plugin main class (required)
    main = "${project.group}.RaidsPerRegion"

    // Plugin Information
    name = project.name
    prefix = "RaidsPerRegion"
    version = "${project.version}"
    description = "${project.description}"
    authors = listOf("ShermansWorld")
    apiVersion = "1.19"

    // Misc properties
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD // STARTUP or POSTWORLD
    depend = listOf("WorldEdit", "WorldGuard", "MythicMobs")
    softDepend = listOf("Towny")

    commands {
        register("raid") {
            description = "Used to start raids"
        }
        register("raidtown") {
            description = "Used to start raids"
        }
        register("raidsperregion") {
            description = "Used to cancel raids or reload plugin"
            aliases = listOf("rpr")
        }
    }
}
