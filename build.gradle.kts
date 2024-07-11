import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("jacoco")
}

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(kotlin("stdlib"))
    compileOnly("org.slf4j:slf4j-api:2.0.7")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    compileOnly("io.split.client:java-client:4.4.4")
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.h2database:h2:2.1.214")
    compileOnly("com.michael-bull.kotlin-result:kotlin-result:1.1.16")
    compileOnly("com.github.ksuid:ksuid:1.1.2")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")

    //forKHandles
    compileOnly(platform("dev.forkhandles:forkhandles-bom:2.19.0.0"))
    compileOnly("dev.forkhandles:values4k")
    compileOnly("dev.forkhandles:result4k")

    // exposed
    compileOnly(platform("org.jetbrains.exposed:exposed-bom:0.41.1"))
    compileOnly("org.jetbrains.exposed:exposed-jdbc")
    compileOnly("org.jetbrains.exposed:exposed-dao")

    // aws v2
    compileOnly(platform("software.amazon.awssdk:bom:2.26.15"))
    compileOnly("software.amazon.awssdk:sqs")
    compileOnly("software.amazon.awssdk:dynamodb-enhanced")
    compileOnly("software.amazon.awssdk:evidently")

    // http4k
    compileOnly(platform("org.http4k:http4k-bom:5.25.0.0"))
    compileOnly("org.http4k:http4k-core")
    compileOnly("org.http4k:http4k-contract")
    compileOnly("org.http4k:http4k-format-jackson")
    compileOnly("org.http4k:http4k-format-jackson-yaml")
    compileOnly("org.http4k:http4k-format-moshi")
    compileOnly("org.http4k:http4k-format-gson")
    compileOnly("org.http4k:http4k-client-websocket")

    // http4k-connect
    compileOnly(platform("org.http4k:http4k-connect-bom:5.18.0.0"))
    compileOnly("org.http4k:http4k-connect-amazon-sqs")
    compileOnly("org.http4k:http4k-connect-amazon-evidently")
    compileOnly("org.http4k:http4k-connect-amazon-dynamodb")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.4.2")
    testImplementation("org.http4k:http4k-testing-kotest")
    testImplementation("org.http4k:http4k-server-jetty11")
    testImplementation("org.slf4j:slf4j-log4j12:2.0.7")
    testImplementation("dev.forkhandles:result4k-kotest")
    testImplementation("org.http4k:http4k-connect-amazon-sqs-fake")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake")
    testImplementation("org.http4k:http4k-connect-amazon-evidently-fake")
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    coordinates("dev.andrewohara", "service-utils", "1.20.0")

    pom {
        name.set("Service Utils")
        description.set("Collection of useful kotlin microservice utilities")
        inceptionYear.set("2021")
        url.set("https://github.com/oharaandrew314/service-utils")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("oharaandrew314")
                name.set("Andrew O'Hara")
                url.set("https://github.com/oharaandrew314")
            }
        }
        scm {
            url.set("https://github.com/oharaandrew314/service-utils")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}