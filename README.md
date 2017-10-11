# Aspectran - a light-weight Java application framework

[![Build Status](https://travis-ci.org/aspectran/aspectran.svg?branch=master)](https://travis-ci.org/aspectran/aspectran)
[<img src="https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master" alt="Coverage Status"/>](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran)
[![License](https://img.shields.io/:license-apache-orange.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/aspectran-readme?pixel)](https://github.com/aspectran/aspectran)

![aspectran](http://www.aspectran.com/images/header_aspectran.png)

[![asciicast](https://asciinema.org/a/116148.png)](https://asciinema.org/a/116148)

Aspectran is a Java framework for building Web and command-line applications.
Aspectran will support most of the functionality required in an enterprise environment, and will grow into a next-generation Java application framework.

The main features of Aspectran are as follows:

* **Support various execution environments with the same configuration settings**  
  You can share the same configuration settings among different execution environments, such as the Web and CLI-based applications.
* **Support POJO (*Plain Old Java Object*) programming model**  
  You can concentrate on implementing the actual functionality you need, rather than extending the functionality by inheriting specific classes.
  The resulting value can be returned to the most simple Java object.
* **Support Inversion of Control (*IoC*)**  
  The framework controls the overall flow and invokes the functionality of the module created by the developer.
  Provides the ability to manage the creation and lifecycle of objects, allowing developers to focus on business logic.
* **Support Dependency Injection (*DI*)**  
  The framework links modules that depend on each other at runtime.
  It can maintain low coupling between modules and increase code reusability.
* **Support Aspect-Oriented Programming (*AOP*)**  
  You can write code by separating core functions and additional functions.
  Once the core functionality implementation is complete, features such as transactions, logging, security, and exception handling can be combined with core functionality.
* **Support building RESTful Web Services**

The following packages based on the `com.aspectran.core` package exist to support various execution environments.

* `com.aspectran.console`: Provides an interface for executing commands in an application built with Aspectran
* `com.aspectran.embedded`: Provides the ability to embed Aspectran in other Java applications
* `com.aspectran.web`: Provides overall functionality for building web applications within a web application container
* `com.aspectran.with.jetty`: Supports for building standalone Web application that is built-in Jetty server

## Maven dependencies

Use the following definition to use Aspectran in your maven project:

    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-all</artifactId>
      <version>5.0.0-SNAPSHOT</version>
    </dependency>

Aspectran can also be used with more low-level jars:

    <!-- You can use this to build a command line application. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-console</artifactId>
      <version>5.0.0-SNAPSHOT</version>
    </dependency>
    <!-- You can use this as a library for building other applications. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-embed</artifactId>
      <version>5.0.0-SNAPSHOT</version>
    </dependency>
    <!-- You can use it to build a web application. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-web</artifactId>
      <version>5.0.0-SNAPSHOT</version>
    </dependency>
    <!-- You can use it to build a web application with built-in Jetty 9. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-with-jetty</artifactId>
      <version>5.0.0-SNAPSHOT</version>
    </dependency>

## Building

Requirements

* Maven 3.3+ (prefer included maven-wrapper)
* Java 8+

Check out and build:

    git clone git://github.com/aspectran/aspectran.git
    cd aspectran
    mvn install
    
## Links

* [Web Site](http://www.aspectran.com/)
* [Quick Start Guide](http://www.aspectran.com/getting-started/quickstart/)
* [Downloads](http://www.aspectran.com/getting-started/download/)
* [API Reference](http://api.aspectran.com/)
* [Changelog](http://www.aspectran.com/docs/changelog/)

## License

Aspectran is freely usable, licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
