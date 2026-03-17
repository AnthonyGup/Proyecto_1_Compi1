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
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val generatedSrcDir = "build/generated/sources/parser"

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

tasks.register<JavaExec>("generateParser") {
    group = "parser"
    mainClass.set("java_cup.Main")
    classpath = configurations["cup"]

    args = listOf(
        "-destdir", generatedSrcDir,
        "-parser", "Parser",
        "-symbols", "sym",
        "src/main/cup/parser.cup"
    )
}

tasks.register<JavaExec>("generateLexer") {
    group = "parser"
    mainClass.set("jflex.Main")
    classpath = configurations["jflex"]

    args = listOf(
        "-d", generatedSrcDir,
        "src/main/jflex/lexer.flex"
    )
}

tasks.register("generateParserAndLexer") {
    dependsOn("generateParser", "generateLexer")
}

tasks.named("preBuild") {
    dependsOn("generateParserAndLexer")
}
