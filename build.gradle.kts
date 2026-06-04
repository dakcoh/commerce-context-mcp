plugins {
	java
	id("org.springframework.boot") version "3.5.14"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.commerce"
// GitHub Actions 릴리즈 시: ./gradlew bootJar -Pversion=X.Y.Z
// 로컬 개발 시: 0.0.1-SNAPSHOT (기본값)
version = if (project.hasProperty("version") &&
              project.property("version").toString().isNotBlank() &&
              project.property("version").toString() != "unspecified")
    project.property("version").toString()
else
    "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "1.1.7"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register("validateKnowledge") {
	group = "verification"
	description = "Validates knowledge YAML binding, schema, categories, and quality rules."
	dependsOn("test")
}
