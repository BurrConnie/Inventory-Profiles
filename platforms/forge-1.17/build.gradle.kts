import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.modrinth.minotaur.TaskModrinthUpload
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension
import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace
import org.anti_ad.mc.configureCommon
import org.anti_ad.mc.platformsCommonConfig
import proguard.gradle.ProGuardTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val supported_minecraft_versions = listOf("1.17.1")
val mod_loader = "forge"
val mod_version = project.version
val minecraft_version = "1.17.1"
val forge_version = "37.0.112"
val mod_artefact_version = project.ext["mod_artefact_version"]


logger.lifecycle("""
    ***************************************************
    Processing "${project.path}"
    supported versions: $supported_minecraft_versions
    loader: $mod_loader
    mod version: $mod_version
    building against MC: $minecraft_version
    ***************************************************
    """.trimIndent())

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net/maven") }
        mavenCentral()

        //this is where out custom version of org.spongepowered.mixingradle is
        //I hope I'll be able to remove it soon
        maven {
            setUrl("../../temp/mixingradle-repo")
        }

        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    }
    dependencies {
        classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "5.1.+")
        classpath(group = "org.spongepowered", name = "mixingradle", version = "0.8.1-SNAPSHOT" )
    }
}



configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(30, "seconds")
}

apply(from = "https://raw.githubusercontent.com/SizableShrimp/Forge-Class-Remapper/main/classremapper.gradle")

//I have no idea why but these MUST be here and not in plugins {}...
apply(plugin = "net.minecraftforge.gradle")
apply(plugin = "org.spongepowered.mixin")



plugins {
    java
    idea
    `maven-publish`
    signing
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "1.2.1"
}

configureCommon()
platformsCommonConfig()


java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.5"
    jvmTarget = "16"
}

group = "org.anti-ad.mc"

repositories {
    maven { url = uri("https://maven.minecraftforge.net/maven") }
    mavenCentral()
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven {
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup ("curse.maven")
        }
    }
}

val fg: net.minecraftforge.gradle.userdev.DependencyManagementExtension = project.extensions["fg"] as net.minecraftforge.gradle.userdev.DependencyManagementExtension

