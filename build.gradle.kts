plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.vanniktech.maven.publish")
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
    compileOnly("dev.forkhandles:time4k")

    // http4k
    compileOnly(platform(Http4k.bom))
    compileOnly(Http4k.client.websocket)
    compileOnly("org.http4k:http4k-api-openapi")
    compileOnly("org.http4k:http4k-connect-amazon-sqs")
    compileOnly("org.http4k:http4k-connect-amazon-dynamodb")

    testImplementation(kotlin("test"))
    testImplementation(Http4k.testing.kotest)
    testImplementation(Http4k.server.jetty)
    testImplementation("org.slf4j:slf4j-log4j12:_")
    testImplementation("dev.forkhandles:result4k-kotest")
    testImplementation("org.http4k:http4k-connect-amazon-sqs-fake")
    testImplementation("org.http4k:http4k-connect-amazon-dynamodb-fake")
    testImplementation("org.http4k:http4k-connect-amazon-evidently-fake")
    testImplementation("org.testcontainers:testcontainers:_")
}

configurations { // don't want to bundle dependencies in library, but they are needed in tests
    testImplementation.configure {
        extendsFrom(compileOnly.get())
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}