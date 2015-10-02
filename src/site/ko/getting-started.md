# 시작하기

## 1. Installation
Aspectran을 사용하려면 aspectran-x.x.x.jar 파일이 필요합니다.
추가적으로 다음과 같은 의존 라이브러리를 필요로 합니다.
* cglib
* commons-fileupload
* commons-io
* logging 라이브러리(commons-logging, log4j, slf4j)

Maven을 사용한다면 [pom.xml](https://github.com/topframe/aspectran/blob/master/pom.xml) 파일을 참고해서 의존 라이브러리를 추가해 주세요.

- - -

## 2. 작동 환경
Aspectran은 다음 요건만 충족을 하면 원할한 작동이 보장됩니다.
* Java 6 이상
* Servlet 2.5 이상

- - -

## 3. 웹 컨테이너에 서블릿으로 등록하기
`web.xml `파일을 다음과 같이 수정합니다.
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
				root: "/WEB-INF/aspectran/config/getting-started.xml"
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
### 3.1 초기화 파라메터 정의
먼저 컨텍스트 초기화 파라메터 "aspectran:config"를 정의합니다.
"aspectran:config" 파라메터는 ***APON***(Aspectran Parameter Object Notation) 문서형식의 설정 값을 가질 수 있습니다.
> ***APON***(Aspectran Parameter Object Notation)은 ***JSON***과 표기법이 비슷합니다.
> 미리 정해진 형식의 파라메터를 주고 받기 위해서 새롭게 개발된 표기법입니다.

| 파라메터 | 설명 |
|-----------|-------|
| **context** | Aspectran 환경설정을 위한 정의 |
| **context.root** | 환경 설정을 위해 가장 먼저 참조할 xml 파일의 경로  |
| **context.encoding** | XML 파일을 APON 문서형식으로 변환시에 문자열 인코딩 방식을 지정 |
| **context.resources** | Aspectran에서 별도로 관리할 수 있는 리소스의 경로를 배열로 지정 (Aspectran은 계층형의 ClassLoader를 별도로 내장하고 있습니다.) ex) WEB-INF/aspectran/config, WEB-INF/aspectran/classes, WEB-INF/aspectran/lib, file:/c:/Users//Projects/java/classes |
| **context.hybridLoading** | 환경 설정을 빠르게 로딩하기 위해 다수의 XML 파일을 APON 문서형식으로 변환할지 여부를 지정합니다. XML 형식의 환경 설정 파일이 수정되면 APON 파일로 변환되고,  다음 기동 시에 XML 파일을 로딩하는 것이 아니라 APON 파일을 찾아서 로딩합니다. 다수의 XML 파일을 파싱하는 걸리는 시간을 단축할 수 있습니다. |
| **context.autoReloading** | 리소스 자동 갱신 기능에 대한 정의 (Aspectran에서 별도로 관리하는 리소스에 대해서는 WAS를 재시작을 하지 않더라도 자동 갱신이 가능합니다.) |
| **context.autoReloading.reloadMethod** | 리소스의 갱신 방법을 지정 (hard: Java Class 갱신 가능 , soft: 환경 설정 내역만 갱신 가능) |
| **context.autoReloading.observationInterval** | 리소스가 수정 여부를 관찰하는 시간 간격을 초 단위로 지정 |
| **context.autoReloading.startup** | 리소스 자동 갱신 기능을 사용할지 여부를 지정 |
| **scheduler** | 스케쥴러 동작환경을 위한 정의 |
| **scheduler.startDelaySeconds** | 모든 환경이 초기화된 후 스케쥴러가 기동될 수 있도록 시간 간격을 초 단위로 지정 |
| **scheduler.waitOnShutdown** | 실행중인 Job이 종료되기를 기다렸다가 스케쥴러를 종료할지 여부를 지정 |
| **scheduler.startup** | 스케쥴러를 기동할지 여부를 지정 |

### 3.2 AspectranServiceListener 등록
웹 어플리케이션이 시작되면서 Aspectran 서비스도 함께 기동되도록 하기 위해 ***AspectranServiceListener***를 등록합니다.
`<listner-class>`에  "com.aspectran.web.startup.listener.AspectranServiceListener"를 지정합니다.
컨텍스트 초기화 파라메터 "aspectran:config"를 참조해서 Aspectran 서비스 환경이 구성됩니다.

> AspectranServiceListener에 의해 기동된 Aspectran 서비스는 여러 WebActivityServlet에서 사용될 수 있습니다.
> 즉, 전역적인 하나의 Aspectran 서비스 환경을 구성할 수 있습니다.