dependencies {
    "shadedApi"(project(":common"))

    "shadedApi"("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    "shadedApi"("org.jetbrains.kotlin:kotlin-stdlib-common:1.5.31")

    //"implementation"("org.jetbrains.kotlin:kotlin-stdlib:1.5.21")
    //"implementation"("org.jetbrains.kotlin:kotlin-stdlib-common:1.5.21")

    "minecraft"("net.minecraftforge:forge:$minecraft_version-$forge_version")
    "implementation"("org.spongepowered:mixin:0.8.3-SNAPSHOT")
    "annotationProcessor"("org.spongepowered:mixin:0.8.3-SNAPSHOT:processor")
    "testAnnotationProcessor"("org.spongepowered:mixin:0.8.3-SNAPSHOT:processor")

    runtimeOnly ( fg.deobf("curse.maven:nekoration-508028:3579060"))
}

afterEvaluate {
    project.sourceSets.getByName("main") {
        this.java.srcDirs("./src/shared/java")
    }
}

if ("true" == System.getProperty("idea.sync.active")) {
    afterEvaluate {
        tasks.withType<JavaCompile>().all {
            options.annotationProcessorPath = files()
        }
    }
}




tasks.register<Copy>("copyMixinMappings") {
    val inName = layout.buildDirectory.file("tmp/compileJava/mixin.refmap.json")
    val outName = layout.buildDirectory.file("resources/main/")
    from(inName)
    into(outName)
    rename {
        "ipnext.refmap.json"
    }
}


tasks.jar {
    manifest {
        attributes(mapOf(
            "MixinConfigs" to "mixins.ipnext.json"
        ))
    }
    dependsOn("copyMixinMappings")
}

tasks.register<Copy>("copyProGuardJar") {

    var shadow = tasks.getByName<ShadowJar>("shadowJar");
    val fromJarName = shadow.archiveBaseName.get()
    val fabricRemapJar = tasks.named<ShadowJar>("shadowJar").get()
    val inName = layout.buildDirectory.file("libs/" + fabricRemapJar.archiveFileName.get().replace("-shaded", "-all-proguard"))
    val outName = fabricRemapJar.archiveFileName.get().replace("-shaded", "")
    logger.lifecycle("""
        
        ******************************
        will copy from: $inName
        to $outName
        ******************************
        
    """.trimIndent())
    from(
        inName
        )
    rename {
        outName
    }
    into(layout.buildDirectory.dir("libs"))
}

val proguard by tasks.registering(ProGuardTask::class) {

    configuration("../../proguard.txt")
    printmapping {
        project.layout.buildDirectory.file("proguard/mappings.map")
    }
    // project(":platforms:fabric_1_17").tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFileName

    val fabricRemapJar = tasks.named<ShadowJar>("shadowJar").get()
    val inName = fabricRemapJar.archiveFileName.get().replace("-shaded", "")
    val outName = fabricRemapJar.archiveFileName.get().replace("-shaded", "-all-proguard")
    dependsOn(fabricRemapJar)
    dependsOn("jar")
    logger.lifecycle(""" 
        ****************************
        Input name for proguard:
        build/libs/${inName}
        ****************************
    """.trimIndent())
    injars("build/libs/${inName}")
    outjars("build/libs/${outName}")

    doFirst {
        libraryjars(configurations.runtimeClasspath.get().files.filter {
            !it.name.contains("InventoryProfilesNext-common")
        })
    }

}

val customJar by dummyJar( // dummy jar
    thisJarNam = "",
    fromJarNam = ""
                         )

fun dummyJar(thisJarNam: String, fromJarNam: String) = tasks.creating(Jar::class) { // dummy jar for reobf
    var shadow = tasks.getByName<ShadowJar>("shadowJar");
    val fromJarName = shadow.archiveBaseName
    val thisJarName = shadow.archiveFileName.get()
    archiveFileName.set(shadow.archiveFileName)
    dependsOn(tasks["proguard"])
    doLast {
        copy {
            from("build/libs/$fromJarName--all-proguard.jar")
            into("build/libs")
            rename { thisJarName }
        }
    }
    finalizedBy(tasks["copyProGuardJar"])
}


tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set(tasks.getByName<Jar>("jar").archiveBaseName.orNull) // Pain. Agony, even.
    archiveClassifier.set("") // Suffering, if you will.
    //finalizedBy(tasks["customJar"])
}


afterEvaluate {
    tasks.named<RenameJarInPlace>("reobfJar") {
        var shadow = tasks.getByName("customJar");
        dependsOn(shadow)
        dependsOn(tasks["copyProGuardJar"])
        //input = shadow.archiveFile.orNull?.asFile
    }
    tasks.named<ProGuardTask>("proguard") {
        var shadow = tasks.getByName<ShadowJar>("shadowJar");
        dependsOn(shadow)
    }

    tasks.register<Copy>("copyJarForPublish") {
        dependsOn("shadowJar")
        dependsOn("reobfJar")

        val forgeRemapJar = tasks.named<org.gradle.jvm.tasks.Jar>("shadowJar").get()
        from(forgeRemapJar.archiveFile.get().asFile)
        into(layout.buildDirectory.dir("publish"))
        rename {
            "$mod_loader-$minecraft_version-$mod_artefact_version.jar"
        }

        logger.lifecycle("will rename ${forgeRemapJar.archiveFile.get().asFile} to $mod_loader-$minecraft_version-$mod_artefact_version.jar")
    }
    tasks.named<DefaultTask>("build") {
        dependsOn("copyJavadoc")
        dependsOn("packageSources")
        dependsOn("copyJarForPublish")
    }

}

var rcltName = ""

configurations {
    implementation.get().extendsFrom(this.findByName("shadedApi"))
}

