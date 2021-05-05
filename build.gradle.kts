import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  // Kotlin support
  id("org.jetbrains.kotlin.jvm") version "1.5.0"
  // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
  id("org.jetbrains.intellij") version "0.7.3"
  // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
  id("org.jetbrains.changelog") version "1.1.2"
  // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
  id("io.gitlab.arturbosch.detekt") version "1.16.0"
  // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
  id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project
val idePath: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
  mavenCentral()
  jcenter()
}
dependencies {
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.16.0")
  implementation("commons-io:commons-io:2.6")
  implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
  implementation("io.sentry:sentry:4.3.0")
  testImplementation("org.assertj:assertj-core:3.19.0")
  testImplementation("io.mockk:mockk:1.11.0")
}

configurations {
  implementation.configure {
    // sentry brings in a slf4j that breaks when
    // with the platform slf4j
    exclude("org.slf4j")
  }
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
  pluginName = pluginName_
  version = platformVersion
  type = platformType
  downloadSources = platformDownloadSources.toBoolean()
  updateSinceUntilBuild = true
  alternativeIdePath = idePath

  // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
  setPlugins("io.unthrottled.amii:0.8.2")
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
  config = files("./detekt-config.yml")
  buildUponDefaultConfig = true
  autoCorrect = true

  reports {
    html.enabled = false
    xml.enabled = false
    txt.enabled = false
  }
}

tasks {
  // Set the compatibility versions to 1.8
  withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }

  withType<Detekt> {
    jvmTarget = "1.8"
  }

  patchPluginXml {
    version(pluginVersion)
    sinceBuild(pluginSinceBuild)
    untilBuild(pluginUntilBuild)

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    pluginDescription(
      closure {
        File("${project.projectDir}/README.md").readText().lines().run {
          val start = "<!-- Plugin description -->"
          val end = "<!-- Plugin description end -->"

          if (!containsAll(listOf(start, end))) {
            throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
          }
          subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }
      }
    )

    changeNotes(
      closure {
        markdownToHTML(File("${project.projectDir}/docs/RELEASE-NOTES.md").readText())
      }
    )
  }

  runPluginVerifier {
    setFailureLevel(FailureLevel.COMPATIBILITY_PROBLEMS)
    ideVersions(pluginVerifierIdeVersions)
  }

  publishPlugin {
    token(System.getenv("PUBLISH_TOKEN"))
    // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
    channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
  }
}