### 3.3 WebActivityServlet 등록
`<servlet-class>`에 "com.aspectran.web.startup.servlet.WebActivityServlet"을 지정합니다.
`<servlet-name>`에는 Aspectran 서비스를 위한 서블릿이라는 의미의 고유한 서블릿 이름을 부여해 주기 바랍니다.

> 서블릿 초기화 파라메터로 "aspectran:cofnig"를 정의하면 독자적인 Aspectran 서비스 환경을 구성합니다.
> 즉, 전역 Aspectran 서비스를 사용하지 않습니다.

### 3.4 서블릿 URL 패턴 등록
`<url-pattern>`  해당하는 요청은 Aspectran 서비스가 처리할 수 있도록 합니다.
만약 `<url-pattern>` 이 "/example/*"이라면 "/example/"로 시작하는 Aspectran의 Translet이 실행됩니다.

> Aspectran의 Translet이란?
> 요청을 받고 결과 값을 적절히 가공해서 응답하는 처리자를 Aspectran 내부에서는 "Translet"이라고 명명하였습니다.
> Translet은 고유 이름을 가지고 있으며, 요청 URI와 직접적으로 매핑이 됩니다.

### 3.5 DefaultServlet 이름 지정하기
요청 URI에 해당하는 Translet이 존재하지 않을 경우 서블릿 컨테이너의 DefaultServlet에게 넘겨주는 역할을 하는 핸들러가 항상 동작하고 있습니다. 그 핸들러의 이름은 DefaultServletHttpRequestHandler입니다. DefaultServletHttpRequestHandler는 DefaultServlet의 이름이 무엇인지 자동으로 판단합니다. 만약 DefaultServlet의 이름이 다르게 지정되어야 할 경우 아래와 같은 초기화 파라메터를 추가합니다.
```xml
<context-param>
	<param-name>aspectran:defaultServletName</param-name>
	<param-value>default</param-value>
</context-param>
```

- - -

## 4. 환경 설정 파일 작성하기
위 `web.xml` 파일에서 `context.root`를 "/WEB-INF/aspectran/config/getting-started.xml"이라고 지정했었습니다.

###### getting-started.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE aspectran PUBLIC "-//aspectran.com//DTD Aspectran 1.0//EN"
                           "http://aspectran.github.io/dtd/aspectran-1.0.dtd">

