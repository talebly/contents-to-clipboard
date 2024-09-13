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

// Configure Gradle IntelliJ Plugin
intellij {
  version.set("2023.2.6")
  type.set(System.getenv().getOrDefault("INTELLIJ_TYPE", editions.first()))
  plugins.set(listOf(/* Plugin Dependencies */))
}

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
  tasks.register("build${edition.replaceFirstChar { it.uppercase() }}Plugin") {
    dependsOn("buildPlugin")
    doLast {
      val originalFile = layout.buildDirectory.file("distributions/${project.name}-${project.version}.zip").get().asFile
      val newFile = layout.buildDirectory.file("distributions/${project.name}-${project.version}-$edition.zip").get().asFile
      originalFile.renameTo(newFile)
    }
  }
}

// Create a task to build all editions
tasks.register("buildAllPlugins") {
  dependsOn(editions.map { "build${it.replaceFirstChar { it.uppercase() }}Plugin" })
}
