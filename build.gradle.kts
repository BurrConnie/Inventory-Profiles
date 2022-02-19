import org.anti_ad.mc.getGitHash
import java.io.ByteArrayOutputStream



buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.2.0-beta2")
    }
}


val versionObj = Version("1", "3", "3",
                         preRelease = (System.getenv("IPNEXT_RELEASE") == null))


repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven(url = "https://maven.fabricmc.net") {
        name = "Fabric"
    }
    maven("https://maven.terraformersmc.com/releases")
    maven ("https://plugins.gradle.org/m2/")


}

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    idea
    `java-library`
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}





// This is here but it looks like it's not inherited by the child projects
tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")
    }
}

allprojects {
    version = versionObj
    group = "org.anti-ad.mc"
    ext.set("mod_artefact_version", versionObj.toCleanString())

    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
    }

}


tasks.named<Jar>("jar") {
    enabled = false
}

tasks.register("owner-testing-env") {
    onlyIf {
        System.getenv("IPNEXT_ITS_ME") != null
    }
    doLast {
        val bos = ByteArrayOutputStream()
        exec {
            workingDir = layout.projectDirectory.asFile.absoluteFile
            commandLine("${System.getenv("HOME")}/.local/bin/update-ipnext-test-env.sh",
                        project.layout.buildDirectory.dir("libs").get().asFile.absolutePath,
                        "-$versionObj")
            standardOutput = bos
        }
        logger.lifecycle(bos.toString())
    }
}


tasks.register<DefaultTask>("minimizeJars") {
    subprojects.filter {
        val isFabric = it.name.startsWith("fabric")
        val isForge = it.name.startsWith("forge")
        isFabric || isForge
    }.forEach {
        val isForge = !it.name.startsWith("fabric")
        val taskName = if (isForge) { "shadowJar" } else { "remapShadedJar" }
        val jarTask = it.tasks.named<org.gradle.jvm.tasks.Jar>(taskName)
        dependsOn(jarTask)
        if (isForge) {
            val endTask = it.tasks.named("reobfJar")
            dependsOn(endTask)
        }
        val jarFile = jarTask.get()
        val jarPath = it.layout.buildDirectory.file("libs/" + jarFile.archiveFileName.get())
        doLast {
            exec {
                this.workingDir = it.layout.projectDirectory.asFile
                val script = project.layout.projectDirectory.file("optimize-jar.sh")
                this.executable = script.asFile.absolutePath
                this.args(jarPath.get().asFile.absolutePath, it.layout.buildDirectory.get().asFile.absolutePath)

            }
        }
    }
}

tasks.register<Copy>("copyPlatformJars") {
    subprojects.filter {
        val isFabric = it.name.startsWith("fabric")
        val isForge = it.name.startsWith("forge")
        isFabric || isForge
    }.forEach {
        val isForge = !it.name.startsWith("fabric")
        val taskName = if (isForge) { "shadowJar" } else { "remapShadedJar" }
        val jarTask = it.tasks.named<org.gradle.jvm.tasks.Jar>(taskName)
        dependsOn(jarTask)
        if (isForge) {
            val endTask = it.tasks.named("reobfJar")
            dependsOn(endTask)
        }
        val jarFile = jarTask.get()
        val jarPath = it.layout.buildDirectory.file("libs/" + jarFile.archiveFileName.get())
        logger.lifecycle("""
            *************************
              ${it.path} finalized mod jar is ${jarPath.get().asFile.absoluteFile}
            *************************
        """.trimIndent())
        from(jarPath)
    }

    into(layout.buildDirectory.dir("libs"))

    subprojects.forEach {
        it.getTasksByName("build", false).forEach { t ->
            dependsOn(t)
        }
    }
    dependsOn("minimizeJars")
    finalizedBy("owner-testing-env")
}

tasks.named<DefaultTask>("build") {

    subprojects.filter {
        val isFabric = it.name.startsWith("fabric")
        val isForge = it.name.startsWith("forge")
        isFabric || isForge
    }.forEach {
        dependsOn(it.tasks["build"])
    }
    dependsOn(tasks["copyPlatformJars"])
    //finalizedBy(tasks["copyPlatformJars"])
}

afterEvaluate {
    tasks.named<DefaultTask>("build") {
        subprojects.filter {
            val isFabric = it.name.startsWith("fabric")
            val isForge = it.name.startsWith("forge")
            isFabric || isForge
        }.forEach {
            dependsOn(it.tasks["build"])
        }
        subprojects.forEach {
            it.getTasksByName("build", false).forEach { t ->
                dependsOn(t)
            }
        }
        dependsOn(tasks["copyPlatformJars"])
    }
}

/**
 * Version class that does version stuff.
 */
@Suppress("MemberVisibilityCanBePrivate")
class Version(val major: String, val minor: String, val revision: String, val preRelease: Boolean = false) {

    val gitHash
        get() = getGitHash()

    override fun toString(): String {
        return if (!preRelease)
            "$major.$minor.$revision"
        else //Only use git hash if it's a prerelease.
            "$major.$minor.$revision-BETA+C$gitHash-SNAPSHOT"
    }

    fun toCleanString(): String {
        return if (!preRelease)
            "$major.$minor.$revision"
        else //Only use git hash if it's a prerelease.
            "$major.$minor.$revision-SNAPSHOT"
    }
}
