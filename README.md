# Aspectran - Java application framework

[![Build Status](https://github.com/aspectran/aspectran/workflows/Java%20CI/badge.svg)](https://github.com/aspectran/aspectran/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master)](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.aspectran/aspectran-project)](https://central.sonatype.com/artifact/com.aspectran/aspectran-project)
[![javadoc](https://javadoc.io/badge2/com.aspectran/aspectran-all/javadoc.svg)](https://javadoc.io/doc/com.aspectran/aspectran-all)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![asciicast](https://asciinema.org/a/325210.png)](https://asciinema.org/a/325210)

Aspectran is a lightweight, high‑performance framework for building both simple shell applications and
large enterprise web services on the JVM. It emphasizes an intuitive POJO‑centric programming model while
providing powerful infrastructure features such as IoC, DI, AOP, REST support, and embedded web servers.

## Key Features

- **POJO Programming**  
  Developers interact with the framework using plain Java objects; no need to understand complex internal APIs.
- **Inversion of Control (IoC)**  
  The framework creates and manages object lifecycles, freeing developers to focus on business logic.
- **Dependency Injection (DI)**  
  Modules are wired at runtime for low coupling and high reusability.
- **Aspect‑Oriented Programming (AOP)**  
  Supports cross‑cutting concerns like transactions, logging, security, and exception handling.
- **RESTful Web Services**  
  Designed from the ground up for REST APIs, optimized for microservice architectures.
- **Fast Development & Startup**  
  Intuitive model leads to rapid prototyping and faster runtime compared to many other frameworks.
- **Production‑grade Applications**  
  Runs on multiple OSes; can be deployed as a standalone JVM app, servlet in Tomcat/WildFly, or embedded in another Java application.

## Modules

- **`aspectran-all`**: A single JAR containing all Aspectran modules and their dependencies, providing a convenient all-in-one package for easy deployment.
- **`aspectran-bom`**: Aspectran Bill of Materials (BOM) for managing dependency versions.
- **`aspectran-core`**: The core module of the Aspectran an framework, providing essential APIs and their implementations for building component-based applications.
- **`aspectran-daemon`**: Provides functionality to run Aspectran-based applications as background daemons on Unix-like systems or as services on Windows.
- **`aspectran-embed`**: Provides the ability to embed Aspectran within other Java applications, allowing them to leverage Aspectran's features without a full container setup.
- **`aspectran-logging`**: Provides a flexible logging module for Aspectran, with Logback as the default implementation. It includes bridges for various logging APIs like JCL, JUL, and Log4j, routing them to SLF4J.
- **`aspectran-shell`**: A module for building interactive command-line (shell) applications using the Aspectran framework.
- **`aspectran-shell-jline`**: Enhances Aspectran-based interactive shell applications with the feature-rich JLine 3 library.
- **`aspectran-utils`**: A collection of miscellaneous utility classes used across the Aspectran framework.
- **`aspectran-web`**: Provides support for building web applications, compatible with Jakarta EE.
- **`aspectran-rss-lettuce`**: Provides a Redis-based session store implementation for Aspectran, using the high-performance Lettuce client.
- **`aspectran-with-jetty`**: Provides an embedded Jetty web server, allowing Aspectran to run as a self-contained, flexible, and mature web application.
- **`aspectran-with-undertow`**: Provides an embedded Undertow web server, allowing Aspectran to run as a self-contained, high-performance web application.
- **`aspectran-with-freemarker`**: Provides support for the FreeMarker template engine.
- **`aspectran-with-pebble`**: Provides support for the Pebble template engine.
- **`aspectran-with-thymeleaf`**: Provides support for the Thymeleaf template engine.
- **`aspectran-with-jpa`**: Provides integration with Jakarta Persistence API (JPA).
- **`aspectran-with-mybatis`**: Provides integration with the MyBatis persistence framework.
- **`aspectran-with-logback`**: A POM module that bundles Logback dependencies, primarily for building and testing other modules.

## Supported Execution Environments

- Command‑line shell (interactive & background modes)
- Embedded web servers: Undertow, Jetty
- Servlet containers: Apache Tomcat, WildFly

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
Then, open a browser at http://localhost:8080.

## Continuous Integration

- [GitHub Actions](https://github.com/aspectran/aspectran/actions) workflows handle CI/CD.

## Links

- Official site – [https://aspectran.com/](https://aspectran.com/)
- Demo – [https://public.aspectran.com/](https://public.aspectran.com/)
- API docs – [https://javadoc.io/doc/com.aspectran/aspectran-all](https://javadoc.io/doc/com.aspectran/aspectran-all)

## License

Aspectran is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
