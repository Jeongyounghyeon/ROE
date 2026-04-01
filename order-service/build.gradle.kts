import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "io.github.jeongyounghyeon"
version = "0.0.1-SNAPSHOT"
description = "ROE"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Core
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // State Machine
    implementation("org.springframework.statemachine:spring-statemachine-starter:4.0.0")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    // Messaging
    implementation("org.springframework.boot:spring-boot-starter-kafka")

    // Cache & Distributed Lock
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson:3.45.1")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Distributed Tracing
    implementation("io.micrometer:micrometer-tracing-bridge-brave")

    // Structured Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

tasks.named<BootRun>("bootRun") {
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

tasks.register<BootRun>("bootRunProd") {
    group = "application"
    description = "Run with prod profile"
    classpath = sourceSets["main"].runtimeClasspath
    args("--spring.profiles.active=prod")
    jvmArgs("--sun-misc-unsafe-memory-access=allow")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
