# [Aspectran](http://www.aspectran.com) [![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/readme?pixel)](https://github.com/topframe/aspectran)
*Aspectran*은 엔터프라이즈급 자바 웹 응용 프로그램을 구축하기 위한 가볍고 확장 가능한 프레임워크입니다.
*Aspectran*을 활용하기 위해 거창한 개념을 이해할 필요가 없습니다.  
*Aspectran*을 활용하기 위해 새로운 개념을 받아들일 필요도 없습니다.  
*Aspectran*의 몇 가지 기본 개념은 명확하고 신뢰할 수 있는 결과물을 만들 수 있도록 합니다.  
당신은 이제부터 남는 시간을 결과물의 품질향상에 투자할 수 있습니다.  

## 주요 특징
* POJO(*Plain Old Java Object*) 방식의 경량 프레임워크입니다.  
  기능 구현을 위해 특정 인터페이스를 구현하거나 상속을 받을 필요가 없습니다.
  결과 값은 가장 간단한 자바 오브젝트에 담아서 반환하면 됩니다.
* 제어 반전(*Inversion of Control, IoC*)을 지원합니다.  
  프레임워크가 전체적인 흐름을 제어하면서 사용자가 작성한 모듈의 기능을 호출하는 방식입니다.
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
│   ├── activity    요청, 처리, 응답을 처리하기 위한 패키지
│   ├── adapter     핵심 기능과 구현 기능간의 인터페이스를 위한 패키지
│   ├── context     핵심 기능 구동에 필요한 환경을 구성하기 위한 패키지
│   ├── service     핵심 기능을 제공하기 위한 패키지
│   └── util        공통 유틸리티 패키지
├── scheduler     핵심 기능을 상속받아서 Scheduler를 구현한 패키지
├── support       외부 라이브러리 지원을 위한 패키지
└── web           핵심 기능이 상속받아서 Web 환경을 구현한 패키지
```

## Quick Reference Guide
* [Introduction](introduction.md)
* [Getting started](getting-started.md)

# License
Aspectran is freely usable, licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).