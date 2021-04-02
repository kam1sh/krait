description = "Powerful and extensible configuration library - jackson commons"
val jacksonVersion = "2.10.1"
dependencies {
    api(project(":krait-core"))

    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}