plugins {
    java
    application
}

group = "org.labs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.labs.Main")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"   // кириллица в строках и комментариях
}

tasks.test {
    useJUnitPlatform()
}
