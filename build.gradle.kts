plugins {
    id("java")
    id("maven-publish")
}

group = "com.ducksgames.worlds"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.emortal.dev/snapshots")
}

dependencies {
    implementation("com.github.luben:zstd-jni:1.5.5-3")
    compileOnly("com.github.Minestom:Minestom:c5047b8037")
}
publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}