# 스타터와 제티 서버 추가

## 스프링 부트의 AutoConfig

- 애플리케이션 서버가 뜰 때 SpringApplication.run에 넘겨준 클래스부터 시작하여 빈 스캔을 시작한다.
- 해당 클래스의 `@SpringBookApplication` 애노테이션에는 애플리케이션 빈 스캔을 위한 `@ComponentScan`과 인프라 스트럭처 빈 스캔을 위한 메타 애노테이션인 `@EnableAutoConfiguration`이 있다.
- 이 애노테이션에서 import로 Selector 구현 클래스를 사용해 외부 파일`org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 문자열로 등록해놓은 기능별 컨피그 클래스를 빈 후보로 읽어오고, 빈 메소드를 실행해 빈으로 등록한다.

## 스프링 부트의 opinionated 설정

![스크린샷 2023-04-30 오후 9.07.36.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/1b827366-31d9-4e5e-9f70-be77d2580bb3/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-04-30_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_9.07.36.png)

- 스프링 부트의 빈 후보 파일을 열어보면 약 144개의 후보 클래스의 풀 패키지 경로를 확인할 수 있다.
- 스프링 부트 2.7.12 버전에서 함께 사용했을 때 문제 없는 빈들의 버전을 맞춰서 제공하는 것을 알 수 있다.
- 여기서 제공하는 빈 클래스는 Spring Boot 이니셜라이저로 초기화할 때 포함한 모듈의 라이브러리로 들어온다. 우리가 인프라 스트럭처 빈을 기능별로 잘게 쪼개고 config로 등록한 것과 동일한 원리이다.

## 조건부 자동구성

빈 후보를 읽고 실제로 실행할 빈 메소드를 조건에 따라 결정한다. 기본 모듈을 포함했을 때 스프링 부트가 제공하는 빈 후보는 144개가 넘는데, 실제로 프로젝트에서 사용하지 않는 빈을 포함하는 것은 비효율적이다. 그래서 빈 후보를 읽고 실제 빈을 등록할 때 로직이 껴서 빈을 등록할지 말지 결정할 수 있다.

## 라이브러리

gradle을 통해 스프링 부트 이니셜라이저가 추가해준 `org.springframework.boot:spring-boot-starter-web` 을 살펴보자. 이 모듈에는 스프링 부트가 자동구성으로 등록할 빈의 클래스가 포함된다. 로딩, aop, 톰캣, 스프링 부트 스타터 등 다양한 빈 클래스가 있는 것을 확인할 수 있다.

```
**./gradlew dependencies --configuration compileClassPath**

Welcome to Gradle 7.6.1!

Here are the highlights of this release:
 - Added support for Java 19.
 - Introduced `--rerun` flag for individual task rerun.
 - Improved dependency block for test suites to be strongly typed.
 - Added a pluggable system for Java toolchains provisioning.

For more details see https://docs.gradle.org/7.6.1/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)

> Task :dependencies

------------------------------------------------------------
Root project 'helloboot'
------------------------------------------------------------

