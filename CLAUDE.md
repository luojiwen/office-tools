# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Office Tools is a Spring Boot application for Office document processing, providing comprehensive Word document manipulation capabilities through multiple libraries. The application supports document format conversion, text extraction, watermarking, encryption, and Cloudflare R2 file storage integration.

**Tech Stack:**
- Spring Boot 4.0.1
- Java 17
- Maven 3.9.12 (with Maven wrapper)
- Aspose.Words 24.01 (system-scoped dependency)
- Spire.Doc 12.4.14
- Spring Security 2.1.4.RELEASE
- AWS SDK S3 2.25.11 (for Cloudflare R2 integration)
- Apache Commons FileUpload 1.5
- Apache Commons IO 2.15.1
- Lombok (optional)

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

# Run Spire.Doc API test (utility)
./mvnw test -Dtest=SpireApiTest
```

### Package
```bash
./mvnw clean package
```
This creates an executable JAR file in the `target/` directory.

## Architecture and Important Configuration

### Three-Layer Architecture

The application follows a classic three-tier architecture pattern:
- **Controller Layer**: REST API endpoints handling HTTP requests/responses
- **Service Layer**: Business logic interfaces and implementations
- **Domain Layer**: DTOs and configuration classes

All packages are organized under `cn.bugstack.officetools` following Spring Boot conventions.

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

**Note:** If Spire.Doc dependencies fail to resolve, verify the repository is accessible and check dependency versions.

### Package Structure

- **cn.bugstack.officetools** - Root package
  - `OfficeToolsApplication` - Main Spring Boot application class
  - `controller` - REST API controllers
    - `AsposeWordController` - Aspose.Words operations (/api/aspose/word)
    - `SpireDocController` - Spire.Doc operations (/api/spire/doc)
    - `R2Controller` - Cloudflare R2 file operations (/api/r2)
  - `service` - Business logic layer
    - `AsposeWordService` - Aspose.Words service interface
    - `SpireDocService` - Spire.Doc service interface
    - `R2Service` - Cloudflare R2 storage service interface
    - `impl` - Service implementations
  - `domain.dto` - Data transfer objects
    - `ApiResponse<T>` - Unified API response wrapper
  - `config` - Configuration classes
    - `R2Config` - Cloudflare R2 AWS S3 client configuration
  - `util` - Utility classes
    - `HttpHeaderUtil` - HTTP header utilities for file downloads

### Application Configuration

Configuration is managed through `src/main/resources/application.properties`:
- Application name: `office-tools`
- Server port: `8081`
- File upload limits: 100MB max file size, 100MB max request size
- Cloudflare R2 configuration with endpoint, credentials, and bucket name

## API Endpoints

### Aspose.Words Operations (/api/aspose/word)
- `POST /convert` - Convert document format (pdf, html, txt, etc.)
- `POST /info` - Get document information (pages, word count, etc.)
- `POST /merge` - Merge multiple Word documents
- `POST /insert` - Insert text into document (start/end position)
- `POST /replace` - Replace text in document (case-sensitive option)

### Spire.Doc Operations (/api/spire/doc)
- `POST /convert` - Convert document format (pdf, html, txt, xps, etc.)
- `POST /info` - Get document information
- `POST /extract` - Extract text content from document
- `POST /watermark` - Add text/image watermark to document
- `POST /protect` - Protect document with password
- `POST /unprotect` - Remove document protection with password

### Cloudflare R2 Operations (/api/r2)
- `POST /upload` - Upload file to R2 storage
- `DELETE /delete/{fileName}` - Delete file from R2 storage
- `GET /exists/{fileName}` - Check if file exists in R2 storage

## Development Notes

### Adding New Functionality

When adding document processing features:
1. Create service interfaces in `service` package
2. Implement service logic in `service.impl` package
3. Create REST controllers in `controller` package following existing patterns
4. Use DTOs from `domain.dto` for API responses
5. Follow Spring Boot conventions for package structure under `cn.bugstack.officetools`

### Dependencies

- **Aspose.Words**: Primary library for Word document manipulation (system-scoped)
- **Spire.Doc**: Additional document processing capabilities (from e-iceblue repository)
- **AWS SDK S3**: Used for Cloudflare R2 compatibility
- **Spring Security**: Included but not yet configured (version 2.1.4.RELEASE - note this is older than Spring Boot 4.0.1)

### Maven Wrapper

The project includes Maven wrapper files (`mvnw`, `mvnw.cmd`) ensuring consistent Maven 3.9.12 usage across environments. Always use `./mvnw` instead of system `mvn` command.

### File Upload Configuration

The application supports file uploads up to 100MB configured through:
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### Cloudflare R2 Integration

The application integrates with Cloudflare R2 object storage using AWS SDK S3:
- Compatible with S3 API
- Configured through `application.properties`
- Provides file upload, delete, and existence check operations
- Uses path-style access as required by R2

### HTTP Header Utilities

The `HttpHeaderUtil` class provides proper Content-Disposition headers for file downloads:
- Supports ASCII and non-ASCII filenames (Chinese characters)
- Uses RFC 5987 standard for international filename encoding
- Ensures compatibility with different browsers and clients

### Error Handling

All API endpoints use the `ApiResponse<T>` wrapper class for consistent error handling:
- Includes success flag, message, data payload, and timestamp
- Provides static factory methods for success and error responses
- Proper HTTP status codes are returned for different error scenarios

### Testing

The project includes:
- Spring Boot test framework integration
- Basic application context tests
- Spire.Doc API utility test for method discovery
- Test files should be placed in `src/test/java/cn/bugstack/officetools/test/`

### Key Implementation Patterns

1. **Service Layer Pattern**: Clear separation between interfaces and implementations
2. **DTO Pattern**: Unified API response structure for all endpoints
3. **Dependency Injection**: Constructor-based DI for better testability
4. **Exception Handling**: Consistent error responses with proper HTTP status codes
5. **File Processing**: Stream-based processing to handle large files efficiently
6. **Configuration**: External configuration through Spring Boot properties
7. **Cross-Origin**: CORS enabled for R2 controller endpoints

### Important Notes

- The system-scoped Aspose.Words JAR must be included in the Maven build configuration
- Cloudflare R2 credentials are stored in application.properties (consider using environment variables in production)
- File operations use `ByteArrayOutputStream` for in-memory processing suitable for documents up to 100MB
- All document processing operations support both .doc and .docx formats
- API endpoints are currently unauthenticated - Spring Security is included but not configured
- R2Controller has CORS enabled for cross-origin requests

### Environment Variables (Recommended for Production)

Instead of hardcoding R2 credentials in `application.properties`, use environment variables:
```bash
export R2_ENDPOINT=https://[your-account-id].r2.cloudflarestorage.com
export R2_BUCKET_NAME=[your-bucket-name]
export R2_ACCESS_KEY_ID=[your-access-key-id]
export R2_SECRET_ACCESS_KEY=[your-secret-access-key]
export R2_PUBLIC_DOMAIN=[your-public-domain-optional]
```

Then reference them in `application.properties`:
```properties
r2.endpoint=${R2_ENDPOINT}
r2.bucket-name=${R2_BUCKET_NAME}
r2.access-key-id=${R2_ACCESS_KEY_ID}
r2.secret-access-key=${R2_SECRET_ACCESS_KEY}
r2.public-domain=${R2_PUBLIC_DOMAIN:}
```

## Docker Deployment

### Building Docker Image

```bash
# Build the image
docker build -t office-tools:latest .

