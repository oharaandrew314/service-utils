import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.vanniktech.maven.publish")
    id("jacoco")
}

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(kotlin("stdlib"))
    compileOnly("org.slf4j:slf4j-api:_")
    compileOnly(KotlinX.serialization.json)
    compileOnly("io.split.client:java-client:_")
    compileOnly("com.zaxxer:HikariCP:_")
    compileOnly("com.h2database:h2:_")
    compileOnly("com.github.ksuid:ksuid:_")
    compileOnly("com.github.ben-manes.caffeine:caffeine:_")
    compileOnly("redis.clients:jedis:_")

    //forKHandles
    compileOnly(platform("dev.forkhandles:forkhandles-bom:_"))
    compileOnly("dev.forkhandles:values4k")
    compileOnly("dev.forkhandles:result4k")

    // aws v2
    compileOnly(platform("software.amazon.awssdk:bom:_"))
    compileOnly("software.amazon.awssdk:sqs")
    compileOnly("software.amazon.awssdk:dynamodb-enhanced")
    compileOnly("software.amazon.awssdk:evidently")

    // http4k
    compileOnly(platform(Http4k.bom))
    compileOnly(Http4k.client.websocket)
    compileOnly("org.http4k:http4k-api-openapi")
    compileOnly("org.http4k:http4k-connect-amazon-sqs")
    compileOnly("org.http4k:http4k-connect-amazon-evidently")
    compileOnly("org.http4k:http4k-connect-amazon-dynamodb")

    testImplementation(kotlin("test"))
    testImplementation(Http4k.testing.kotest)
    testImplementation(Http4k.server.jetty)
    testImplementation("org.slf4j:slf4j-log4j12:_")
    testImplementation("dev.forkhandles:result4k-kotest")
    testImplementation("org.http4k:http4k-connect-amazon-sqs-fake")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake")
    testImplementation("org.http4k:http4k-connect-amazon-evidently-fake")
    testImplementation("com.github.fppt:jedis-mock:_")
}

configurations { // don't want to bundle dependencies in library, but they are needed in tests
    testImplementation.configure {
        extendsFrom(compileOnly.get())
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
    }
}

mavenPublishing {
    configure(KotlinJvm(sourcesJar = true))
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates("dev.andrewohara", "service-utils", "1.26.0")

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

kotlin {
    jvmToolchain(21)
}