compileClasspath - Compile classpath for source set 'main'.
\--- org.springframework.boot:spring-boot-starter-web -> 2.7.12-SNAPSHOT
     +--- org.springframework.boot:spring-boot-starter:2.7.12-SNAPSHOT
     |    +--- org.springframework.boot:spring-boot:2.7.12-SNAPSHOT
     |    |    +--- org.springframework:spring-core:5.3.27
     |    |    |    \--- org.springframework:spring-jcl:5.3.27
     |    |    \--- org.springframework:spring-context:5.3.27
     |    |         +--- org.springframework:spring-aop:5.3.27
     |    |         |    +--- org.springframework:spring-beans:5.3.27
     |    |         |    |    \--- org.springframework:spring-core:5.3.27 (*)
     |    |         |    \--- org.springframework:spring-core:5.3.27 (*)
     |    |         +--- org.springframework:spring-beans:5.3.27 (*)
     |    |         +--- org.springframework:spring-core:5.3.27 (*)
     |    |         \--- org.springframework:spring-expression:5.3.27
     |    |              \--- org.springframework:spring-core:5.3.27 (*)
     |    +--- org.springframework.boot:spring-boot-autoconfigure:2.7.12-SNAPSHOT
     |    |    \--- org.springframework.boot:spring-boot:2.7.12-SNAPSHOT (*)
     |    +--- org.springframework.boot:spring-boot-starter-logging:2.7.12-SNAPSHOT
     |    |    +--- ch.qos.logback:logback-classic:1.2.12
     |    |    |    +--- ch.qos.logback:logback-core:1.2.12
     |    |    |    \--- org.slf4j:slf4j-api:1.7.32 -> 1.7.36
     |    |    +--- org.apache.logging.log4j:log4j-to-slf4j:2.17.2
     |    |    |    +--- org.slf4j:slf4j-api:1.7.35 -> 1.7.36
     |    |    |    \--- org.apache.logging.log4j:log4j-api:2.17.2
     |    |    \--- org.slf4j:jul-to-slf4j:1.7.36
     |    |         \--- org.slf4j:slf4j-api:1.7.36
     |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
     |    +--- org.springframework:spring-core:5.3.27 (*)
     |    \--- org.yaml:snakeyaml:1.30
     +--- org.springframework.boot:spring-boot-starter-json:2.7.12-SNAPSHOT
     |    +--- org.springframework.boot:spring-boot-starter:2.7.12-SNAPSHOT (*)
     |    +--- org.springframework:spring-web:5.3.27
     |    |    +--- org.springframework:spring-beans:5.3.27 (*)
     |    |    \--- org.springframework:spring-core:5.3.27 (*)
     |    +--- com.fasterxml.jackson.core:jackson-databind:2.13.5
     |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.13.5
     |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.13.5
     |    |    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.13.5 (c)
     |    |    |         +--- com.fasterxml.jackson.core:jackson-core:2.13.5 (c)
     |    |    |         +--- com.fasterxml.jackson.core:jackson-databind:2.13.5 (c)
     |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.5 (c)
     |    |    |         +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.5 (c)
     |    |    |         \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.5 (c)
     |    |    +--- com.fasterxml.jackson.core:jackson-core:2.13.5
     |    |    |    \--- com.fasterxml.jackson:jackson-bom:2.13.5 (*)
     |    |    \--- com.fasterxml.jackson:jackson-bom:2.13.5 (*)
     |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.5
     |    |    +--- com.fasterxml.jackson.core:jackson-core:2.13.5 (*)
     |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.13.5 (*)
     |    |    \--- com.fasterxml.jackson:jackson-bom:2.13.5 (*)
     |    +--- com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.5
     |    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.13.5 (*)
     |    |    +--- com.fasterxml.jackson.core:jackson-core:2.13.5 (*)
     |    |    +--- com.fasterxml.jackson.core:jackson-databind:2.13.5 (*)
     |    |    \--- com.fasterxml.jackson:jackson-bom:2.13.5 (*)
     |    \--- com.fasterxml.jackson.module:jackson-module-parameter-names:2.13.5
     |         +--- com.fasterxml.jackson.core:jackson-core:2.13.5 (*)
     |         +--- com.fasterxml.jackson.core:jackson-databind:2.13.5 (*)
     |         \--- com.fasterxml.jackson:jackson-bom:2.13.5 (*)
     +--- org.springframework.boot:spring-boot-starter-tomcat:2.7.12-SNAPSHOT
     |    +--- jakarta.annotation:jakarta.annotation-api:1.3.5
     |    +--- org.apache.tomcat.embed:tomcat-embed-core:9.0.74
     |    +--- org.apache.tomcat.embed:tomcat-embed-el:9.0.74
     |    \--- org.apache.tomcat.embed:tomcat-embed-websocket:9.0.74
     |         \--- org.apache.tomcat.embed:tomcat-embed-core:9.0.74
     +--- org.springframework:spring-web:5.3.27 (*)
     \--- org.springframework:spring-webmvc:5.3.27
          +--- org.springframework:spring-aop:5.3.27 (*)
          +--- org.springframework:spring-beans:5.3.27 (*)
          +--- org.springframework:spring-context:5.3.27 (*)
          +--- org.springframework:spring-core:5.3.27 (*)
          +--- org.springframework:spring-expression:5.3.27 (*)
          \--- org.springframework:spring-web:5.3.27 (*)

(c) - dependency constraint
(*) - dependencies omitted (listed previously)
```

## 제티 서버 추가하기

톰캣처럼 제티도 내장서버를 포함할 수 있다. 다만 제티 내장서버 클래스는 제티 라이브러리에 포함되어있으므로 Gradle로 임포트하고, @Bean 붙여 팩토리 메소드로 빈으로 등록해야한다.

우선 다음과 같이 Gradle로 제티 내장 클래스가 포함된 라이브러리를 임포트한다.

```
implementation 'org.springframework.boot:spring-boot-starter-jetty'
```

제티 내장 서버를 등록하는 인프라 스트럭처 빈을 팩토리 메소드로 등록한다.

```java
@MyAutoConfiguration
public class JettyWebServerConfig {

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new JettyServletWebServerFactory();
    }

}
```

이때 톰캣 내장 서버 빈과 타입이 동일하므로, 아이디를 다르게 줘서 후보가 충돌되지 않도록 변경한다. 참고로 팩토리 메소드로 등록한 빈은 메소드 이름을 따른다.

```java
@MyAutoConfiguration
public class JettyWebServerConfig {

    @Bean("jettyWebServerFactory")
    public ServletWebServerFactory servletWebServerFactory() {
        return new JettyServletWebServerFactory();
    }

}

@MyAutoConfiguration
public class TomcatWebServerConfig {

