import org.gradle.kotlin.dsl.*
import java.io.ByteArrayOutputStream

plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.1.0"

    // ESSENCIAL pra empacotar JavaFX sem dor:
    id("org.beryx.jlink") version "3.1.1"
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
    implementation("org.kordamp.ikonli:ikonli-material2-pack:12.3.1")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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

/* --------------------------
   RUST ENGINE: build + stage
   -------------------------- */

val engineBinName = "calc_engine"

val engineDir = layout.projectDirectory.dir("../engine")
val engineReleaseBin = engineDir.file("target/release/$engineBinName")

// Onde o binário vai parar dentro da imagem final:
val stagedNativeDir = layout.buildDirectory.dir("staging/native/linux-x86_64")
val stagedEngineBin = stagedNativeDir.map { it.file(engineBinName) }

// 1) Compila o Rust em release
val buildEngineRelease = tasks.register<Exec>("buildEngineRelease") {
    workingDir = engineDir.asFile
    commandLine("cargo", "build", "--release")
}

// 2) Copia o binário compilado pra staging e marca executável
val stageEngine = tasks.register("stageEngine") {
    dependsOn(buildEngineRelease)

    doLast {
        val src = engineReleaseBin.asFile
        require(src.exists()) { "Binário Rust não encontrado: ${src.absolutePath}" }

        val dst = stagedEngineBin.get().asFile
        dst.parentFile.mkdirs()

        src.copyTo(dst, overwrite = true)
        dst.setExecutable(true)

        println("Engine staged: ${dst.absolutePath}")
    }
}

/* --------------------------
   JLINK + JPACKAGE (deb)
   -------------------------- */

val appName = "CalculadoraPoliglota"
val appVersion = "1.0.0"

// teu ícone atual:
val iconPng = layout.projectDirectory.file("src/main/resources/icons/logo_256.png")

jlink {
    // cria runtime custom (inclui JavaFX corretamente)
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))

    launcher {
        name = "calculadorapoliglota"
        mainClass.set(application.mainClass)
    }
    jpackage {
        installerType = "deb"
        appVersion = appVersion
        imageName = "Calculadora Poliglota" // se quiser bonito no menu
        icon = iconPng.asFile.absolutePath
    }

}

/**
 * Hook: depois do jlink montar a imagem, injeta o binário rust na imagem.
 * Caminho típico da imagem gerada pelo plugin:
 * build/image/  (com bin/ e lib/)
 */
tasks.named("jlink") {
    dependsOn(stageEngine)
    doLast {
        val imageDir = layout.buildDirectory.dir("image").get().asFile
        val target = imageDir.resolve("lib/app/native/linux-x86_64/$engineBinName")
        target.parentFile.mkdirs()

        stagedEngineBin.get().asFile.copyTo(target, overwrite = true)
        target.setExecutable(true)

        println("Engine injected into image: ${target.absolutePath}")
    }
}

// tarefa final: gera o .deb
tasks.register("packageDeb") {
    dependsOn("jpackage")
    doLast {
        println("Deb gerado em: ${layout.buildDirectory.dir("jpackage").get().asFile.absolutePath}")
    }
}