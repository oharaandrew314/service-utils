plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("maven-publish")
    id("jacoco")
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {

    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.slf4j:slf4j-api:2.0.7")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    compileOnly("io.split.client:java-client:4.4.4")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("com.michael-bull.kotlin-result:kotlin-result:1.1.16")

    //forKHandles
    compileOnly(platform("dev.forkhandles:forkhandles-bom:2.3.0.0"))
    compileOnly("dev.forkhandles:values4k")
    compileOnly("dev.forkhandles:result4k")

    // exposed
    compileOnly(platform("org.jetbrains.exposed:exposed-bom:0.41.1"))
    compileOnly("org.jetbrains.exposed:exposed-jdbc")
    compileOnly("org.jetbrains.exposed:exposed-dao")

    // aws v1
    compileOnly(platform("com.amazonaws:aws-java-sdk-bom:1.12.445"))
    compileOnly("com.amazonaws:aws-java-sdk-dynamodb")
    compileOnly("com.amazonaws:aws-java-sdk-sqs")

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

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.4.2")
    testImplementation("org.http4k:http4k-testing-kotest")
    testImplementation("com.github.oharaandrew314:mock-aws-java-sdk:1.2.0")
    testImplementation("org.slf4j:slf4j-log4j12:2.0.7")
    testImplementation("dev.mrbergin:result4k-kotest-matchers:1.0.0")
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
