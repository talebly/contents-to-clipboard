plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.24"
  id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.talebly"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// Define the editions you want to support
val editions = listOf("IC", "IU")

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }

  buildSearchableOptions {
    enabled = false
  }
}

// Create tasks for each edition
editions.forEach { edition ->
  tasks.register<org.jetbrains.intellij.tasks.BuildPluginTask>("build${edition}Plugin") {
    group = "build"
    description = "Builds the plugin for IntelliJ IDEA $edition edition"

    // Configure the intellij plugin per task
    intellij {
      version.set("2023.2.6")
      type.set(edition)
      plugins.set(listOf(/* Plugin Dependencies */))
    }

    // Set the destination directory for the plugin distribution
    destinationDirectory.set(layout.buildDirectory.dir("distributions/$edition"))

    // Ensure the plugin archive has the edition in its name
    archiveBaseName.set("${project.name}-$edition")

    // Optionally, configure signing and publishing here if needed
  }
}

// Create a task to build all editions
tasks.register("buildAllPlugins") {
  group = "build"
  description = "Builds the plugin for all specified editions"

  dependsOn(editions.map { "build${it}Plugin" })
}