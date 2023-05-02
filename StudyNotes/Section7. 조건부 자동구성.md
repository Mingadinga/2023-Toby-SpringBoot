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

# 커스텀 @Conditional

스프링 부트가 빈 등록의 조건으로 사용하는 대표적인 시나리오는 **현재 어떤 라이브러리가 이 프로젝트에 포함**되어있는가이다. 라이브러리에 제티 서버가 있으면 제티를 띄우고, 없으면 톰캣을 띄우는 식으로 말이다.

톰캣 내장서버의 핵심 클래스는 `package org.apache.catalina.startup`에, 제티 내장서버의 핵심 클래스는 `package org.eclipse.jetty.server`에 들어있다. 이 클래스가 존재하는지를 확인해서 조건을 구성하면 된다.

## isPresent

이 클래스가 프로젝트에 포함되어있는지 확인하기 위해 Spring이 제공하는 유틸리티를 활용한다. `isPresent`를 사용하여 클래스의 풀 패키지 경로로 클래스 유무를 확인할 수 있다.

```java
// Tomcat Condition
static class TomcatCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return ClassUtils.isPresent("org.apache.catalina.startup.Tomcat", context.getClassLoader());
	}
}

// Jetty Condition
static class JettyCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return ClassUtils.isPresent("org.eclipse.jetty.server.Server", context.getClassLoader());
	}
}
```

Gradle에서 제티와 톰캣 라이브러리 의존성을 제거하여 조건 자동구성을 확인할 수 있다. 상위 모듈 아래 포함된 라이브러리는 다음과 같이 제거할 수 있다.

```
dependencies {
	implementation ('org.springframework.boot:spring-boot-starter-web') {
		exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
	}

	implementation 'org.springframework.boot:spring-boot-starter-jetty'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## 메타 애노테이션 활용

애노테이션 안에 matches 코드가 반복되므로 메타 애노테이션으로 분리해서 중복을 없애자. 풀 패키지 경로를 필드로 받아서 Utils로 isPresent로 존재 유무를 확인하는 애노테이션을 만들고, 이 애노테이션을 메타 애노테이션으로 갖는 애노테이션을 만들어 컨피그 클래스에 사용한다.

```java
public class MyOnClassCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalMyOnClass.class.getName());
        String value = (String) attrs.get("value");
        return ClassUtils.isPresent(value, context.getClassLoader());
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Conditional(MyOnClassCondition.class)
public @interface ConditionalMyOnClass {
    String value(); // 이런 클래스가 존재하는가
}

@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
public class TomcatWebServerConfig {

    @Bean("tomcatWebServerFactory")
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

}

@MyAutoConfiguration
@ConditionalMyOnClass("org.eclipse.jetty.server.Server")
public class JettyWebServerConfig {

    @Bean("jettyWebServerFactory")
    public ServletWebServerFactory servletWebServerFactory() {
        return new JettyServletWebServerFactory();
    }

}
```

# 자동 구성 정보 대체하기

스프링 부트가 자기 주장대로 실행하는 빈 메소드의 오브젝트를 무시하고 내가 만든 커스텀 컨피그를 사용하도록 구성정보를 변경할 수 있다. 커스텀 컨피그 클래스는 사용자 구성정보 등록하듯 컴포넌트 스캔을 사용하되, 자동 구성정보로 정의되는 빈을 대신해 등록된다.

스프링 부트가 자동으로 넣어주는 인프라 스트럭처 빈은 개발하면서 신경 쓰지 않아도 된다. 하지만 개발 도중 필요에 의해 기술과 관련된 인프라 스트럭처 빈을 직접 등록해야하는 경우가 있다. 컨디셔널 애노테이션을 확장해서 사용한다.

애플리케이션 패키지에 다음과 같이 9090번 포트로 서버를 띄우는 톰캣 내장서버 빈을 정의한다. @Configuration을 사용했으므로 컴포넌트 스캔에 의해 등록된다.

```java
package tobyspring.helloboot;

@Configuration(proxyBeanMethods = false) // 메소드 호출로 의존관계 주입할 것이 아니므로
public class WebServerConfiguration {
    @Bean
    ServletWebServerFactory customWebServerFactory() {
        TomcatServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
        serverFactory.setPort(9090);
        return serverFactory;
    }
}
```

이때 스프링이 자동으로 등록하는 톰캣 서버와 충돌이 나기 때문에 컨디셔널로 조건을 등록해야한다. 유저 구성정보를 먼저 스캔한 후 자동 구성정보를 스캔하므로, 자동 구성정보에 있는 톰캣 빈에 조건을 추가해 ServletWebServerFactory 타입 빈의 등록 유무를 확인한다. 스프링이 제공하는 `@ConditionalOnMissingBean`를 사용한다. 유저 구성정보의 우선순위가 더 높아진다.

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
public class TomcatWebServerConfig {

    @Bean("tomcatWebServerFactory")
    @ConditionalOnMissingBean // ServletWebServerFactory 타입 빈이 없으면 이 빈을 등록
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

}
```

