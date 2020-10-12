description = "Powerful and extensible configuration library - jackson commons"
val jacksonVersion = "2.10.1"
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":krait-core"))

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
}