    @Bean("tomcatWebServerFactory")
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

}
```

# @Conditional과 Condition

@Conditional(Condition 구현 클래스)를 사용하여 빈의 등록 조건을 지정할 수 있다. Condition의 matches가 반환하는 값이 true라면 팩토리 메소드를 실행해 빈을 생성해 등록하고, false라면 실행하지 않는다.

만약 조건에 따라 빈을 등록했는데 스프링 컨테이너에 같은 아이디로 등록될 수 있는 빈 후보가 여러개라면 충돌 나서 서버가 뜨지 못한다.

## 톰캣과 제티 서버 예제

스프링 컨테이너는 빈 스캔 과정에서 ServletWebServerFactory 타입으로 등록될 수 있는 빈 후보가 두개임을 확인하고, @Conditional 조건을 확인하여 true인 클래스를 선택하여 모든 팩토리 빈 메소드를 실행한다.

```java
@MyAutoConfiguration
@Conditional(JettyWebServerConfig.JettyCondition.class)
public class JettyWebServerConfig {

    @Bean("jettyWebServerFactory")
    public ServletWebServerFactory servletWebServerFactory() {
        return new JettyServletWebServerFactory();
    }

    static class JettyCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return true;
        }
    }

}

@MyAutoConfiguration
@Conditional(TomcatWebServerConfig.TomcatCondition.class)
public class TomcatWebServerConfig {

    @Bean("tomcatWebServerFactory")
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    static class TomcatCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return false;
        }
    }
}
```

## @Conditional Target

@Conditional 애노테이션은 클래스 레벨과 빈 메소드 레벨에 붙을 수 있다. 클래스 레벨 조건이 참이면 전부 빈 메소드에 등록하는데, 이때 빈 메소드 조건이 거짓이면 실행하지 않는다. 클래스 레벨 조건 거짓이면 내부 빈 메소드 들여다보지도 않으므로 빈이 등록되지 않는다.

# 학습 테스트

학습 테스트를 통해 Conditional 조건을 확장하는 연습을 해볼 수 있다.

## True, False Conditional 구현

우선 Conditional을 적용할 수 있는 빈을 만들고, ApplicationContextRunner을 이용해 테스트해보자.

Config1은 빈으로 등록할 후보이고, Config2는 빈으로 등록하지 않을 후보이다. 각각 true와 false를 리턴하는 matches를 구현한 TrueCondition, FalseCondition을 만들었고 이 클래스를 사용하는 @Conditional을 메타 애노테이션으로 갖는 @TrueConditional과 @FalseConditional을 만들어 Config1, Config2에 지정했다.

ApplicationContextRunner는 테스트에서 간단하게 사용할 수 있는 애플리케이션 컨텍스트이다. 빈이 등록됐는지 간단하게 예외 없이 확인할 수 있다는 장점이 있다.

```java
public class ConditionalTest {

    @Test
    public void conditional() {
        // 빈을 등록할 애플리케이션 컨텍스트
        // ApplicationContextRunner 테스트용 애.컨 - 빈이 등록됐는지 간단하게 예외 없이 확인 가능

        // true
        ApplicationContextRunner contextRunner = new ApplicationContextRunner();
        contextRunner.withUserConfiguration(Config1.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(MyBean.class);
                    assertThat(context).hasSingleBean(Config1.class);
                });

        // false
        new ApplicationContextRunner().withUserConfiguration(Config2.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MyBean.class);
                    assertThat(context).doesNotHaveBean(Config1.class);
                });

    }

		@Configuration
    @TrueConditional
    static class Config1 {
        @Bean
        MyBean myBean() {
            return new MyBean();
        }
    }

    @Configuration
    @FalseConditional
    static class Config2 {
        @Bean
        MyBean myBean() {
            return new MyBean();
        }
    }

    static class MyBean { }

		@Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Conditional(TrueCondition.class)
    @interface TrueConditional { }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Conditional(FalseCondition.class)
    @interface FalseConditional { }

    static class TrueCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return true;
        }
    }

    static class FalseCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return false;
        }
    }

}
```

## 애노테이션의 애트리뷰트를 이용해 조건 판단하기

위에서 True와 False를 따로 만들었던 것을 애노테이션의 애트리뷰트로 하나로 합칠 수 있다. boolean 필드를 갖는 @BooleanConditional을 만들어 컨피그에 붙이고, BooleanCondition의 matches에서 메타 정보를 읽어 value 값에 따라 등록 여부를 반환하면 된다.

```java
@Configuration
@BooleanConditional(value = true)
static class Config1 {
	@Bean
	MyBean myBean() {
		return new MyBean();
	}
}

@Configuration
@BooleanConditional(value = false)
static class Config2 {
	@Bean
	MyBean myBean() {
		return new MyBean();
	}
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Conditional(BooleanCondition.class)
@interface BooleanConditional {
	boolean value();
}

static class BooleanCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// AnnotatedTypeMetadata : 애노테이션이 적용된 환경의 메타 정보 ex) Config1, Config2의 메타 정보
		// metadata.getAnnotationAttributes(String annotationName) : 메타 정보에서 애트리뷰트만 불러오기
		Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(BooleanConditional.class.getName());
		// annotationAttributes.forEach((s, o) -> System.out.println("key : "+s+", value : "+o));
		return (Boolean) annotationAttributes.get("value");
	}
}
```
