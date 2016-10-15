# [Aspectran](http://www.aspectran.com)
[![Build Status](https://travis-ci.org/aspectran/aspectran.svg?branch=master)](https://travis-ci.org/aspectran/aspectran)
[![Coverage Status](https://coveralls.io/repos/github/aspectran/aspectran/badge.svg?branch=master)](https://coveralls.io/github/aspectran/aspectran?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran)
[![License](http://img.shields.io/:license-apache-orange.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/aspectran-readme?pixel)](https://github.com/aspectran/aspectran)

![aspectran](http://www.aspectran.com/images/header_aspectran.png)

Aspectran은 엔터프라이즈급 자바 어플리케이션을 구축하기 위한 경량 프레임워크입니다.
웹 환경, 콘솔 기반 또는 내장 어플리케이션 등의 다른 실행 환경에서 동일한 환경 설정 구성으로 동일한 서비스를 제공할 수 있는 것이 특징입니다.
Aspectran은 엔터프라이즈 환경에서 요구하는 대부분의 기능을 지원하며, 더 나아가 자바 어플리케이션 프레임워크의 새로운 표준을 제시하기 위한 차세대 프레임워크입니다.

Aspectran은 다음과 같은 핵심 기능을 지원합니다.

* POJO(*Plain Old Java Object*) 방식의 프로그래밍을 지원합니다.  
  특정 클래스를 상속받아서 기능을 확장하는 방식이 아니고, 실제 필요한 핵심 로직과 기능 구현에만 집중할 수 있습니다.
  결과 값은 가장 간단한 자바 오브젝트에 담아서 반환하면 됩니다.
* 제어 반전(*Inversion of Control, IoC*)을 지원합니다.  
  프레임워크가 전체적인 흐름을 제어하면서 개발자가 작성한 모듈의 기능을 호출하는 방식입니다.
  객체에 대한 생성 및 생명주기를 관리할 수 있는 기능을 제공하며, 개발자는 비즈니스 로직에 집중하여 개발할 수 있게 됩니다.
* 의존성 주입(*Dependency Injection, DI*)을 지원합니다.  
  프레임워크가 실행시에 서로 의존하는 모듈을 연결합니다.
  모듈 간의 낮은 결합도를 유지할 수 있고, 코드 재사용성을 높일 수 있습니다.
* 관점 지향 프로그래밍(*Aspect-Oriented Programming, AOP*)을 지원합니다.  
  핵심 기능과 부가적인 기능을 분리해서 코드를 작성할 수 있습니다.
  핵심 기능이 구현된 이후에 트랜잭션이나 로깅, 보안, 예외처리와 관련된 기능을 핵심 기능과 결합할 수 있습니다.
* RESTful 웹서비스 구축 환경을 지원합니다.

Aspectran 3는 핵심 기능이 포함된 `core` 패키지를 기반으로 각각 다른 실행 환경을 지원하는 다음과 같은 패키지를 포함하고 있습니다.

* `console` 패키지:  Console 기반 응용 프로그램 구축을 지원하는 기능이 포함된 패키지
* `embedded` 패키지 :  Aspectran을 다른 응용 프로그램의 내부에서 실행할 수 있는 기능이 포함된 패키지
* `web` 패키지:  웹 어플리케이션 구축을 지원하는 기능이 포함된 패키지

동일한 설정 구성으로 실행 환경이 다른 3개의 어플리케이션을 구축할 수 있습니다.
즉, 동일한 기능을 제공하는 다른 실행 환경에서 구동되는 어플리케이션을 손 쉽게 제작할 수 있습니다.
다양한 방식으로 서비스를 제공해야 하는 엔터프라이즈 환경에서 꼭 필요한 기능입니다. 

## 주요 패키지 구조

```
com.aspectran
├── core          핵심 기능 패키지
│   ├── activity    요청, 처리, 응답을 처리하는 핵심 기능 패키지
│   ├── adapter     핵심 기능과 구현 기능간의 인터페이스를 위한 패키지
│   ├── context     공통 모듈 및 구동 환경을 구성하기 위한 패키지
│   ├── service     서비스를 제공하기 위한 핵심 기능 패키지
│   └── util        공통 유틸리티 패키지
├── console       Console 기반 어플리케이션 실행 환경을 위한 패키지
│   ├── activity    Console 기반 어플리케이션이 받은 요청의 처리와 응답을 수행하기 위한 패키지
│   ├── adapter     Console 기반 어플리케이션 실행 환경과 핵심 기능 간의 인터페이스를 위한 패키지
│   └── service     Console 기반 어플리케이션 실행 환경에 적합한 서비스를 제공하기 위한 패키지
├── embedded      임베디드 어플리케이션 실행 환경을 위한 패키지
│   ├── activity    임베디드 어플리케이션이 받은 요청의 처리와 응답을 수행하기 위한 패키지
│   ├── adapter     임베디드 어플리케이션 실행 환경과 핵심 기능 간의 인터페이스를 위한 패키지
│   └── service     임베디드 어플리케이션 실행 환경에 적합한 서비스를 제공하기 위한 패키지
├── scheduler     핵심 기능을 이용해서 내장 스케쥴링 서비스를 구현한 패키지
│   ├── activity    Job을 실행하기 위한 패키지
│   ├── adapter     내장 스케쥴링 서비스와 핵심 기능 간의 인터페이스를 위한 패키지
│   ├── service     내장 스케쥴링 서비스를 구동하기 위한 패키지
│   └── support     내장 스케쥴링 서비스 설정을 지원하는 패키지
└── web           웹 어플리케이션 실행 환경을 위한 패키지
    ├── activity    웹 어플리케이션이 받은 요청의 처리와 응답을 수행하기 위한 패키지
    ├── adapter     웹 어플리케이션 실행 환경과 핵심 기능 간의 인터페이스를 위한 패키지
    ├── service     웹 어플리케이션 실행 환경에 적합한 서비스를 제공하기 위한 패키지
    ├── startup     웹 어플리케이션 실행 환경에서 서비스를 구동하기 위한 패키지
    └── support     웹 어플리케이션에 필요한 확장 기능을 지원하기 위한 패키지 
```

## [Quick Start Guide](http://www.aspectran.com/getting-started/quickstart/)
간단한 웹 어플리케이션을 만드는 과정을 통하여 Aspectran의 기본적인 사용법과 특징을 파악할 수 있습니다.

## [Download](http://www.aspectran.com/getting-started/download/)
Aspectran 라이브러리를 적접 다운로드 받는 경로를 안내하고, Maven 사용자를 위한 Aspectran Atifact 정보를 제공합니다.

## [User Guides](http://www.aspectran.com/docs/guides/)
Aspectran 사용자를 위한 유저 가이드 문서를 제공합니다.

## [API Reference](http://www.aspectran.com/docs/api/)
Aspectran API 문서를 제공합니다.
* [http://api.aspectran.com/3.0.0/](http://api.aspectran.com/)

## [FAQ](http://www.aspectran.com/docs/faq/)
Aspectran과 관련해 자주 묻는 질문을 모았습니다.  
찾으시는 질문이 없거나 더 자세한 문의를 원하시면 [Contact](/contact/) 페이지 또는 [Aspectran Issues](https://github.com/aspectran/aspectran/issues)에 질문 또는 의견을 남겨 주시기 바랍니다.

## [Changelog](http://www.aspectran.com/docs/changelog/)
Aspectran의 주요 변경 이력에 대한 정보를 제공합니다.  

## [Modules](http://www.aspectran.com/modules/)
외부 라이브러리 연동에 필요한 자바 소스 패키지와 설정 메타데이터를 모듈 형태로 제공합니다.
* [https://github.com/aspectran/aspectran-modules](https://github.com/aspectran/aspectran-modules)

## [Examples](http://www.aspectran.com/examples/)
Aspectran을 이용해서 만든 예제 응용 프로그램을 제공하고 있습니다.  
대부분의 예제 응용 프로그램은 구글 클라우드 환경에서 구동하고 있기 때문에 직접 실행해 볼 수 있습니다.
* [https://github.com/aspectran-guides](https://github.com/aspectran-guides)

## License
Aspectran is freely usable, licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