configure<UserDevExtension> {
    mappings(mapOf(
        "channel" to "official",
        "version" to "1.17.1"
                  ))
    runs {
        val runConfig = Action<RunConfig> {
            properties(mapOf(
                //"forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP",
                "forge.logging.console.level" to "debug",
                "mixin.env.remapRefMap" to "true",
                "mixin.env.refMapRemappingFile" to "${projectDir}/build/createSrgToMcp/output.srg",
                "mixin.debug.verbose" to "true",
                "mixin.debug.export" to "true",
                "mixin.debug.dumpTargetOnFailure" to "true",
                "bsl.debug" to "true"
                            ))
            arg("--mixin.config=mixins.ipnext.json")
            workingDirectory = project.file("run").canonicalPath
            source(org.anti_ad.mc.FilteringSourceSet(sourceSets["main"], "InventoryProfilesNext-common", logger))


            jvmArg("--add-exports=java.base/sun.security.util=ALL-UNNAMED")
            jvmArg("--add-opens=java.base/java.util.jar=ALL-UNNAMED")
            //taskName = "plamenRunClient"
            this.forceExit = false
        }
        val action = create("client", runConfig)

        rcltName = action.taskName

        create("server", runConfig)
        //create("data", runConfig)
        all {
            lazyToken("minecraft_classpath") {
                configurations["shadedApi"].copyRecursive().resolve().filter {
                    it.absolutePath.contains("kotlin")
                }.joinToString(File.pathSeparator) {
                    it.absolutePath
                }
            }
        }
    }
    afterEvaluate {

    }
}


tasks.register<Copy>("injectCommonResources") {
    tasks["prepareRuns"].dependsOn("injectCommonResources")
    dependsOn(":common:processResources")
    from(project(":common").layout.buildDirectory.dir("resources/main"))
    include("assets/**")
    into(project.layout.buildDirectory.dir("resources/main"))
}




tasks.register<DefaultTask>("fixRunJvmArgs") {
    tasks["prepareRuns"].finalizedBy("fixRunJvmArgs")


    mustRunAfter("prepareRunClient")

    doLast {
        val ts = tasks.named(rcltName, JavaExec::class)

        val newArgs = mutableListOf<String>()

        val commonPath = project(":common").buildDir.absolutePath + "/classes/"
        val javaCommon = commonPath + "java/main"
        val kotlinCommon = commonPath + "kotlin/main"
        ts.get().environment["MOD_CLASSES"] = "${ts.get().environment["MOD_CLASSES"]}:$javaCommon"
        ts.get().environment["MOD_CLASSES"] = "${ts.get().environment["MOD_CLASSES"]}:$kotlinCommon"
        val newClassPath = project.files()
        newClassPath.from(File(javaCommon))
        newClassPath.from(File(kotlinCommon))
        newClassPath.from(ts.get().classpath)
        ts.get().classpath = newClassPath
        //ts.get().classpath(File(commonPath + "java/main"))
        //ts.get().classpath(File(commonPath + "kotlin/main"))

        logger.lifecycle("Detected JVM Arguments:")
        ts.get().allJvmArgs.forEach {
            logger.lifecycle("\t$it")
        }

        ts.get().allJvmArgs.forEach {
            var processed = false

            if (it.startsWith("-DlegacyClassPath.file")) {
                val cpFile: String? = it.split("=").elementAtOrNull(1)
                if (cpFile != null) {

                    val f = File(cpFile)

                    val fcpPath = "${f.parentFile.path}/runtimeClasspath.txt"
                    logger.lifecycle("Checking if $fcpPath exists")
                    val fullCpFile = File(fcpPath)
                    val kotlinJars = mutableListOf<String>()
                    if (fullCpFile.exists()) {
                        kotlinJars.addAll(fullCpFile.readLines().filter { line ->
                            line.contains("kotlin")
                        })
                    }
                    val clean = f.readLines().filter { line ->
                        !line.contains("InventoryProfilesNext-common")
                    }
                    f.printWriter().use { pw ->
                        logger.lifecycle("Building new legacy classpath file")
                        kotlinJars.forEach { jar ->
                            logger.lifecycle("\tadding kotlin jar: $jar")
                            pw.println(jar)
                        }
                        clean.forEach { s ->
                            logger.lifecycle("\tadding other jar: $s")
                            pw.println(s)
                        }
                    }
                }
            }

            if (it.contains("InventoryProfilesNext-common")) {
                val split = it.split(":")
                var newValue: String = ""
                split.forEach { cp ->
                    if (!cp.contains("InventoryProfilesNext-common")) {
                        if (newValue != "") {
                            newValue = "$newValue:$cp"
                        } else {
                            newValue = cp
                        }
                    }
                }
                newArgs.add(newValue)
                processed = true
            }

            if (!processed) {
                newArgs.add(it)
            }

        }
        ts.get().allJvmArgs = newArgs
    }
}

