# [Aspectran](http://www.aspectran.com)

Aspectran은 웹, 콘솔 기반 및 임베디드 응용 프로그램을 구축하기 위한 경량 Java 어플리케이션 프레임워크입니다.
Aspectran은 엔터프라이즈 환경에서 요구되는 대부분의 기능을 지원하며 차세대 Java 어플리케이션 프레임워크로 성장할 것입니다.

Aspectran의 주요 기능은 다음과 같습니다.

* 동일한 구성 설정으로 다양한 실행 환경 지원  
  웹, 콘솔 기반 및 기타 어플리케이션 같은 다른 실행 환경에 대해 동일한 구성 설정을 사용할 수 있습니다.
* POJO(*Plain Old Java Object*) 방식 프로그래밍 지원  
  특정 클래스를 상속받아서 기능을 확장해 나가는 방식이 아닌 실제 필요한 기능 구현에만 집중할 수 있습니다.
  결과 값은 가장 간단한 자바 오브젝트에 담아서 반환하면 됩니다.
* 제어 반전(*Inversion of Control, IoC*) 지원  
  프레임워크가 전체적인 흐름을 제어하면서 개발자가 작성한 모듈의 기능을 호출하는 방식입니다.
  객체에 대한 생성 및 생명주기를 관리할 수 있는 기능을 제공하며, 개발자는 비즈니스 로직에 집중하여 개발할 수 있게 됩니다.
* 의존성 주입(*Dependency Injection, DI*) 지원  
  프레임워크가 실행시에 서로 의존하는 모듈을 연결합니다.
  모듈 간의 낮은 결합도를 유지할 수 있고, 코드 재사용성을 높일 수 있습니다.
* 관점 지향 프로그래밍(*Aspect-Oriented Programming, AOP*) 지원  
  핵심 기능과 부가적인 기능을 분리해서 코드를 작성할 수 있습니다.
  핵심 기능이 구현된 이후에 트랜잭션이나 로깅, 보안, 예외처리와 관련된 기능을 핵심 기능과 결합할 수 있습니다.
* RESTful 웹서비스 구축 환경 지원

다양한 실행환경을 지원하기 위해 `core` 패키지를 기반으로하는 다음 패키지가 존재합니다.

* `com.aspectran.console` 패키지: 콘솔 기반 어플리케이션을 구축할 수 있는 기능 지원
* `com.aspectran.embedded` 패키지 : Aspectran을 다른 응용 프로그램에 내장할 수 있는 기능 지원
* `com.aspectran.web` 패키지: 웹 어플리케이션을 구축할 수 있는 기능 지원

## 주요 패키지 구조

Aspectran은 다음과 같은 주요 패키지로 구성됩니다.

```
com.aspectran
├── core          핵심 기능 패키지
│   ├── activity    요청과 응답을 처리하는 핵심 기능 패키지
│   ├── adapter     핵심 기능과 구현 기능간의 인터페이스를 위한 패키지
│   ├── context     공통 모듈 및 구동 환경을 구성하기 위한 패키지
│   ├── service     서비스를 제공하기 위한 핵심 기능 패키지
│   └── util        공통 유틸리티 패키지
├── console       Console 기반 어플리케이션 실행 환경을 위한 패키지
│   ├── activity    Console 기반 어플리케이션이 받은 요청과 응답을 처리하기 위한 패키지
│   ├── adapter     Console 기반 어플리케이션 실행 환경과 핵심 기능 간의 인터페이스를 위한 패키지
│   └── service     Console 기반 어플리케이션 실행 환경에 적합한 서비스를 제공하기 위한 패키지
├── embedded      임베디드 어플리케이션 실행 환경을 위한 패키지
│   ├── activity    임베디드 어플리케이션이 받은 요청과 응답을 처리하기 위한 패키지
│   ├── adapter     임베디드 어플리케이션 실행 환경과 핵심 기능 간의 인터페이스를 위한 패키지
│   └── service     임베디드 어플리케이션 실행 환경에 적합한 서비스를 제공하기 위한 패키지
├── scheduler     핵심 기능을 이용해서 내장 스케쥴링 서비스를 구현한 패키지
│   ├── activity    Job을 실행하기 위한 패키지
│   ├── adapter     내장 스케쥴링 서비스와 핵심 기능 간의 인터페이스를 위한 패키지
│   ├── service     내장 스케쥴링 서비스를 구동하기 위한 패키지
│   └── support     내장 스케쥴링 서비스 설정을 지원하는 패키지
└── web           웹 어플리케이션 실행 환경을 위한 패키지
    ├── activity    웹 어플리케이션이 받은 요청과 응답을 처리하기 위한 패키지
    ├── adapter     웹 어플리케이션 실행 환경과 핵심 기능 간의 인터페이스를 위한 패키지
    ├── service     웹 어플리케이션 실행 환경에 적합한 서비스를 제공하기 위한 패키지
    ├── startup     웹 어플리케이션 실행 환경에서 서비스를 구동하기 위한 패키지
    └── support     웹 어플리케이션에 필요한 확장 기능을 지원하기 위한 패키지
```

## [Quick Start Guide](http://www.aspectran.com/getting-started/quickstart/)
Aspectran을 이용해서 간단한 웹 어플리케이션을 만드는 과정을 설명합니다.

## [Download](http://www.aspectran.com/getting-started/download/)
Aspectran 라이브러리를 적접 다운로드 받는 방법과 Maven 사용자를 위한 Aspectran Atifact 정보를 제공합니다.

## [User Guides](http://www.aspectran.com/docs/guides/)
Aspectran 사용자를 위한 유저 가이드 문서를 제공합니다.

## [API Reference](http://www.aspectran.com/docs/api/)
Aspectran API 문서를 제공합니다.
* [http://api.aspectran.com/3.3.0/](http://api.aspectran.com/)

## [Changelog](http://www.aspectran.com/docs/changelog/)
Aspectran의 주요 변경 이력에 대한 정보를 제공합니다.  
Aspectran의 소스 코드에 대한 상세한 변경이력은 GitHub에서 볼 수 있습니다.

## [Modules](http://www.aspectran.com/modules/)
외부 라이브러리 연동에 필요한 자바 소스 패키지와 설정 메타데이터를 모듈 형태로 제공합니다.
* [https://github.com/aspectran/aspectran-modules](https://github.com/aspectran/aspectran-modules)

## [Examples](http://www.aspectran.com/examples/)
Aspectran을 이용해서 만든 예제 응용 프로그램을 제공하고 있습니다.
* [https://github.com/aspectran-guides](https://github.com/aspectran-guides)

## License
Aspectran은 [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0)에 따라 자유롭게 사용 가능합니다
