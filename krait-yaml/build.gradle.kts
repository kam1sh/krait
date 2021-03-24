description = "Powerful and extensible configuration library - yaml support"
val jacksonVersion = "2.10.1"
dependencies {
    implementation(project(":krait-core"))
    implementation(project(":krait-jackson-common"))
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
}