tasks.register<Delete>("removeCommonResources") {
    tasks["prepareRuns"].finalizedBy("removeCommonResources")
    doLast {
        delete(project.layout.buildDirectory.dir("resources/main/assets"))
    }

    mustRunAfter("runClient")
}

gradle.buildFinished {
}

afterEvaluate {
    tasks.forEach {
        logger.info("*******************8found task: {} {} {}", it, it.name, it.group)
    }

}

tasks.register<Jar>("deobfJar") {
    from(sourceSets["main"].output)
    archiveClassifier.set("dev")
    group = "forge"
}

val deobfElements = configurations.register("deobfElements") {
    isVisible = false
    description = "De-obfuscated elements for libs"
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_API))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 16)
    }
    outgoing.artifact(tasks.named("deobfJar"))
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.addVariantsFromConfiguration(deobfElements.get()) {
    mapToMavenScope("runtime")
}




configure<com.matthewprenger.cursegradle.CurseExtension> {

    if (System.getenv("CURSEFORGE_DEPOY_TOKEN") != null && System.getenv("IPNEXT_RELEASE") != null) {
        apiKey = System.getenv("CURSEFORGE_DEPOY_TOKEN")
    }

    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        id = "495267"
        changelogType = "markdown"
        changelog = file("../../changelog.md")
        releaseType = "beta"
        supported_minecraft_versions.forEach {
            if (!it.toLowerCase().contains("pre") && !it.toLowerCase().contains("shanpshot")) {
                this.addGameVersion(it)
            }
        }
        val forgeReobfJar = tasks.named<Jar>("shadowJar").get()
        val remappedJarFile = forgeReobfJar.archiveFile.get().asFile
        mainArtifact(remappedJarFile, closureOf<com.matthewprenger.cursegradle.CurseArtifact> {
            displayName = "Inventory Profiles Next-$mod_loader-$minecraft_version-$mod_version"
        })

        afterEvaluate {
            uploadTask.dependsOn("build")
        }

    })
    options(closureOf<com.matthewprenger.cursegradle.Options> {
        debug = false
        javaIntegration = false
        forgeGradleIntegration = mod_loader == "forge"
    })
}

// ============
// modrith
// ============


val publishModrinth by tasks.registering(TaskModrinthUpload::class) {

    onlyIf {
        System.getenv("MODRINTH_TOKEN") != null && System.getenv("IPNEXT_RELEASE") != null
    }
    versionType = com.modrinth.minotaur.request.VersionType.BETA
    token = System.getenv("MODRINTH_TOKEN") // An environment property called MODRINTH that is your token, set via Gradle CLI, GitHub Actions, Idea Run Configuration, or other

    projectId = "O7RBXm3n"
    versionNumber = "$mod_loader-$minecraft_version-$mod_version" // Will fail if Modrinth has this version already
    // On fabric, use 'remapJar' instead of 'jar'
    this.changelog

    val forgeReobfJar = tasks.named<Jar>("shadowJar").get()
    val remappedJarFile = forgeReobfJar.archiveFile
    uploadFile = remappedJarFile // This is the java jar task. If it can't find the jar, try 'jar.outputs.getFiles().asPath' in place of 'jar'
    supported_minecraft_versions.forEach { ver ->
        addGameVersion(ver) // Call this multiple times to add multiple game versions. There are tools that can help you generate the list of versions
    }
    versionName = "IPN $mod_version for $mod_loader $minecraft_version"
    changelog = project.rootDir.resolve("changelog.md").readText()
    addLoader(mod_loader)

}