<aspectran>

	<!-- 기본 설정 -->
	<settings>
		<setting name="transletNamePattern" value="/example/*"/>
		<setting name="pointcutPatternVerifiable" value="true"/>
	</settings>

	<!-- Aspectran의 Translet이 처리한 결과값을 화면에 표현하기 위해 JSP를 이용합니다. -->
	<bean id="jspViewDispatcher" class="com.aspectran.web.view.JspViewDispatcher" scope="singleton">
		<property>
			<item name="templatePathPrefix">/WEB-INF/jsp/</item>
			<item name="templatePathSuffix">.jsp</item>
		</property>
	</bean>

	<!-- com.aspectran.eaxmple 패키지 하위의 모든 경로에서 클래스 이름이 "Action"으로 끝나는 클래스를 모두 찾아서 Bean으로 등록합니다. -->
	<!-- ex) com.aspectran.example.sample.SampleAction 클래스의 bean id는 "sample.SampleAction"이 됩니다. -->
	<bean id="*" class="com.aspectran.example.**.*Action" scope="singleton">
		<filter>
			exclude: [
				"com.aspectran.example.common.**.*"
				"com.aspectran.example.sample.**.*"
			]
		</filter>
	</bean>

	<!-- com.aspectran.eaxmple 패키지 하위의 모든 경로에서 클래스 이름이 "Advice"으로 끝나는 클래스를 모두 찾아서 ID가 "advice."으로 시작하는 Bean으로 등록합니다. -->
	<!-- ex) com.aspectran.example.sample.SampleAdvice 클래스의 bean id는 "advice.sample.SampleAdvice"이 됩니다. -->
	<bean id="advice.*" class="com.aspectran.example.**.*Advice" scope="singleton"/>

	<bean id="sampleBean" class="com.aspectran.example.sample.SampleBean" scope="singleton"/>

	<!-- 요청 정보를 분석하는 단계에서 사용할 기본 환경 변수를 정의합니다. -->
	<aspect id="defaultRequestRule">
		<joinpoint scope="request"/>
		<settings>
			<setting name="characterEncoding" value="utf-8"/>
			<setting name="multipart.maxRequestSize" value="10M"/>
			<setting name="multipart.temporaryFilePath" value="/d:/"/>
		</settings>
	</aspect>

	<!-- 요청에 대해 응답하는 단계에서 사용할 기본 환경 변수를 정의합니다. -->
	<aspect id="defaultResponseRule">
		<joinpoint scope="response"/>
		<settings>
			<setting name="characterEncoding" value="utf-8"/>
			<setting name="defaultContentType" value="text/html"/>
			<setting name="viewDispatcher" value="jspViewDispatcher"/>
		</settings>
	</aspect>

	<!-- Translet의 이름이 "/example"로 시작하는 Translet을 실행하는 중에 발생하는 에러 처리 규칙을 정의합니다.  -->
	<!-- 에러요인과 응답 컨텐츠의 형식에 따라 처리방식을 다르게 정할 수 있습니다. -->
	<aspect id="defaultExceptionHandlingRule">
		<joinpoint scope="translet">
			<pointcut>
				target: {
					translet: "/example/*"
				}
			</pointcut>
		</joinpoint>
		<exceptionRaised>
			<responseByContentType exceptionType="java.lang.reflect.InvocationTargetException">
				<transform type="transform/xml" contentType="text/xml">
					<echo id="result">
						<item type="map">
							<value name="errorCode">E0001</value>
							<value name="message">error occured.</value>
						</item>
					</echo>
				</transform>
				<transform type="transform/json" contentType="application/json">
					<echo id="result">
						<item type="map">
							<value name="errorCode">E0001</value>
							<value name="message">error occured.</value>
						</item>
					</echo>
				</transform>
			</responseByContentType>
		</exceptionRaised>
	</aspect>

	<!-- "helloworld.HelloWorld.Action" 빈에서 echo, helloWorld, counting 메쏘드 호출 전 후로 환영인사와 작별인사를 건넵니다. -->
	<aspect id="helloWorldAdvice">
		<joinpoint scope="translet">
			<pointcut>
				target: {
					+: "/example/**/*@helloworld.HelloWorldAction^echo|helloWorld|counting"
				}
			</pointcut>
		</joinpoint>
		<advice bean="advice.helloworld.HelloWorldAdvice">
			<before>
				<action method="wellcome"/>
			</before>
			<after>
				<action method="goodbye"/>
			</after>
		</advice>
	</aspect>

	<!-- "/example/counting" Translet이 호출되면 요청정보를 분석을 완료한 시점에 실행되는 지정된 Adivce가 실행됩니다. -->
	<!-- 카운팅할 숫자의 범위에 대한 유효성을 검사합니다. -->
	<aspect id="checkCountRangeAdvice">
		<joinpoint scope="request">
			<pointcut>
				target: {
					+: "/example/counting"
				}
			</pointcut>
		</joinpoint>
		<advice bean="advice.helloworld.HelloWorldAdvice">
			<after>
				<action method="checkCountRange"/>
			</after>
		</advice>
	</aspect>

	<!-- "Hello, World."라는 문구를 텍스트 형식의 컨텐츠로 응답합니다.  -->
	<translet name="echo/${name}/${age}">
		<transform type="transform/text" contentType="text/plain">
			<template>
				Hello, World.
			</template>
		</transform>
	</translet>

	<!-- helloworld.HelloWorldAction 빈에서 helloWorld 메쏘드를 실행해서 "Hello, World."라는 문구를 텍스트 형식의 컨텐츠로 응답합니다. -->
	<translet name="helloWorld">
		<transform type="transform/text" contentType="text/plain">
			<action bean="helloworld.HelloWorldAction" method="helloWorld"/>
		</transform>
	</translet>

	<!-- 숫자를 세는 Translet입니다. 시작 값과 마지막 값을 파라메터로 받습니다. -->
	<!-- 숫자의 범위가 유효한지를 검사합니다. -->
	<translet name="counting">
		<request>
			<attribute>
				<item name="from"/>
				<item name="to"/>
			</attribute>
		</request>
		<content>
			<action id="count1" bean="helloworld.HelloWorldAction" method="counting">
				<argument>
					<item valueType="int">@{from}</item>
					<item valueType="int">@{to}</item>
				</argument>
			</action>
		</content>
		<response>
			<transform type="transform/xml"/>
		</response>
	</translet>

	<!-- 스케쥴러 환경설정을 불러들입니다. -->
	<import file="/WEB-INF/aspectran/config/example-scheduler.xml"/>

