# [Aspectran](http://www.aspectran.com)
[![Build Status](https://travis-ci.org/aspectran/aspectran.svg)](https://travis-ci.org/aspectran/aspectran)
[![Dependency Status](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56eec08e35630e0029dafca6)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.aspectran/aspectran)
[![Apache 2](http://img.shields.io/badge/license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/aspectran-readme?pixel)](https://github.com/aspectran/aspectran)

![aspectran](http://www.aspectran.com/images/header_aspectran.png)

Aspectran은 엔터프라이즈급 자바 웹 응용 프로그램을 구축하기 위한 가볍고 확장 가능한 프레임워크입니다.

*Aspectran 은 방대한 개념으로 포장되어 있지 않습니다.*  
*Aspectran 은 생소한 개념을 창조하지 않습니다.*  
*Aspectran 의 직관적인 몇 가지의 개념으로 명확하고 신뢰할 수 있는 결과물을 만들 수 있습니다.*  

## 주요 기능
2008년 봄부터 개발을 시작한 이후로 7년이라는 시간이 지났습니다.
정식 버전 출시와 함께 오픈소스로 공개를 하고 여러분과 함께 만들어 가려고 합니다.
7년 전 *Aspectran* 이라는 프레임워크를 만들어야겠다는 생각을 했을 때 그 때의 감동은 아직도 가슴 한 켠에 남아 있습니다.  
작고 간단한 기능을 구현하면서 잠시 자만도 했고,
크고 복잡한 핵심기능은 1년여의 긴 고민 끝에 겨우 완성할때도 있었습니다.  
그렇게 완성된 *Aspectran* 의 주요 기능은 다음과 같습니다.

* POJO(*Plain Old Java Object*) 방식의 경량 프레임워크입니다.  
  특정 클래스를 상속받아서 기능을 확장할 필요가 없기 때문에 간단하고 명확한 프로그래밍을 할 수 있습니다.
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

## 주요 패키지 구조

```
com.aspectran
├── core          핵심 기능 패키지
│   ├── activity    요청, 처리, 응답을 처리하는 패키지
│   ├── adapter     핵심 기능과 구현 기능간의 인터페이스를 위한 패키지
│   ├── context     공통 모듈 및 구동 환경을 구성하기 위한 패키지
│   ├── service     핵심 기능 서비스를 제공하기 위한 패키지
│   └── util        공통 유틸리티 패키지
├── console       Console Application 실행환경에서 핵심 기능을 제공하기 위한 패키지
│   ├── activity    Console Application 실행환경에서 요청, 처리, 응답을 처리하는 패키지
│   ├── adapter     Console Application 실행환경과 핵심 기능 간의 인터페이스를 위한 패키지
│   └── service     입력한 명령을 해석해서 핵심 기능 서비스를 호출하는 역할을 하는 패키지
├── scheduler     핵심 기능을 상속받아서 Scheduler를 구현한 패키지
│   ├── activity    Job을 실행하기 위한 패키지
│   ├── adapter     Scheduler와 핵심 기능 간의 인터페이스를 위한 패키지
│   └── service     Scheduler 서비스를 제어하기 위한 패키지
└── web           Web 환경에서 핵심 기능을 제공하기 위한 패키지
    ├── activity    Web 환경의 요청, 처리, 응답을 처리하기 위한 패키지
    ├── adapter     Web 환경과 핵심 기능 간의 인터페이스를 위한 패키지
    ├── service     Web 환경에서 핵심 기능 서비스를 제공하기 위한 패키지
    └── startup     Web 환경에서 핵심 기능 서비스를 구동하기 위한 패키지
```

## Getting Started

* [http://www.aspectran.com/getting-started/](http://www.aspectran.com/getting-started/)

## Documentation

* [http://www.aspectran.com/docs/](http://www.aspectran.com/docs/)

# License

Aspectran is freely usable, licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).
