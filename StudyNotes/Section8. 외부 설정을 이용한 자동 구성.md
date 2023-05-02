# Environment 추상화와 프로퍼티

![스크린샷 2023-05-03 오전 7.15.57.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/4b8e3cdf-f4d9-48cc-b058-9a4ed0981665/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-05-03_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_7.15.57.png)

외부 설정이 자동 구성 중 어느 타이밍에 어떤 방식으로 일어나는가?

스캔 시점에 스프링 컨테이너에 빈이 등록되어있지 않다면 미리 준비된 컨피그 빈 클래스가 등록된다. 이때 외부 설정 정보를 이용해 생성된 빈 오브젝트의 프로퍼티 값을 수정하는 작업이 일어난다. 톰캣 포트를 9090번으로 수정하는 것과 같이, **기술적인 오브젝트에서 프로퍼티를 설정**하는 작업을 할 수 있다. 이 작업은 유저 구성정보로 빈을 등록해도 되지만, 모든 빈을 자바 코드로 대체해서 등록하는 것은 번거롭다. 그래서 스프링 부트는 애플리케이션에 대한 동작을 추상화하는 Environment로부터 프로퍼티를 읽어와 자동구성으로 등록되는 빈의 프로퍼티를 변경해준다.

## Environment 추상화란?

Application Property를 세팅하는 방법은 매우 다양하다. 커맨드 라인 args, 환경변수, 클라우드 컨피그 서비스 등이 있다. 스프링은 자바 애플리케이션이 프로퍼티를 얻어오는 방법을 추상화해서 일관된 방법으로 프로퍼티를 얻을 수 있다. 스프링이 제공하는 서비스 추상화 기능이다.

## Environment 추상화의 종류

- StandardServletEnvironment : 웹 환경일 때 사용하는 프로퍼티 → System Properties, System Environment Variables
- StandardEnvironment : 자바가 다루는 프로퍼티, OS 환경 변수의 프로퍼티 → ServletConfig Parameters, ServletContext Prameters, JNDI
- SpringBoot : @PropertySource, [application.properties](http://application.properties) 프로퍼티
- 각 프로퍼티 리소스에는 우선선위가 있다.

# 자동 구성에 Environment 프로퍼티 적용

스프링 컨테이너가 초기화되면 어떤 기능이 자동으로 수행되는 방법을 찾아보자. 이때 스프링 컨테이너에 만들어진 빈 오브젝트를 사용하고 테스트할 수 있어야 한다. 이런 작업은 초기화 작업이나 컨테이너 기능을 확인할 때 활용할 수 있다.

## 일반적인 프로퍼티 사용법

스프링 부트는 ApplicationRunner 타입의 인터페이스를 구현해서 빈으로 등록하면, 스프링 컨테이너 초기화를 마친 후 해당 빈의 run 메소드를 실행한다. 이때 애플리케이션을 실행할 때 받은 변수값도 매개변수로 주입 받는다.

```java
@MySpringBootApplication
public class HellobootApplication {

	@Bean
	ApplicationRunner applicationRunner(Environment env) {
		return args -> {
			String name = env.getProperty("my.name");
			System.out.println("my.name: "+name);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(HellobootApplication.class, args);
	}

}
```

스프링 부트가 자동으로 추가해준 application.properties에 프로퍼티를 넣어보자.

```java
// application.properties
my.name=ApplicationProperties
```

이번엔 우선선위가 더 높은 환경변수에 프로퍼티를 설정해보자. 환경변수는 OS 설정이나 컨테이너 환경변수로 설정할 수 있다. 인텔리제이에서 구동 환경의 환경변수를 넣을 수 있다. 대문자와 언더스코어를 사용한다.

![스크린샷 2023-05-03 오전 7.53.22.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5503a2d8-455f-4297-97d9-6cc35bd0660e/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-05-03_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_7.53.22.png)

환경변수보다 우선순위가 높은 시스템 프로퍼티를 선언해보자. 우리가 선언적으로 시스템 프로퍼티를 설정할 수 있는 것은 자바 명령어로 프로그램을 실행할 때 -D 옵션으로 프로퍼티와 밸류 값을 주는 것이다. 인텔리제이 VM Option으로 추가할 수 있다. 소문자와 점을 사용한다.

![스크린샷 2023-05-03 오전 7.56.15.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/f2ec1c7e-70c3-486a-88a6-cb82a36c79bc/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-05-03_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_7.56.15.png)

일반적으로 웹 애플리케이션을 만들 때는 application.properties에 설정 정보를 작성하고, 필요한 경우 환경 변수로 설정을 오버라이딩해서 사용한다.

## 프로퍼티를 스프링 부트의 자동 구성에 적용하기

자동구성으로 등록되는 빈을 자바 코드로 등록한다면, 해당 객체가 제공하는 세터로 프로터피를 설정할 수 있다. 또한 빈 메소드이므로 env을 메소드 주입으로 받아 외부 파일에 있는 프로퍼티를 설정한다.

톰캣 내장 서버 빈의 컨텍스트 Path를 application.properties에 등록해 사용해본다.

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
public class TomcatWebServerConfig {

    @Bean("tomcatWebServerFactory")
    @ConditionalOnMissingBean // ServletWebServerFactory 타입 빈이 없으면 이 빈을 등록
    public ServletWebServerFactory servletWebServerFactory(Environment env) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setContextPath(env.getProperty("contextPath")); // 모든 서블릿의 매핑 앞에 /app가 붙음
        return factory;
    }

}

// application.properties
contextPath=/app
```