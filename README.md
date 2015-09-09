# [Aspectran](http://www.aspectran.com) [![Analytics](https://ga-beacon.appspot.com/UA-66807210-1/aspectran/readme?pixel)](https://github.com/topframe/aspectran)
Aspectran is a lightweight and extensible framework for building enterprise-ready Java Web applications.
There is no need to understand complex concepts to take advantage of the Aspectran.
There is no need to spend a completely new concept in order to take advantage of the Aspectran.
Some of the basic concepts of Aspectran allows you to create a result that can be clear and reliable.
You can spend your spare time to improve the quality of the results from now on.

## 주요 특징
* POJO(Plain Old Java Object) 방식의 경량 프레임워크이다. 기능 구현을 위해 특정 인터페이스를 구현하거나 상속을 받을 필요가 없다. 결과 값은 가장 간단한 자바 오브젝트에 담아서 반환하면 된다.
* 제어 반전(Inversion of Control, IoC)을 지원한다. 프레임워크가 전체적인 흐름을 제어하면서 사용자가 작성한 모듈의 기능을 호출하는 방식이다.
* 의존성 주입(Dependency Injection, DI)을 지원한다. 프레임워크가 실행시에 서로 의존하는 모듈을 연결한다. 모듈 간의 낮은 결합도를 유지할 수 있고, 코드 재사용성을 높일 수 있다.
* 관점 지향 프로그래밍(Aspect-Oriented Programming, AOP)를 지원한다. 핵심 기능과 부가적인 기능을 분리해서 코드를 작성할 수 있다. 핵심 기능이 구현된 이후에 트랜잭션이나 로깅, 보안, 예외처리와 관련된 기능을 핵심 기능과 결합할 수 있다. 

## Java 패키지 구조
```
com.aspectran
├── core          핵심 기능 패키지
│   ├── activity    요청, 처리, 응답을 처리하는 패키지
│   ├── adapter     핵심 기능과 구현 기능간의 인터페이스 패키지
│   ├── context     핵심 기능 구동에 필요한 환경을 구성하는 패키지
│   ├── service     핵심 기능을 제공하는 패키지
│   └── util        공통 유틸리티 패키지
├── scheduler     핵심 기능을 상속받아서 Scheduler를 구현한 패키지
├── support       외부 라이브러리 지원을 위한 패키지
└── web           핵심 기능이 상속받아서 Web 환경을 구현한 패키지
```