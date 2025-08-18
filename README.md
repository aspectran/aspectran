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

## Features

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

## Packages

- **`com.aspectran.core`** - Core framework functionality.
- **`com.aspectran.daemon`** - Run Aspectran as a background process (Unix/Windows service).
- **`com.aspectran.embed`** - Embed Aspectran into non‑Aspectran-based Java apps.
- **`com.aspectran.logging`** - Logback integration.
- **`com.aspectran.shell`** - Build simple interactive shell (CLI) apps.
- **`com.aspectran.shell-jline`** - Feature‑rich shell using JLine 3.
- **`com.aspectran.utils`** - Misc utilities shared across modules.
- **`com.aspectran.web`** - Jakarta EE compatible web support.
- **`com.aspectran.rss-lettuce`** - Redis session store via Lettuce client.
- **`com.aspectran.jetty`** - Embedded Jetty servlet container.
- **`com.aspectran.undertow`** - Embedded Undertow servlet container.
- **`com.aspectran.jpa`** - Jakarta Persistence API (JPA 3.2) integration.
- **`com.aspectran.mybatis`** - MyBatis ORM support.
- **`com.aspectran.freemarker`** - Freemarker templating engine.
- **`com.aspectran.pebble`** - Pebble templating engine.
- **`com.aspectran.thymeleaf`** - Thymeleaf templating engine.

_**`with-logback` POM module** – Packages all Logback dependencies and is primarily used for building and testing other modules._

## Supported Execution Environments

- Command‑line shell (interactive & background modes)
- Embedded web servers: Undertow, Jetty
- Servlet containers: Apache Tomcat, WildFly

## Building

```sh
# Clone and build (requires Maven 3.6.3+)
git clone https://github.com/aspectran/aspectran.git
cd aspectran
./build rebuild   # or ./mvnw clean install
```
_**Java 17 or higher**: Aspectran requires Java 17 as the minimum runtime version._

## Running the Demo
``` sh
./build demo      # builds and starts the demo application
# Open a browser at http://localhost:8080
```

## Continuous Integration

- [GitHub Actions](https://github.com/aspectran/aspectran/actions) workflows handle CI/CD.

## Links

- Official site – [https://aspectran.com/](https://aspectran.com/)
- Demo – [https://public.aspectran.com/](https://public.aspectran.com/)
- API docs – [https://javadoc.io/doc/com.aspectran/aspectran-all](https://javadoc.io/doc/com.aspectran/aspectran-all)

## License

Aspectran is released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