tasks.register<Copy>("copyJavadoc") {
    dependsOn(":common:packageJavadoc")

    val javadocJar = project(":common").tasks.named<Jar>("packageJavadoc").get()
    from(javadocJar)
    into(layout.buildDirectory.dir("publish"))
    rename {
        "$mod_loader-$minecraft_version-$mod_artefact_version-javadoc.jar"
    }
    logger.lifecycle("will rename ${javadocJar.archiveFile.get().asFile} to $mod_loader-$minecraft_version-$mod_artefact_version.jar" )
}

val prepareSourceJar = tasks.register<Copy>("prepareSourceJar") {
    dependsOn(":common:generateGrammarSource")
    dependsOn(":common:generateTestGrammarSource")
    val commonKotlinSources = project(":common").layout.projectDirectory.dir("src/main/java")
    val commonAntlrSources = project(":common").layout.projectDirectory.dir("src/main/java")
    val commonGeneratedSources = project(":common").layout.buildDirectory.dir("generated-src/antlr/main")
    val platformSources = layout.projectDirectory.dir("src/main/java")
    from(commonKotlinSources) {
        include("**/*.java")
        include("**/*.kt")
    }
    from(commonGeneratedSources) {
        include("**/*.java")
        include("**/*.tokens")
        include("**/*.interp")
    }
    from(commonAntlrSources) {
        include("**/*.g4")
    }
    from(platformSources) {
        include("**/*.java")
        include("**/*.kt")
    }
    into(layout.buildDirectory.dir("srcJarContent"))
}

tasks.register<org.gradle.jvm.tasks.Jar>("packageSources") {
    dependsOn("prepareSourceJar")
    archiveClassifier.set("sources")
    archiveBaseName.set("$mod_loader-$minecraft_version-$mod_artefact_version")
    archiveVersion.set("")
    destinationDirectory.set(layout.buildDirectory.dir("publish"))

    from(layout.buildDirectory.dir("srcJarContent"))

}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.anti-ad.mc"
            artifactId = "inventory-profiles-next"
            version = "$mod_loader-$minecraft_version-$mod_artefact_version"
            val mainArtefact = layout.buildDirectory.file("publish/$mod_loader-$minecraft_version-$mod_artefact_version.jar")
            val javadocArtefact = layout.buildDirectory.file("publish/$mod_loader-$minecraft_version-$mod_artefact_version-javadoc.jar")
            val sourcesArtefact = layout.buildDirectory.file("publish/$mod_loader-$minecraft_version-$mod_artefact_version-sources.jar")
            artifact(mainArtefact)
            artifact(javadocArtefact) {
                classifier = "javadoc"
            }
            artifact(sourcesArtefact) {
                classifier = "sources"
            }
            pom {
                url.set("https://inventory-profiles-next.github.io/")
                this.name.set("Inventory Profiles Next")
                description.set("""
                    Client side Minecraft MOD that adds multiple features to help you keep your inventory organized. 
                """.trimIndent())
                scm {
                    val connectionURL = "scm:git:https://github.com/blackd/Inventory-Profiles"
                    connection.set(connectionURL)
                    developerConnection.set(connectionURL)
                    url.set("https://github.com/blackd/Inventory-Profiles")
                }
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://raw.githubusercontent.com/blackd/Inventory-Profiles/all-in-one/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("mirinimi")
                        name.set("Plamen K. Kosseff")
                        email.set("plamen@anti-ad.org")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "local"
            val rloc = rootProject.layout.projectDirectory.dir("repo/releases")
            val sloc = rootProject.layout.projectDirectory.dir("repo/snapshots")

            setUrl {
                if (version.toString().endsWith("SNAPSHOT"))
                    sloc
                else
                    rloc
            }
        }
    }
}

val hasSigningKey = project.hasProperty("signingKeyId") || project.hasProperty("signingKey")
if(hasSigningKey) {
    doSign(project)
}

fun doSign(project: Project) {
    project.signing {
        setRequired { project.gradle.taskGraph.hasTask("publish") }

        val signingKeyId: String? = project.findProperty("signingKeyId") as String?
        val signingKey: String? = project.findProperty("signingKey") as String?
        val signingPassword: String? = project.findProperty("signingPassword") as String?
        if (signingKeyId != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        } else if (signingKey != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        sign(publishing.publications.getByName("maven"))
    }
}