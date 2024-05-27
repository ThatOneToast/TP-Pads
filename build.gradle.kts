plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

group = "dev.toast"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release.set(targetJavaVersion)
    }
}

tasks.assemble {
    dependsOn("shadowJar")
}

tasks {
    shadowJar {
        destinationDirectory.set(file("/Users/toast/Desktop/testserver/plugins/"))
    }
}
