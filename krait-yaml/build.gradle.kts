description = "Powerful and extensible configuration library - yaml support"
val jacksonVersion = "2.10.1"
dependencies {
    api(project(":krait-core"))
    api(project(":krait-jackson-common"))
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}