# Run the container
docker run -d \
  -p 8081:8081 \
  -e R2_ENDPOINT=https://your-account-id.r2.cloudflarestorage.com \
  -e R2_BUCKET_NAME=your-bucket-name \
  -e R2_ACCESS_KEY_ID=your-access-key-id \
  -e R2_SECRET_ACCESS_KEY=your-secret-access-key \
  office-tools:latest
```

### Dockerfile Features

The project includes an optimized multi-stage Dockerfile:
- **Build Stage**: Uses Maven 3.9.12 with Java 17 to build the application
- **Runtime Stage**: Uses lightweight Alpine JRE for smaller image size
- **Security**: Runs as non-root user
- **Health Check**: Built-in health check at `/actuator/health`
- **Signal Handling**: Uses dumb-init for proper graceful shutdowns

### Render.com Deployment

1. **Push to GitHub**: Ensure your code is on GitHub
2. **Connect Render**: Link your GitHub repository to Render.com
3. **Configure Service**:
   - Type: Web Service
   - Runtime: Docker
   - Region: Singapore (or nearest to your users)
   - Branch: master
4. **Environment Variables** (set in Render dashboard):
   ```
   R2_ENDPOINT=https://your-account-id.r2.cloudflarestorage.com
   R2_BUCKET_NAME=your-bucket-name
   R2_ACCESS_KEY_ID=your-access-key-id
   R2_SECRET_ACCESS_KEY=your-secret-access-key
   R2_PUBLIC_DOMAIN= (optional)
   ```
5. **Deploy**: Render will automatically deploy on git push

**Note**: The project includes `render.yaml` for infrastructure-as-code deployment configuration.

### Health Checks

The application exposes a health check endpoint at `/actuator/health`:
- Used by Docker HEALTHCHECK directive
- Monitored by Render.com for service health
- Returns application status and dependencies health

### Testing Docker Build Locally

```bash
# Build and test locally before deploying
docker build -t office-tools:test .
docker run -p 8081:8081 \
  -e R2_ENDPOINT=https://your-endpoint.r2.cloudflarestorage.com \
  -e R2_BUCKET_NAME=test-bucket \
  -e R2_ACCESS_KEY_ID=test-key \
  -e R2_SECRET_ACCESS_KEY=test-secret \
  office-tools:test

# Check health
curl http://localhost:8081/actuator/health
```