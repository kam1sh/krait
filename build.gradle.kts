import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    `maven-publish`
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
    version = "0.1"
}

subprojects {
    apply(plugin = "maven-publish")

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

    java {
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("default") {
                from(components["java"])
            }
        }
        repositories {
            maven {
                url = uri("$buildDir/repository")
            }
        }
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}