스프링 부트로 프로젝트를 처음 만들면 스프링 부트가 설정해놓은 자동 구성정보에 의해 인프라 스트럭처 빈이 자동으로 등록된다. 하지만 개발을 하다가 내가 원하는 인프라 스트럭처 빈을 등록하고 싶다면 @Conditional을 확장해 자동 구성 빈을 대체할 수 있다. 따라서 스프링 부트로 빠르게 시작하고, 개발을 하는 단계에서 스프링의 자동 구성정보를 걷어내는 방식으로 개발을 할 수 있다. 그런데 스프링 부트가 워낙 잘 구성을 해놓아서 대부분 그대로 사용해도 된다. ^^


# 스프링 부트의 @Conditional

스프링 부트가 제공하는 자동 구성정보가 어떻게 만들어져있는지 확인해본다. 스프링 부트는 @Conditional을 확장하여 자동 구성정보를 제공한다. 스프링 부트가 제공하는 자동구성이 어떤 조건을 만족할 때 어떤 빈이 등록되는지 알고 있어야 커스톰 빈을 등록해 대체할 수 있다. 특히 직접 만든 기술이나 스프링 부트가 지원하지 않는 기술을 빈으로 등록해야할 때 필요하다.

> `@ConditionalOnClass` 클래스와 `@ConditionalOnMissingBean` 조합은 가장 대표적으로 사용하는 방식이다. 클래스의 존재로 해당 기술 사용 여부를 판단하고, 직접 추가한 커스톰 빈 구성의 존재를 확인해서 자동 구성의 빈 오브젝트를 이용할지 최종 결정한다.
>

## Class Conditions

- `@ConditionalOnClass`
- `@ConditionalOnMissingClass`

지정한 클래스의 프로젝트 내 존재를 확인해서 포함 여부를 결정한다.

주로 @Configuration 클래스 레벨에서 사용한다. 클래스 레벨이 거짓이면 내부 메소드는 확인하지 않는다. 클래스 레벨이 참이면 내부 메소드의 조건도 확인해서 빈으로 등록한다.

이 애노테이션을 클래스에 사용하지 않고 빈 메소드에 사용하면 불필요하게 메모리에 올라오는 빈이 생기므로, 클래스 레벨에 사용해야한다.

## Bean Conditions

- `@ConditionalOnBean`
- `@ConditionalOnMissingBean`

빈의 존재 여부를 기준으로 포함여부를 결정한다. 빈의 타입 또는 이름을 지정한다. 지정된 빈 정보가 없으면 메소드의 리턴 타입을 기준으로 빈의 존재 여부를 체크한다.

`@ConditionalOnMissingBean`은 컨테이너에 등록된 빈 정보를 기준으로 체크하기 때문에 적용 순서가 중요하다. 스프링이 자동구성을 할 때 사용자 구성정보(애플리케이션 빈)를 먼저 읽고 외부파일을 읽기 때문에, 사용자 구성정보를 우선하려는 경우 안전하다. 반면 커스톰 빈 구성정보에 적용하는건 피해야한다.

## 스프링의 @Profile

어떤 환경에서 해당 Bean을 사용할지를 결정하는 애노테이션. 개발 환경, 운영 환경, 테스트 환경 등 서로 다른 환경에서 실행할 때, 특정 프로파일에 맞게 설정된 빈만 사용하도록 제한할 수 있다. 예를 들어`@Profile("dev")` 애노테이션이 지정된 빈을 등록한 후, `spring.profiles.active` 시스템 프로퍼티를 "dev"로 설정하면 해당 빈이 활성화된다.

@Profile도 내부적으로 @Conditional을 사용하여 빈 등록 환경을 구분한다. 현재 활성화된 환경의 빈이라면 빈으로 등록한다.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ProfileCondition.class)
public @interface Profile { 
	String[] value();
}

class ProfileCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
		if (attrs != null) {
			for (Object value : attrs.get("value")) {
				if (context.getEnvironment().acceptsProfiles(Profiles.of((String[]) value))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

}
```