# Aspectran - Java application framework

[![Build Status](https://travis-ci.org/aspectran/aspectran.svg?branch=master)](https://travis-ci.org/aspectran/aspectran)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master)](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran/badge.svg#v5.5.0)](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran)
[![License](https://img.shields.io/:license-apache-orange.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/aspectran-readme?pixel)](https://github.com/aspectran/aspectran)

![aspectran](http://www.aspectran.com/images/header_aspectran.png)

[![asciicast](https://asciinema.org/a/208915.png)](https://asciinema.org/a/208915)

Aspectran is a Java framework for building Web and command-line applications.  
Aspectran will grow into a next-generation Java application framework that supports most of the functionality required in an enterprise environment.

The main features of Aspectran are as follows:

* **Support multiple execution environments with identical configuration settings**  
  You can share the same configuration settings in different execution environments, such as Web and CLI-based applications.
* **Support POJO (*Plain Old Java Object*) programming model**  
  Rather than inheriting certain classes and extending functionality, you can concentrate on implementing the functionality that is actually needed.
  The result value can be returned as the simplest Java object.
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
  Aspectran is designed to be suitable for building RESTful Web Services.

The following packages based on the `com.aspectran.core` package exist to support various execution environments.

* `com.aspectran.daemon`: Provides a daemon that runs Aspectran as a service in the background on Unix-based or Windows operating systems
* `com.aspectran.embed`: Provides an interface that can be used by embedding Aspectran in Java applications
* `com.aspectran.shell`: Provides an interactive shell that lets you use or control Aspectran directly from the command line
* `com.aspectran.shell-jline`: Provides an interactive shell using the feature-rich JLine
* `com.aspectran.web`: Provides overall functionality for building web applications within a web application container
* `com.aspectran.with.jetty`: Add-on package for integrating Embedded Jetty
* `com.aspectran.with.mybatis`: Add-on package for integrating MyBatis

## Maven dependencies

Use the following definition to use Aspectran in your maven project:

    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-all</artifactId>
      <version>5.5.0</version>
    </dependency>

Aspectran can also be used with more low-level jars:

    <!-- This can be used to build a daemon application that runs in the background. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-daemon</artifactId>
      <version>5.5.0</version>
    </dependency>
    
    <!-- This can be used to build command-line based applications. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-shell</artifactId>
      <version>5.5.0</version>
    </dependency>
    
    <!-- This can be used to build command-line based applications that use the feature-rich JLine. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-shell-jline</artifactId>
      <version>5.5.0</version>
    </dependency>
    
    <!-- This can be used to build web applications. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-web</artifactId>
      <version>5.5.0</version>
    </dependency>
    
    <!-- This can be used to embed Aspectran in your application. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-embed</artifactId>
      <version>5.5.0</version>
    </dependency>
    
    <!-- This can be used to build a web application server with built-in Jetty. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-with-jetty</artifactId>
      <version>5.5.0</version>
    </dependency>
    
    <!-- This can be used to build applications that use MyBatis. -->
    <dependency>
      <groupId>com.aspectran</groupId>
      <artifactId>aspectran-with-mybatis</artifactId>
      <version>5.5.0</version>
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

* [Official Website](http://www.aspectran.com/)
* [Aspectran Demo Site](http://demo.aspectran.com/)
* [Skylark (Online Text to Speech Web APP)](http://skylark.aspectran.com/)
* [API Reference](http://javadoc.io/doc/com.aspectran/aspectran-all)

## License

Aspectran is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
