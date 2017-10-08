# [Aspectran](http://www.aspectran.com)
[![Build Status](https://travis-ci.org/aspectran/aspectran.svg?branch=master)](https://travis-ci.org/aspectran/aspectran)
[<img src="https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master" alt="Coverage Status"/>](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran)
[![License](https://img.shields.io/:license-apache-orange.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/aspectran-readme?pixel)](https://github.com/aspectran/aspectran)

![aspectran](http://www.aspectran.com/images/header_aspectran.png)

[![asciicast](https://asciinema.org/a/116148.png)](https://asciinema.org/a/116148)

Aspectran is a lightweight Java application framework for building Web, console-based, and embedded applications.
Aspectran will support most of the functionality required in an enterprise environment, and will grow into a next-generation Java application framework.

The main features of Aspectran are as follows:

* **Support various execution environments with the same configuration settings**  
  You can use the same configuration settings for different execution environments, such as Web, console-based, and other applications.
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

The following packages based on the `core` package exist to support various execution environments.

* The `com.aspectran.console` package: Support ability to build console-based applications
* The `com.aspectran.embedded` package: Support ability to embed Aspectran in other applications
* The `com.aspectran.web` package: Support ability to build web applications

## [Quick Start Guide](http://www.aspectran.com/getting-started/quickstart/)
Describes the process of developing a simple web application using Aspectran.

## [Download](http://www.aspectran.com/getting-started/download/)
Provides information on downloading the Aspectran library directly and information about Aspectran Atifact for Maven users.

## [API Reference](http://www.aspectran.com/docs/api/)
Provides Aspectran API documentation.
* [http://api.aspectran.com/4.1.0/](http://api.aspectran.com/)

## [Changelog](http://www.aspectran.com/docs/changelog/)
Provides information on Aspectran's major change history.  
You can view detailed changes history of Aspectran's source code in GitHub.

## License
Aspectran is freely usable, licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
