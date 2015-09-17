# 시작하기

## Installation
Aspectran을 사용하려면 aspectran-x.x.x.jar 파일이 필요합니다.
추가적으로 다음과 같은 의존 라이브러리를 필요로 합니다.
* cglib
* commons-fileupload
* commons-io
* logging 라이브러리(commons-logging, log4j, slf4j)

Maven을 사용한다면 [pom.xml](https://github.com/topframe/aspectran/blob/master/pom.xml) 파일을 참고해서 의존 라이브러리를 추가해 주세요.

## 작동 환경
Aspectran은 다음 요건만 충족을 하면 원할한 작동이 보장됩니다.
* Java 6 이상
* Servlet 2.5 이상

## 웹 컨테이너에 서블릿으로 등록하기
web.xml 파일을 다음과 같이 수정합니다.
```xml
<?xml version="1.0" encoding="utf-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
  <display-name>aspectran-examples</display-name>
  <welcome-file-list>
    <welcome-file>index.html</welcome-file>
    <welcome-file>index.htm</welcome-file>
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
  <context-param>
    <param-name>aspectran:config</param-name>
    <param-value>
			context: {
				root: "/WEB-INF/aspectran/config/example-root.xml"
				encoding: "utf-8"
				resources: [
					"/WEB-INF/aspectran/config"
					"/WEB-INF/aspectran/classes"
					"/WEB-INF/aspectran/lib"
				]
				hybridLoading: true
				autoReloading: {
					reloadMethod: hard
					observationInterval: 5
					startup: true
				}
			}
			scheduler: {
				startDelaySeconds: 10
				waitOnShutdown: true
				startup: false
			}
		</param-value>
  </context-param>
  <listener>
    <listener-class>com.aspectran.web.startup.listener.AspectranServiceListener</listener-class>
  </listener>
  <servlet>
    <servlet-name>aspectran-example</servlet-name>
    <servlet-class>com.aspectran.web.startup.servlet.WebActivityServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>aspectran-example</servlet-name>
    <url-pattern>/example/*</url-pattern>
  </servlet-mapping>
  <!-- 실제 운영환경에서는 스케쥴러에 직접 접근할 수 없도록 서블릿매핑을 제거하도록 합니다. -->
  <servlet-mapping>
    <servlet-name>aspectran-example</servlet-name>
    <url-pattern>/scheduler/*</url-pattern>
  </servlet-mapping>
</web-app>
```
#### 기본 환경 설정
먼저 컨텍스트 초기화 파라메터 "aspectran:config"를 정의합니다.
"aspectran:config" 파라메터는 ***APON***(Aspectran Parameter Object Notation) 문서형식의 설정 값을 가질 수 있습니다.
> ***APON***(Aspectran Parameter Object Notation)은 ***JSON***과 표기법이 비슷합니다.
> 미리 정해진 형식의 파라메터를 주고 받기 위해서 새롭게 개발된 표기법입니다.

| 파라메터 | 설명 |
|-----------|-------|
| context | Aspectran 기본 환경설정을 위한 정의 |
| context.root | 기본 환경 설정을 위해 가장 먼저 참조할 xml 파일의 경로  |
| context.encoding | xml 파일을 APON 문서형식으로 변환할때 사용되는 문자열 인코딩 방식을지정 |
| context.resources | Aspectran에서 별도로 관리할 수 있는 리소스 경로를 배열로 지정 |
| context.hybridLoading | 기본 설정을 빠르게 로딩하기 위해 다수의 xml 파일을 APON 문서형식으로 변환할지 여부를 지정 |
| context.autoReloading | Aspectran에서 별도로 관리하는 리소스의 자동 갱신 기능에 대한 정의 |
| context.autoReloading.reloadMethod | 리소스의 갱신 방법을 지정(hard, soft) |
| context.autoReloading.observationInterval | 리소스가 수정되었는지 관찰하는 시간 간격을 초 단위로 지정 |
| context.autoReloading.startup | 리소스 자동 갱신 기능을 사용할지 여부를 지정 |
| scheduler | 스케쥴러 동작환경을 위한 정의 |
| scheduler.startDelaySeconds | 모든 환경이 초기화된 후 스케쥴러가 기동될 수 있도록 시간 간격을 초 단위로 지정 |
| scheduler.waitOnShutdown | 실행중인 Job이 종료되기를 기다렸다가 스케쥴러를 종료할지 여부 |
| scheduler.startup | 스케쥴러를 기동할지 여부 |

### AspectranServiceListener 등록
웹 애플리케이션이 시작되면서 Aspectran 서비스도 함께 기동되도록 하기 위해 ***AspectranServiceListener***를 등록합니다.
`<listner-class>`에  "com.aspectran.web.startup.listener.AspectranServiceListener"를 지정합니다.
컨텍스트 초기화 파라메터 "aspectran:config"를 참조해서 Aspectran 서비스 환경이 구성됩니다.

> AspectranServiceListener에 의해 기동된 Aspectran 서비스는 여러 WebActivityServlet에서 사용될 수 있습니다.
> 즉, 전역적인 하나의 Aspectran 서비스 환경을 구성할 수 있습니다.

### WebActivityServlet 등록
`<servlet-class>`에 "com.aspectran.web.startup.servlet.WebActivityServlet"을 지정합니다.
`<servlet-name>`에는 Aspectran 서비스를 위한 서블릿이라는 의미의 고유한 서블릿 이름을 부여해 주기 바랍니다.

> 서블릿 초기화 파라메터로 "aspectran:cofnig"를 정의하면 독자적인 Aspectran 서비스 환경을 구성합니다.
> 즉, 전역 Aspectran 서비스를 사용하지 않습니다.

### 서블릿 URL 패턴 등록
`<url-pattern>`  해당하는 요청은 Aspectran 서비스가 처리할 수 있도록 합니다.
만약 `<url-pattern>` 이 "/example/*"이라면 "/example/"로 시작하는 Aspectran의 Translet이 실행됩니다.

> Aspectran의 Translet이란?
> 요청을 받고 결과 값을 적절히 가공해서 응답하는 처리자를 Aspectran 내부에서는 "Translet"이라고 명명하였습니다.
> Translet은 고유 이름을 가지고 있으며, 요청 URI와 직접적으로 매핑이 됩니다.
