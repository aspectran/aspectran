# Aspectran - Java Application Framework

[![Web Frameworks Benchmark](https://web-frameworks-benchmark.vercel.app/frameworks/java/aspectran/badge-language.svg)](https://web-frameworks-benchmark.vercel.app/frameworks/java/aspectran/)
[![Build Status](https://github.com/aspectran/aspectran/workflows/Java%20CI/badge.svg)](https://github.com/aspectran/aspectran/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master)](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.aspectran/aspectran-project)](https://central.sonatype.com/artifact/com.aspectran/aspectran-project)
[![javadoc](https://javadoc.io/badge2/com.aspectran/aspectran-all/javadoc.svg)](https://javadoc.io/doc/com.aspectran/aspectran-all)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![asciicast](https://cdn.jsdelivr.net/gh/aspectran/aspectran-assets@main/assets/screenshots/asciinema/1264203.png)](https://asciinema.org/a/1264203)

Aspectran is a lightweight, high-performance framework for building both simple shell applications and
large enterprise web services on the JVM. It emphasizes an intuitive POJO-centric programming model while
providing powerful infrastructure features such as IoC, DI, AOP, REST support, and embedded web servers.

## Key Features

- **POJO Programming**  
  Developers interact with the framework using plain Java objects; no need to understand complex internal APIs.
- **Inversion of Control (IoC)**  
  The framework creates and manages object lifecycles, freeing developers to focus on business logic.
- **Dependency Injection (DI)**  
  Modules are wired at runtime for low coupling and high reusability.
- **Aspect-Oriented Programming (AOP)**  
  Supports cross-cutting concerns like transactions, logging, security, and exception handling.
- **RESTful Web Services**  
  Designed from the ground up for REST APIs, optimized for microservice architectures.
- **Fast Development & Startup**  
  An intuitive model enables rapid prototyping, fast startup times, and low resource overhead.
- **Production-grade Applications**  
  Runs on multiple operating systems; can be deployed as a standalone JVM application, as a background daemon or service, as a servlet in Tomcat or WildFly, or embedded in another Java application.

## Modules

- **`aspectran-all`**: A single JAR containing all Aspectran modules and their dependencies, providing a convenient all-in-one package for easy deployment.
- **`aspectran-bom`**: Aspectran Bill of Materials (BOM) for managing dependency versions.
- **`aspectran-core`**: The core module of the Aspectran framework, providing essential APIs and their implementations for building component-based applications.
- **`aspectran-daemon`**: Provides functionality to run Aspectran-based applications as background daemons on Unix-like systems or as services on Windows.
- **`aspectran-embed`**: Provides the ability to embed Aspectran within other Java applications, allowing them to leverage Aspectran's features without a full container setup.
- **`aspectran-logging`**: Provides a flexible logging module for Aspectran, with Logback as the default implementation. It includes bridges for various logging APIs like JCL, JUL, and Log4j, routing them to SLF4J.
- **`aspectran-shell`**: A module for building interactive command-line (shell) applications using the Aspectran framework, enabling rapid development of powerful console-based tools.
- **`aspectran-shell-jline`**: Enhances Aspectran-based interactive shell applications with the feature-rich JLine 3 library, providing advanced capabilities like command history, completion, and a better terminal experience.
- **`aspectran-test`**: Provides a specialized testing infrastructure for Aspectran, including a JUnit 5 extension and declarative context bootstrapping for both core and web environments.
- **`aspectran-utils`**: A collection of miscellaneous utility classes used across the Aspectran framework, providing common functionalities for tasks like string manipulation, resource loading, and cryptography.
- **`aspectran-web`**: Provides foundational web infrastructure and container-agnostic abstractions for building web applications and RESTful services without a direct dependency on the Servlet API. It includes generic HTTP request and response adapters, RFC 6265-compliant cookie management, CORS processing, ETag caching interceptor, and locale resolution mechanisms, designed to run seamlessly across diverse runtimes such as Netty, Undertow, and Servlet containers.
- **`aspectran-web-servlet`**: Specializes the Aspectran web infrastructure for Jakarta Servlet environments (such as Apache Tomcat, Jetty, WildFly, and standard WAR deployments). It provides comprehensive integration with the Servlet API, including `ServletWebService`, `WebActivityServlet`, request and response adapters, HTTP session management, servlet filters, lifecycle listeners, and JSP custom tag libraries (`aspectran.tld`).
- **`aspectran-rss-lettuce`**: Provides a Redis-based session store implementation for Aspectran, using the high-performance Lettuce client for scalable and persistent session management.
- **`aspectran-with-jetty`**: Provides an embedded Jetty web server, allowing Aspectran to run as a self-contained, flexible, and mature web application.
- **`aspectran-with-netty`**: Provides an embedded Netty server, allowing Aspectran to run as a high-performance, non-blocking, servlet-less microservice and REST API runtime with multi-context routing and WebSocket support.
- **`aspectran-with-undertow`**: Provides an embedded Undertow web server, allowing Aspectran to run as a self-contained, high-performance web application.
- **`aspectran-with-freemarker`**: Provides integration with the FreeMarker template engine, allowing developers to use FreeMarker templates for rendering views in Aspectran applications.
- **`aspectran-with-pebble`**: Provides integration with the Pebble template engine, allowing developers to use Pebble templates for rendering views in Aspectran applications.
- **`aspectran-with-thymeleaf`**: Provides integration with the Thymeleaf template engine, allowing developers to use Thymeleaf templates for rendering views in Aspectran applications.
- **`aspectran-with-jpa`**: Provides integration with Jakarta Persistence API (JPA), enabling seamless database access and transaction management in Aspectran applications.
- **`aspectran-with-mybatis`**: Provides integration with the MyBatis persistence framework, enabling seamless database access and transaction management in Aspectran applications.
- **`aspectran-with-logback`**: A POM module that bundles Logback dependencies, primarily for building and testing other modules.

## Supported Execution Environments

- Command-line shell applications (interactive and background modes)
- Standalone background daemons (Unix-like systems) and Windows services
- Embedded web servers: Netty, Undertow, Jetty
- Servlet containers: Apache Tomcat, Jetty, WildFly

## Building

First, clone the repository:
```sh
git clone https://github.com/aspectran/aspectran.git
cd aspectran
```

Then, run the build script for your operating system:
```sh
# On Unix-like systems (Linux, macOS, Git Bash)
./build.sh rebuild

# On Windows (Command Prompt or PowerShell)
.\build.bat rebuild
```
Alternatively, you can use Maven directly: `./mvnw clean install`.

_**Java 21 or higher**: Aspectran requires Java 21 as the minimum runtime version._

## Running the Demo

Run the demo script for your operating system:
```sh
# On Unix-like systems (Linux, macOS, Git Bash)
./build.sh demo

# On Windows (Command Prompt or PowerShell)
.\build.bat demo
```
Then, open a browser at [http://localhost:8080](http://localhost:8080).

## Continuous Integration

Automated testing and CI/CD pipelines are managed via [GitHub Actions](https://github.com/aspectran/aspectran/actions).

## Links

- Official Website: [https://aspectran.com/](https://aspectran.com/)
- Live Demo: [https://public.aspectran.com/](https://public.aspectran.com/)
- API Documentation (Javadoc): [https://javadoc.io/doc/com.aspectran/aspectran-all](https://javadoc.io/doc/com.aspectran/aspectran-all)

## License

Aspectran is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
