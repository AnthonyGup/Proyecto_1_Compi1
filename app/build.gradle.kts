plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.cunoc.compiforms"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.cunoc.compiforms"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    sourceSets["main"].java.srcDir("build/generated/sources/parser")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val generatedSrcDir = "build/generated/sources/parser"
val pkmGeneratedSrcDir = "$generatedSrcDir/com/cunoc/compiforms/pkm"
val mainGeneratedSrcDir = "$generatedSrcDir/com/cunoc/compiforms"

configurations {
    create("jflex")
    create("cup")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation("com.github.vbmacher:java-cup-runtime:11b-20160615")

    "jflex"("de.jflex:jflex:1.9.1")
    "cup"("com.github.vbmacher:java-cup:11b-20160615")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.compose.ui.tooling)
}

// --- Tareas para el Parser/Lexer Principal ---
tasks.register<JavaExec>("generateParser") {
    group = "parser"
    mainClass.set("java_cup.Main")
    classpath = configurations["cup"]
    doFirst { file(mainGeneratedSrcDir).mkdirs() }

    args = listOf(
        "-destdir", mainGeneratedSrcDir,
        "-parser", "Parser",
        "-symbols", "sym",
        "src/main/cup/parser.cup"
    )
}

tasks.register<JavaExec>("generateLexer") {
    group = "parser"
    mainClass.set("jflex.Main")
    classpath = configurations["jflex"]
    args = listOf("-d", mainGeneratedSrcDir, "src/main/jflex/lexer.flex")
    doLast {
        val out = file("$mainGeneratedSrcDir/Lexer.java")
        if (out.exists()) {
            val content = out.readText(Charsets.UTF_8)
            val sanitized = if (content.startsWith("\uFEFF")) content.substring(1) else content
            out.writeText(sanitized, Charsets.UTF_8)
        }
    }
}

// --- Tareas para el Parser/Lexer PKM ---
tasks.register<JavaExec>("generatePKMParser") {
    group = "parser"
    mainClass.set("java_cup.Main")
    classpath = configurations["cup"]
    doFirst { file(pkmGeneratedSrcDir).mkdirs() }

    args = listOf(
        "-expect", "0",
        "-destdir", pkmGeneratedSrcDir,
        "-parser", "PKMParser",
        "-symbols", "pkm_sym",
        "src/main/cup/pkm_parser.cup"
    )
}

tasks.register<JavaExec>("generatePKMLexer") {
    group = "parser"
    mainClass.set("jflex.Main")
    classpath = configurations["jflex"]
    args = listOf("-d", pkmGeneratedSrcDir, "src/main/jflex/pkm_lexer.flex")
    doLast {
        val out = file("$pkmGeneratedSrcDir/PKMLexer.java")
        if (out.exists()) {
            val content = out.readText(Charsets.UTF_8)
            val sanitized = if (content.startsWith("\uFEFF")) content.substring(1) else content
            out.writeText(sanitized, Charsets.UTF_8)
        }
    }
}

tasks.register("generateParserAndLexer") {
    dependsOn("generateParser", "generateLexer", "generatePKMParser", "generatePKMLexer")
}

tasks.named("preBuild") {
    dependsOn("generateParserAndLexer")
}
