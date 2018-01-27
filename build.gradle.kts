import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.21"
    application
    id("com.github.johnrengelman.shadow") version "2.0.1"
}

// source directories
java.sourceSets {
    getByName("main") {
        java.srcDir("src/main")
        resources.srcDir("src/resources")
    }
    getByName("test") {
        java.srcDir("src/test")
    }
}


// configure kotlin
val kotlinVersion: String? by extra {
    buildscript.configurations["classpath"].resolvedConfiguration.firstLevelModuleDependencies
            .find { it.moduleName == "org.jetbrains.kotlin.jvm.gradle.plugin" }?.moduleVersion
}

application {
    mainClassName = "xerus.monstercat.MonsterUtilitiesKt"
}

/*shadowJar {
    baseName = "MonsterUtilities"
    classifier = null
}*/

// gradle run -Dexec.args="FINE save"

repositories {
    jcenter()
}

dependencies {
    compile("xerus.utils", "kotlin")
    compile("xerus.utils", "javafx")

    compile("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlinVersion)
    compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "0.+")

    compile("org.controlsfx", "controlsfx", "8.40.+")

    compile("org.apache.httpcomponents", "httpmime", "4.5.4")
    compile("com.google.apis", "google-api-services-sheets", "v4-rev496-1.+")

    testCompile("com.google.api-client", "google-api-client-java6", "1.23.0")
    testCompile("com.google.oauth-client", "google-oauth-client-jetty", "1.11.0-beta")

}

tasks {
    val MAIN = "_Main"

    getByName("runShadow").setGroup("")
    getByName("startShadowScripts").setGroup("")

    "run"(JavaExec::class) {
        group = MAIN
        args = System.getProperty("exec.args", "").split(" ")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    val release by creating(Copy::class) {
        group = MAIN
        dependsOn("shadowJar")
        from("build/libs/MonsterUtilities.jar")
        into(".")
    }

    /*val jar by creating {
        // overwrite
        group = MAIN
        dependsOn("shadowJar")
    }*/

}


/*allprojects {
	apply plugin: "idea"
	idea {
		module {
			outputDir file("build/idea/main")
			testOutputDir file("build/idea/test")
		}
	}
}*/

println("Java version: ${JavaVersion.current()}")
println("Kotlin version: $kotlinVersion")
