# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Office Tools is a Spring Boot application for Office document processing, leveraging Aspose.Words and Spire.Doc libraries for Word document manipulation and conversion.

**Tech Stack:**
- Spring Boot 4.0.1
- Java 17
- Maven 3.9.12 (with Maven wrapper)
- Aspose.Words 24.01 (system-scoped dependency)
- Spire.Doc 12.4.14
- Spring Security 2.1.4.RELEASE

## Build and Development Commands

### Build
```bash
./mvnw clean install
```

### Run Application
```bash
./mvnw spring-boot:run
```
The application runs on port 8081 by default.

### Run Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=OfficeToolsApplicationTests

# Run specific test method
./mvnw test -Dtest=OfficeToolsApplicationTests#contextLoads
```

### Package
```bash
./mvnw clean package
```
This creates an executable JAR file in the `target/` directory.

## Architecture and Important Configuration

### System-Scoped Dependency

This project includes **aspose-words-24.01-jdk17-jie.jar** as a system-scoped dependency located at:
```
src/main/resources/lib/aspose-words-24.01-jdk17-jie.jar
```

**Critical Build Configuration:**
The `pom.xml` includes `<includeSystemScope>true</includeSystemScope>` in the spring-boot-maven-plugin configuration (line 80). This is **required** for the system-scoped JAR to be included in the packaged application. Without this, the application will fail at runtime with ClassNotFoundException.

### Repository Configuration

The project uses a custom Maven repository for Spire.Doc dependencies:
```xml
<repository>
    <id>com.e-iceblue</id>
    <name>e-iceblue</name>
    <url>https://repo.e-iceblue.cn/repository/maven-public/</url>
</repository>
```

### Package Structure

- **cn.bugstack.officetools** - Root package
  - `OfficeToolsApplication` - Main Spring Boot application class
  - Currently minimal structure (no controllers, services, or domain objects yet)

### Application Configuration

Configuration is managed through `src/main/resources/application.properties`:
- Application name: `office-tools`
- Server port: `8081`

## Development Notes

### Adding New Functionality

When adding document processing features:
1. Create controllers in appropriate sub-packages (e.g., `controller`, `service`, `domain`)
2. Use Aspose.Words API for Word document operations
3. Use Spire.Doc as alternative/complementary document processing library
4. Follow Spring Boot conventions for package structure under `cn.bugstack.officetools`

### Dependencies

- **Aspose.Words**: Primary library for Word document manipulation (system-scoped)
- **Spire.Doc**: Additional document processing capabilities (from e-iceblue repository)
- **Spring Security**: Included but not yet configured (version 2.1.4.RELEASE - note this is older than Spring Boot 4.0.1)

### Maven Wrapper

The project includes Maven wrapper files (`mvnw`, `mvnw.cmd`) ensuring consistent Maven 3.9.12 usage across environments. Always use `./mvnw` instead of system `mvn` command.