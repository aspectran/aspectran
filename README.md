# [Aspectran](http://www.aspectran.com)
[![Build Status](https://travis-ci.org/aspectran/aspectran.svg?branch=master)](https://travis-ci.org/aspectran/aspectran)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master&20170423)](https://coveralls.io/github/aspectran/aspectran?branch=master)
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

## Package Structure

Aspectran consists of the following major packages:

```
com.aspectran
├── core          Provides core interfaces and classes for the Aspectran infrastructure
│   ├── activity    A package for providing a core activity for processing request and response
│   ├── adapter     A package for providing basic adapters for integration with a core activity
│   ├── context     A Package for providing core components and configuring an execution environment
│   ├── service     A package for providing a core service using Aspectran infrastrucre
│   └── util        A package that contain miscellaneous utilities
├── console       A package for building console-based applications based on the Aspectran infrastructure
│   ├── activity    Contains a variant of the activity interface for console-based application
│   ├── adapter     Contains a variant of the adapter interface for console-based application
│   ├── inout       A package for enhanced console input and output
│   └── service     Contains a variant of the service interface for console-based application
├── embedded      A package that provides the ability to embed Aspectran in other applications
│   ├── activity    Contains a variant of the activity interface for embedded Aspectran
│   ├── adapter     Contains a variant of the adapter interface for embedded Aspectran
│   └── service     Contains a variant of the service interface for embedded Aspectran
├── scheduler     Built-in scheduler package that integrates with Aspectran infrastructure
│   ├── activity    Contains a variant of the activity interface for built-in scheduler
│   ├── adapter     Contains a variant of the adapter interface for built-in scheduler
│   ├── service     Contains a variant of the service interface for built-in scheduler
│   └── support     A package to support external modules for built-in scheduler
└── web           A package for building web applications based on the Aspectran infrastructure
    ├── activity    Contains a variant of the activity interface for web application
    ├── adapter     Contains a variant of the adapter interface for web application
    ├── service     Contains a variant of the service interface for web application
    ├── startup     Provides servlets and listeners for integration with web application
    └── support     A package to support external modules for web application integration
```

## [Quick Start Guide](http://www.aspectran.com/getting-started/quickstart/)
Describes the process of developing a simple web application using Aspectran.

## [Download](http://www.aspectran.com/getting-started/download/)
Provides information on downloading the Aspectran library directly and information about Aspectran Atifact for Maven users.

## [User Guides](http://www.aspectran.com/docs/guides/)
Provides user guide documentation for Aspectran users.

## [API Reference](http://www.aspectran.com/docs/api/)
Provides Aspectran API documentation.
* [http://api.aspectran.com/4.0.0/](http://api.aspectran.com/)

## [Changelog](http://www.aspectran.com/docs/changelog/)
Provides information on Aspectran's major change history.  
You can view detailed changes history of Aspectran's source code in GitHub.

## [Modules](http://www.aspectran.com/modules/)
Provides java source packages and configuration metadata that can integrate the various external libraries.
* [https://github.com/aspectran/aspectran-modules](https://github.com/aspectran/aspectran-modules)

## [Examples](http://www.aspectran.com/examples/)
Provides a sample application developed using Aspectran.
* [https://github.com/aspectran-guides](https://github.com/aspectran-guides)

## License
Aspectran is freely usable, licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