</aspectran>
```

### 4.1 환경 설정 상수
Aspectran의 기본 설정 항목에 대해 설명합니다.

| 설정 항목명 | 설명 | 사용가능한 값 | 기본 값 |
|---|---|---|---|
| **transletNamePattern** | Translet 이름의 패턴. Translet 이름 문자열은 `<servlet-mapping>` 의 `<url-pattern>`의 값으로 시작해야 접근이 가능합니다.  | ex) /example/*.do | 설정하지 않음 |
| **transletNamePrefix** | `transletNamePattern` 대신 prefix와 suffix를 지정할 수 있습니다. | ex) /example/ | 설정하지 않음 |
| **transletNameSuffix** | `transletNamePattern` 대신 prefix와 suffix를 지정할 수 있습니다. | ex) .do | 설정하지 않음 |
| **transletInterfaceClass** | 사용자 정의 Translet의 인터페이스 클래스를 지정합니다. | ex) com.aspectran.example.common.MyTranslet | 설정하지 않으면 내장 Translet을 사용 |
| **transletImplementClass** | 사용자 정의 Translet의 구현 클래스를 지정합니다. | ex) com.aspectran.example.common.MyTransletImpl | 설정하지 않으면 내장 Translet을 사용 |
| **nullableContentId** | `<content>`의 id 속성을 생략할 수 있는지 여부를 지정합니다. | true or false | true |
| **nullableActionId** | `<action>`의 id 속성을 생략할 수 있는지 여부를 지정합니다. | true or false | true |
| **beanProxifier** | 자바 바이트코드 생성기(Byte Code Instumentation, BCI) 라이브러리를 지정합니다. | javassist or cglib or jdk | javassist |
| **pointcutPatternVerifiable** | pointcut 패턴의 유효성을 체크할지 여부를 지정합니다. | true or false | true |

위 설정항목을 대부분 사용한 `settings` 엘리먼트의 예제입니다.
```xml
<settings>
		<setting name="transletNamePattern" value="/example/*"/>
		<setting name="transletInterfaceClass" value="com.aspectran.example.common.MyTranslet"/>
		<setting name="transletImplementClass" value="com.aspectran.example.common.MyTransletImpl"/>
		<setting name="nullableContentId" value="true"/>
		<setting name="nullableActionId" value="true"/>
		<setting name="beanProxifier" value="javassist"/>
		<setting name="pointcutPatternVerifiable" value="true"/>
</settings>
```

### 4.2 Bean 정의
Bean을 정의하는 방법은 두 가지가 있습니다. 와일드카드를 사용해서 Bean 클래스를 일괄 스캔해서 자동으로 Bean을 등록할 수 있습니다.

#### Bean을 한 개씩 정의하는 방법
```xml
<!-- Aspectran의 Translet이 처리한 결과값을 화면에 표현하기 위해 JSP를 이용합니다. -->
<bean id="jspViewDispatcher" class="com.aspectran.web.view.JspViewDispatcher" scope="singleton">
	<property>
		<item name="templatePathPrefix">/WEB-INF/jsp/</item>
		<item name="templatePathSuffix">.jsp</item>
	</property>
</bean>
```
```xml
<bean id="sampleBean" class="com.aspectran.example.sample.SampleBean" scope="singleton"/>
```

#### 클래스패스에 존재하는 Bean을 일괄 스캔해서 정의하는 방법

com.aspectran.eaxmple 패키지 하위의 모든 경로에서 클래스 이름이 "Action"으로 끝나는 클래스를 모두 찾아서 Bean으로 등록합니다.
> ex) com.aspectran.example.sample.SampleAction 클래스의 Bean ID는 "sample.SampleAction"이 됩니다.

```xml
<beans id="*" class="com.aspectran.example.**.*Action" scope="singleton"/>
```

com.aspectran.eaxmple 패키지 하위의 모든 경로에서 클래스 이름이 "Advice"으로 끝나는 클래스를 모두 찾아서 ID가 "advice."으로 시작하는 Bean으로 등록합니다. 제외할 클래스를 지정할 수 있습니다.
> ex) com.aspectran.example.sample.SampleAdvice 클래스의 Bean ID는 "advice.sample.SampleAdvice"이 됩니다.

```xml
<bean id="advice.*" class="com.aspectran.example.**.*Advice" scope="singleton">
	<filter>
		exclude: [
			"com.aspectran.example.common.**.*"
			"com.aspectran.example.sample.**.*"
		]
	</filter>
</bean>
```

사용자 정의 필터 클래스를 지정할 수 있습니다.
`com.aspectran.core.context.bean.scan.ClassScanFilter` 인터페이스를 구현해야 합니다.

```xml
<bean id="advice.*" class="com.aspectran.example.**.*Advice" scope="singleton">
	<filter class="com.aspectran.example.common.UserClassScanFilter"/>
