import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    `maven-publish`
}

description = "Powerful and extensible configuration library"

allprojects {
    apply(plugin = "kotlin")
    repositories {
        jcenter()
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }
    group = "com.github.kam1sh.krait"
    version = "0.4.0"
}

subprojects {
    apply(plugin = "maven-publish")

    dependencies {
        api("org.slf4j:slf4j-api:1.7.30")
        api(platform("org.jetbrains.kotlin:kotlin-bom"))
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
        repositories {
            maven {
                name = "backup"
                url = uri("https://maven.closeencounterscorps.org")
                credentials {
                    username = System.getProperty("backupUsername")
                    password = System.getProperty("backupToken")
                }
            }
        }
        publications {
            create<MavenPublication>("krait") {
                from(components["java"])
            }
        }
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}
