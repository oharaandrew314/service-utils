plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("maven-publish")
    id("jacoco")
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {

    compileOnly(kotlin("stdlib"))
    compileOnly("org.slf4j:slf4j-api:2.0.7")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    compileOnly("io.split.client:java-client:4.4.4")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("com.michael-bull.kotlin-result:kotlin-result:1.1.16")

    //forKHandles
    compileOnly(platform("dev.forkhandles:forkhandles-bom:2.6.0.0"))
    compileOnly("dev.forkhandles:values4k")
    compileOnly("dev.forkhandles:result4k")

    // exposed
    compileOnly(platform("org.jetbrains.exposed:exposed-bom:0.41.1"))
    compileOnly("org.jetbrains.exposed:exposed-jdbc")
    compileOnly("org.jetbrains.exposed:exposed-dao")

    // aws v2
    compileOnly(platform("software.amazon.awssdk:bom:2.17.85"))
    compileOnly("software.amazon.awssdk:sqs")
    compileOnly("software.amazon.awssdk:dynamodb-enhanced")

    // http4k
    compileOnly(platform("org.http4k:http4k-bom:4.38.0.1"))
    compileOnly("org.http4k:http4k-core")
    compileOnly("org.http4k:http4k-contract")
    compileOnly("org.http4k:http4k-format-jackson")
    compileOnly("org.http4k:http4k-format-jackson-yaml")
    compileOnly("org.http4k:http4k-format-moshi")
    compileOnly("org.http4k:http4k-format-gson")

    // http4k-connect
    compileOnly(platform("org.http4k:http4k-connect-bom:5.1.5.0"))
    compileOnly("org.http4k:http4k-connect-amazon-sqs")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.4.2")
    testImplementation("org.http4k:http4k-testing-kotest")
    testImplementation("org.slf4j:slf4j-log4j12:2.0.7")
    testImplementation("dev.forkhandles:result4k-kotest")
    testImplementation("org.http4k:http4k-connect-amazon-sqs-fake")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake")
}

configurations { // don't want to bundle dependencies in library, but they are needed in tests
    testImplementation.configure {
        extendsFrom(compileOnly.get())
    }
}

allprojects {
    jacoco {
        toolVersion = "0.8.7"
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
    }
}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        artifacts {
            kotlinSourcesJar
        }
    }

    artifacts {
        archives(sourcesJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
