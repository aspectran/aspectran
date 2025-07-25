# Aspectran - Java application framework

[![Build Status](https://github.com/aspectran/aspectran/workflows/Java%20CI/badge.svg)](https://github.com/aspectran/aspectran/actions?query=workflow%3A%22Java+CI%22)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master)](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.aspectran/aspectran-project)](https://central.sonatype.com/artifact/com.aspectran/aspectran-project)
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
* **Create Aspectran-powered, production-grade applications**  
  You can create reliable, standalone Java applications that run on multiple operating systems, and even run them in servlet containers or embed them into other Java applications.

Aspectran-powered applications support the following execution environments on the JVM:

* Consistent shell interface for command line applications
* Runs as a background process on Unix-based or Windows operating systems
* Built-in high performance web application server (Undertow, Jetty)
* Can also be run as a servlet in a traditional servlet container like Apache Tomcat or WildFly

Aspectran consists of the following major packages:

* **com.aspectran.core**  
  Package containing the core features of Aspectran
* **com.aspectran.daemon**  
  Package for running Aspectran-based Java applications as background processes on Unix-based or Windows operating systems
* **com.aspectran.embed**  
  Package for embedding Aspectran in non-Aspectran-based Java applications
* **com.aspectran.logging**  
  Package for using Logback as the underlying logging library
* **com.aspectran.shell**  
  Package for building interactive shell (aka command line) applications based on Aspectran
* **com.aspectran.shell-jline**  
  Package for building feature-rich Aspectran-based interactive shell applications leveraging JLine 3
* **com.aspectran.utils**  
  Miscellaneous utility classes used across multiple packages
* **com.aspectran.web**  
  Packages required to build Jakarta EE compatible web applications
* **com.aspectran.rss-lettuce**  
  Package containing a Redis session store implementation leveraging Lettuce as a client
* **com.aspectran.jetty**  
  Add-on package for using Jetty as the embedded servlet container
* **com.aspectran.undertow**  
  Add-on package for using Undertow as the embedded servlet container
* **com.aspectran.jpa**  
  Add-on package to integrate Jakarta Persistence API (JPA 3.2) support
* **com.aspectran.mybatis**  
  Add-on package for using MyBatis as an ORM framework
* **com.aspectran.freemarker**  
  Add-on package for using Freemarker as the templating engine
* **com.aspectran.pebble**  
  Add-on package for using Pebble as the templating engine
* **com.aspectran.thymeleaf**  
  Add-on package for using Thymeleaf as the templating engine

Additionally, there is a POM module *with-logback* that packages the dependencies needed to use Logback as a 
logging library, and is mainly used to build and test other modules.

## Building

- Requirements
  * Maven 3.6.3+ (prefer included maven-wrapper)
  * Java 17+

- Check out and build:
  ```sh
  git clone https://github.com/aspectran/aspectran.git
  cd aspectran
  ./build rebuild
  ```

## Running the demo

1) To run the demo, use the following command after building:
   ```sh
   ./build demo
   ```
2) Access in your browser at http://localhost:8080

## Continuous Integration

* [GitHub Actions](https://github.com/aspectran/aspectran/actions)

## Links

* [Official Website](https://aspectran.com/)
* [Aspectran Demo Site](https://public.aspectran.com/)
* [API Reference](https://javadoc.io/doc/com.aspectran/aspectran-all)

## License

Aspectran is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
