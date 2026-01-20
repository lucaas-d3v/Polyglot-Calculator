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

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-material2-pack:12.3.1") // Material Design icons

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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
