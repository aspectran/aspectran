# Aspectran - Java application framework

[![Build Status](https://github.com/aspectran/aspectran/workflows/Java%20CI/badge.svg)](https://github.com/aspectran/aspectran/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master)](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran-project/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran-project)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.aspectran/aspectran.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/aspectran/aspectran/)
[![javadoc](https://javadoc.io/badge2/com.aspectran/aspectran-all/javadoc.svg)](https://javadoc.io/doc/com.aspectran/aspectran-all)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![asciicast](https://asciinema.org/a/325210.png)](https://asciinema.org/a/325210)

Aspectran is a framework for developing Java applications that can be used to build simple shell applications and large enterprise web applications.

The key features of Aspectran are:

* **Support POJO (*Plain Old Java Object*) programming model**  
  Developers do not need to know the heavy and complex objects used internally by the framework. They can exchange objects with the framework using simple Java classes.
* **Support Inversion of Control (*IoC*)**  
  What's more, the framework manages the creation and lifecycle of objects while controlling the overall flow, freeing developers to focus on their business logic.
* **Support Dependency Injection (*DI*)**  
  The framework systematically connects modules that depend on each other at runtime to ensure low coupling between modules and to increase code reusability.
* **Support Aspect-Oriented Programming (*AOP*)**  
  The framework combines additional features such as transactions, logging, security, and exception handling with code written by the developer at runtime.
* **Support building RESTful Web Services**  
  Aspectran is a framework designed from the ground up for REST API implementations and optimized for microservices architectures.
* **Fast development and startup time**  
  Aspectran's intuitive programming model guarantees fast development time and runs faster than other frameworks.

Aspectran-based applications support the following execution environments on the JVM:

* Consistent shell interface for command line applications
* Built-in high performance web application server (Undertow, Jetty)
* Daemons running as background processes

Aspectran consists of the following major packages:

* **com.aspectran.core**  
  Package containing the core features of Aspectran
* **com.aspectran.daemon**  
  Package required to build applications that run as background processes on Unix-based or Windows operating systems
* **com.aspectran.embed**  
  Package required to embed Aspectran in other Java applications
* **com.aspectran.shell**  
  Package required to build shell (aka command line) applications
* **com.aspectran.shell-jline**  
  Package for using feature-rich JLine as an interactive shell interface
* **com.aspectran.web**  
  Basic package required for building web applications
* **com.aspectran.websocket**  
  Package required to configure WebSocket endpoints
* **com.aspectran.jetty**  
  Add-on package for building web application servers using Jetty
* **com.aspectran.undertow**  
  Add-on package for building web application servers using Undertow
* **com.aspectran.rss-lettuce**  
  Package containing a Redis session store implementation using Lettuce as a client
* **com.aspectran.mybatis**  
  Add-on package for integration with MyBatis
* **com.aspectran.freemarker**  
  Add-on package for using the FreeMarker template engine
* **com.aspectran.pebble**  
  Add-on package for using the Pebble template engine

## Building

Requirements

* Maven 3.3+ (prefer included maven-wrapper)
* Java 8+

Check out and build:

```sh
git clone git://github.com/aspectran/aspectran.git
cd aspectran
./build rebuild
```

## Running the demo

To run the demo, simply use the following command after having build `Aspectran`

```sh
./build demo
```

## Continuous Integration

* [GitHub Actions](https://github.com/aspectran/aspectran/actions)

## Links

* [Official Website](https://aspectran.com/)
* [Aspectran Demo Site](https://demo.aspectran.com/)
* [API Reference](https://javadoc.io/doc/com.aspectran/aspectran-all)

## Thanks

[![JetBrains](http://aspectran.com/assets/img/jetbrains.svg)](https://www.jetbrains.com/?from=Aspectran)  
[**JetBrains**](https://www.jetbrains.com/?from=Aspectran) products are very helpful in developing **Aspectran**.  
Thank you for the Open Source License!

## License

Aspectran is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
