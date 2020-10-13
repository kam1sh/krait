import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray:com.jfrog.bintray.gradle.plugin:1.8.5")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
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
    version = "0.2.0"
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.bintray")

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

    bintray {
        user = System.getProperty("username")
        key = System.getProperty("token")
        publish = true

        setPublications("krait")

        pkg.apply {
            repo = "krait"
            userOrg = "kam1sh"
            name = project.name
            desc = project.description
            githubRepo = "kam1sh/krait"
            websiteUrl = "https://github.com/kam1sh/krait"
            issueTrackerUrl = "https://github.com/kam1sh/krait/issues"
            vcsUrl = "https://github.com/kam1sh/krait.git"
            setLabels("kotlin", "configuration", "yaml", "dotenv")
            setLicenses("MIT")
            publicDownloadNumbers = true
            version.apply {
                name = project.version.toString()
                desc = "https://github.com/kam1sh/krait"
                vcsTag = project.version.toString()
            }
        }
    }

    publishing {
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
