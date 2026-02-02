plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.3.1"
}

group = "cn.dreeam.sunbox"
version = "1.0-SNAPSHOT"

repositories {
    //mavenCentral() // Maven Central
    //maven("https://maven-central.storage-download.googleapis.com/maven2") // Google Mirror - For US
    maven("https://maven.aliyun.com/repository/public") // Aliyun Mirror - For CN
}

dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    api("it.unimi.dsi:fastutil:8.5.15")
    api("com.google.guava:guava:33.4.0-jre")
    api("org.jetbrains:annotations:26.0.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    withType<JavaCompile> {
        val compilerArgs = options.compilerArgs
        options.encoding = Charsets.UTF_8.name()
        compilerArgs.add("--add-modules=jdk.incubator.vector") // Gale - Pufferfish - SIMD support
    }

    build.configure {
        dependsOn(shadowJar)
    }

    jar {
        manifest {
            attributes["Main-Class"] = rootProject.group.toString() + ".SunBox"
        }
    }
}
