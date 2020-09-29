import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"

    `java-library`
}

allprojects {
    apply(plugin = "kotlin")
    repositories {
        jcenter()
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }
    group = "com.github.kam1sh"
}

subprojects {
    version = "0.1"
    dependencies {
        implementation("org.slf4j:slf4j-api:1.7.30")
        testImplementation("ch.qos.logback:logback-classic:1.2.3")
        testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    }
    tasks.withType<Test>() {
        useJUnitPlatform()
    }
}

dependencies {
    implementation(project("krait-core"))
    implementation(project("krait-dotenv"))
}



/*
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
*/