</bean>
```

#### 와일드카드를 사용해서 여러 클래스를 지정하기
class 속성 값에 사용할 수 있는 와일드카드 문자들은  `*, ?, +` 이고, Escape 문자로 `\` 문자를 사용할 수 있습니다.
여러 패키지를 포함할 경우 `.**.` 문자를 중간에 사용하면 되는데, 예를들어 `com.**.service.*.*Action`과 같이 사용할 수 있습니다.

#### Bean ID 부여 규칙
검색된 여러 개의 클래스에 대하여 Bean ID를 부여하는 규칙은 다음과 같습니다.
* 와일드카드 문자가 시작되는 지점부터 끝까지를 Bean ID로 인식합니다.
예를들어 `com.aspectran.example.**.*Action`에 해당하는 클래스가 `com.aspectran.example.hellloworld.HelloWorldAction`이면 Bean의 ID는 `helloworld.HelloWorldAction`으로 인식합니다..
* id 속성에 `*` 구분자를 포함할 경우 `*` 구분자를 기준으로 앞에 있는 있는 문자열과 뒤에 있는 문자열을 인식한 Bean ID의 앞뒤로 연결합니다. 예를들어 id가 `advice.*`이면 조합된 Bean ID는 `advice.helloworld.HelloWorldAdvice`가 됩니다.

#### Bean을 다음과 같이 상세하게 정의할 수도 있습니다.
```xml
<bean id="sampleBean">
	<features>
		<class>com.aspectran.sample.SampleAction</class>
		<scope>singleton</scope>
		<initMethod>initialize</initMethod>
		<destroyMethod>destory</destroyMethod>
		<lazyInit>true</lazyInit>
	</features>
	<constructor>
		<arguments>
			<item>arg1</item>
			<item type="list" valueType="int">
				<value>1</value>
				<value>2</value>
				<value>3</value>
			</item>
		</arguments>
	</constructor>
	<properties>
		<item name="name">david</item>
		<item name="grade" type="list">
			<value>A</value>
			<value>B</value>
		</item>
		<item name="amount" type="map">
			<value name="food" valueType="float">123456</value>
			<value name="transportation expenses" valueType="1234">value</value>
		</item>
		<item name="anotherBean">
			<reference bean="anotherBean"/>
		</item>
	</properties>
</bean>
```

### 4.3 Aspect 정의
Aspectran이 지원하는 AOP(Aspect Oriented Programming)는 다른 프레임웤에 비해 사용법이 쉽습니다.
Aspectran의 AOP는 Translet, Bean 영역 내에서의 메쏘드 호출 조인포인트(Joingpoint)를 지원합니다.

Aspect는 다음 용도로 사용될 수 있습니다.
* 핵심 비지니스 로직과 공통적인 부가 비지니스 로직을 분리해서 코드를 작성할 수 있습니다.
  > ex) 로깅, 인증, 권한, 성능 테스트
* 트랜잭션 처리
  > 주로 데이터베이스 트랜잭션 기능을 지원하기 사용합니다.

#### Aspect를 이용해서 환경변수 선언하기
Aspectran은 외부의 접속 요청을 Translet이 받아서 처리합니다. Translet의 내부에는 Request, Contents, Response 라는 세 가지 영역이 있습니다. Translet과 Request, Contents, Response 영역에서 참조할 수 있는 공통 환경변수를 선언할 수 있습니다.
> Translet 내부의 세 가지 영역
> * Request: 요청 정보를 분석하는 영역
> * Contents: 액션을 실행하고 결과 값을 생산하는 영역
> * Response: 생산된 결과 값을 출력하는 영역

```xml
<!-- 요청 정보를 분석하는 단계에서 사용할 기본 환경 변수를 정의합니다. -->
<aspect id="defaultRequestRule">
	<joinpoint scope="request"/>
	<settings>
		<setting name="characterEncoding" value="utf-8"/>
		<setting name="multipart.maxRequestSize" value="10M"/>
		<setting name="multipart.temporaryFilePath" value="/d:/"/>
	</settings>
</aspect>

<!-- 요청에 대해 응답하는 단계에서 사용할 기본 환경 변수를 정의합니다. -->
<aspect id="defaultResponseRule">
	<joinpoint scope="response"/>
	<settings>
		<setting name="characterEncoding" value="utf-8"/>
		<setting name="defaultContentType" value="text/html"/>
		<setting name="viewDispatcher" value="jspViewDispatcher"/>
	</settings>
</aspect>
```

