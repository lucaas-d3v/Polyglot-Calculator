plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.k.calc.App")
}

javafx {
    version = "21.0.4"
    modules = listOf("javafx.controls")
}

tasks.test {
    useJUnitPlatform()
}
