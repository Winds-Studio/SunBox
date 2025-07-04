plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-rc1"
    //id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

group = "cn.dreeam.sunbox"
version = "1.0-SNAPSHOT"

repositories {
    //mavenCentral() // Maven Central
    maven("https://maven-central.storage-download.googleapis.com/maven2") // Google Mirror - For US
    //maven("https://maven.aliyun.com/repository/public") // Aliyun Mirror - For CN

    maven("https://maven.nostal.ink/repository/maven-snapshots/")
    maven("https://repo.bsdevelopment.org/releases/")
}

dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    // TODO: Notice: Enable if you needs to access Leaf internal
    //paperweight.devBundle("cn.dreeam.leaf", "1.21.5-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
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
