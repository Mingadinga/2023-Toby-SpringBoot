목차
- 메타 애노테이션
- 합성 애노테이션
- AutoConfiguration 구조로 확장하기
    - 빈 오브젝트 역할과 구분
    - 자동 구성 방식 도입하기 : 컴포넌트 스캔 제외, 기능 별 컨피그 클래스 분리, 동적으로 빈 분리
    - AutoConfiguration 구조
- Configuration과 proxyBeanMethods


이번 섹션에서는 스프링 부트가 어떻게 애플리케이션 개발을 빠르고 편리하게 만들어주는지를 자동 구성을 통해 살펴본다. 스프링 부트가 애노테이션을 활용할 때 사용하는 기법을 알아보고 응용 지식을 쌓는다.
특히 main에 남아있는 내장 서버와 디스패처 서블릿 빈을 등록하는 부분을 애플리케이션 빈으로부터 완전히 분리한다.

# 메타 애노테이션

애노테이션 A가 애노테이션 M를 가지고 있을 때, A가 M을 메타 애노테이션으로 사용한다고 부른다. 예를 들어 @Component를 가지고 있는 @Controller나 @Service가 있다. 여기서 메타 애노테이션은 @Component이다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {}
```

@Component, @Controller, @Service는 기능적인 면에서 동일하다. 스프링 컨테이너가 빈으로 인식하는 것이다. 다만 다른 이름을 부여해서 **추가적인 정보**를 줄 수 있다. Controller나 Service는 전통적인 mvc 구조에서 계층을 드러낸다.

참고로 메타 애노테이션을 사용하는 것은 객체 상속과는 다른 개념이다. 기능을 물려줄 수는 있지만, 하위 애노테이션이 상위 애노테이션으로 사용될 수는 없다. 타깃이 애노테이션 타입인 애노테이션만이 메타 애노테이션이 될 수 있다. 메타 애노테이션은 애노테이션에 붙는 것이기 때문이다.

```java
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.0")
@Testable
public @interface Test {
}
```

Junit의 Test 애노테이션 타깃으로 ElementType.ANNOTATION_TYPE이 지정되어 있다. @Test는 다른 애노테이션의 메타 애노테이션으로 활용될 수 있다.

Main에 남아있는 팩토리 빈 메소드를 별도의 클래스에 분리하여 @Configuration를 사용해 빈 구성 정보로 등록한다. @Configuration는 @Component를 메타 애노테이션으로 가지고 있어서, 서버가 뜰 때 같이 초기화된다.

```java
// before
public class HellobootApplication {

	@Bean
	public ServletWebServerFactory servletWebServerFactory() {
		return new TomcatServletWebServerFactory();
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet(); // spring container 필요
	}

	// ..

}

// after
@Configuration
public class Config {

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet(); // spring container 필요
    }

}
```

# 합성 애노테이션

스프링 부트가 애노테이션을 활용하는 또다른 방법으로 합성 애노테이션이 있다. 메타 애노테이션을 두개 이상 적용해서 **기능을 확장**하는 애노테이션이다. Controller와 ResponseBody를 메타 애노테이션으로 갖는 RestController가 그 예이다. RestController는 컴포넌트로 인식되며 응답 생성시 메시지 바디에 반환 객체를 그대로 작성한다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface RestController { }
```

우리가 작성한 main 클래스에 붙은 여러개의 애노테이션을 스프링부트처럼 바꾸기 위해 합성 애노테이션을 사용할 수 있다.

```java
// before
@Configuration @ComponentScan
public class HellobootApplication { }

// after
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 클래스 인터페이스 이넘
@Configuration
@ComponentScan
public @interface MySpringbootAnnotation {
}

@MySpringbootAnnotation
public class HellobootApplication { }
```

# AutoConfiguration 구조로 확장하기

## 빈 오브젝트 역할과 구분

스프링 컨테이너에 등록되는 빈들은 종류와 역할이 다르고, 설정 정보 작성 방법이 다르다. 어떤 종류의 빈에 어떤 스타일의 구성 정보를 적용하는지 알아보자.

- 애플리케이션 빈 : 개발자가 어떤 빈을 사용하겠다고 명시적으로 **구성정보를 제공**하는 빈. 구성 정보를 바탕으로 스프링 컨테이너가 빈으로 등록한다.
    - 애플리케이션 로직 빈(**사용자 구성정보**) : 애플리케이션의 기능 담당. Controller, Service, Entity
    - 애플리케이션 인트라스트럭처 빈(**자동 구성정보 - AutoConfiguration**) : 애플리케이션이 동작하면서 필요한 기술적인 빈. 이미 만들어져있는 빈을 구성 정보로 등록한다. DataSource, JdbcTranscationManager, TomcatServletWebServerFactory, DispatcherServlet 등
- 컨테이너 인프라스트럭처 빈 : 스프링 컨테이너 자신이거나, 스프링 컨테이너가 자신을 확장하면서 추가해온 것들을 빈으로 등록해서 사용하는 빈. **컨테이너가 스스로 등록**해서 동작시킨다. ApplicationContext, BeanPostProcessor, Environment 등

## 자동 구성 방식 도입하기

기능 별로 자동 구성 정보 클래스를 나누어서 @Configuration을 붙인다. 예를 들어 서블릿 컨테이너를 구성하는데 필요한 ServletWebServerFactory, 스프링 웹을 사용하는데 필요한 DispatcherServlet을 따로 구성한다. 스프링 컨테이너가 애플리케이션의 필요에 따라 구성 정보를 적용해서 빈으로 등록한다.

### 컴포넌트 스캔에서 제외하기

우리가 만든 애플리케이션 인프라스트럭처 빈인 TomcatServletWebServerFactory, DispatcherServlet에 AutoConfigure을 적용해보자. 지금은 다음과 같이 팩토리 메소드를 사용해 사용자 구성 정보 방식으로 빈을 등록하고 있다.

```java
// before
package tobyspring.helloboot;

@Configuration
public class Config {

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet(); // spring container 필요
    }

}
```

이 클래스가 애플리케이션을 만드는 패키지 밑에 빈으로 직접 등록하지 않아도 자동으로 등록되지 않게 해야한다. 우선 **컴포넌트 대상에서 제외**하기 위해 다른 패키지로 이 클래스를 옮긴다.

```java
package tobyspring.config;

@Configuration
public class Config {

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet(); // spring container 필요
    }

}
```

컴포넌트 스캔 대상이 아니더라도 구성 정보에 포함되도록 만들어야한다. 컴포넌트 스캔은 main 클래스에 붙은 @Configuration, @ComponentScan을 메타 애노테이션으로 가진 @MySpringBootApplication에서 시작된다. 여기에 **@Import**를 추가하고 클래스 정보를 넘겨주면 해당 클래스를 구성 정보로 추가한다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 클래스 인터페이스 이넘
@Configuration
@ComponentScan
@Import(Config.class)
public @interface MySpringBootApplication {}
```

### 기능 별로 컨피그 클래스 분리하기

이제 Config 클래스 안에 있는 **팩토리 메소드를 분리하고, import에 추가**한다. 다만 상위 애노테이션에 클래스 이름을 그대로 노출하는 것은 좋지 않으므로 메타 애노테이션으로 분리한다.

```java
public class DispatcherServletConfig {
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet(); // spring container 필요
    }
}

public class TomcatServletWebServerConfig {
    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
**@Import({DispatcherServletConfig.class, TomcatServletWebServerConfig.class})**
public @interface EnableMyAutoConfiguration {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // 클래스 인터페이스 이넘
@Configuration
@ComponentScan
@EnableMyAutoConfiguration
public @interface MySpringBootApplication {
}
```

### 동적으로 빈 등록하기

현재는 등록할 빈을 Import에 하드코딩하고 있다. 하지만 실제로 이 두 빈이 언제나 등록되어야 하는 것은 아니므로, 동적으로 등록 가능하도록 변경해야한다. @Import가 아닌 `ImportSelector`을 사용한다.

```java
public class MyAutoConfigImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{
                "tobyspring.config.autoconfig.DispatcherServletConfig",
                "tobyspring.config.autoconfig.TomcatWebServerConfig"
        };
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MyAutoConfigImportSelector.class)
public @interface EnableMyAutoConfiguration {
}
```

이번에는 선택할 빈을 외부 파일로 분리해서 적용해본다. `ImportCandidates`을 사용해서 등록할 빈의 목록을 외부 파일로 분리하고 읽어온다. load로 Iterable<String>을 읽어서 문자열 배열을 반환해야하므로, 임시 컬렉션을 사용해 변환한다. load는 클래스 로더를 필요로 하는데, 이 클래스 로더는 스프링이 자동으로 넣어주는 빈에 속하므로 생성자 주입으로 받는다.

```java
public class MyAutoConfigImportSelector implements DeferredImportSelector {

    private final ClassLoader classLoader;

    public MyAutoConfigImportSelector(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        List<String> autoConfigs = new ArrayList<>();
        ImportCandidates.load(MyAutoConfiguration.class, classLoader).forEach(autoConfigs::add);
        return autoConfigs.stream().toArray(String[]::new);

    }

}
```

`ImportCandidates.load()`주석을 읽어보면 META-INF/spring/full-qualified-annotation-name.imports 경로의 파일에서 빈 정보를 읽어온다고 한다. 리소스 폴더 아래에 파일을 만들고 빈의 풀 패키지 경로를 작성한다.

```java
// tobyspring.config.MyAutoConfiguration.imports
tobyspring.config.autoconfig.DispatcherServletConfig
tobyspring.config.autoconfig.TomcatWebServerConfig
```

처음에 분리한 Config 클래스에 @MyAutoConfiguration을 추가한다. 없어도 동작하지만, 관례를 따라 붙여서 사용한다.

```java
@MyAutoConfiguration
public class TomcatWebServerConfig {
    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }
}

@MyAutoConfiguration
public class DispatcherServletConfig {
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet(); // spring container 필요
    }
}
```

그리고 @MyAutoConfiguration에 `proxyBeanMethods = false` 속성을 적용한다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration(proxyBeanMethods = false)
public @interface MyAutoConfiguration {
}
```

## AutoConfiguration 구조

자동 구성 정보를 사용하는 최종적인 구조는 다음과 같다. 애플리케이션 빈을 사용하는 패키지와 분리한다. 자동 구성으로 등록할 빈을 기능별로 다른 클래스에 분리하여 작성하고, Configuration을 붙인다. 자동 구성 정보만을 담은 외부 파일(`.imports`)을 따로 두고, `Selector`을 통해서 등록될 빈 정보를 가져온다. 이렇게 가져온 빈 목록을 Import로 가져오고(`@EnableMyAutoConfiguration`), 최종적으로 Configuration(`@MySpringBootApplication`)을 붙인다.

![스크린샷 2023-04-29 오후 1.56.14.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/59fa9176-3419-408a-82aa-2bdfb0873762/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-04-29_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_1.56.14.png)

# @Configuration과 proxyBeanMethods

@Configuration은 유저 구성 정보를 작성하는 중에도 사용할 일이 있다. 스프링이 자동으로 결정해준 애플리케이션 인프라스트럭처 빈이 아니라 직접 빈을 코드로 등록하는 작업을 하는 경우 사용한다. 그래서 @Configuration이 어떻게 동작하는지 알아두는 것이 중요하다. @Configuration은 싱글톤으로 관리되도록 프록시를 확장한다.

@Configuration은 스프링 컨테이너가 빈을 스캔해야하는 설정 정보 파일을 알리는 애노테이션이다. 여기에 자바 코드로 팩토리 메소드를 작성해 빈의 의존관계를 설정할 수 있다. 스프링 컨테이너는 기본적으로 싱글톤으로 빈을 관리하는데, 팩토리 메소드로 작성하다보면 싱글톤이 아닌 빈을 주입 받는 코드도 생긴다. 가령 이러하다. Bean1과 Bean2는 둘다 Common에 의존하는데, Common은 싱글톤이 아니다.

```java
@Test
public void configuration() {
	MyConfig myConfig = new MyConfig();
	Bean1 bean1 = myConfig.bean1();
	Bean2 bean2 = myConfig.bean2();
	Assertions.assertThat(bean1.common).isSameAs(bean2.common);
}

@Configuration
static class MyConfig {
	@Bean
	Common common() {
		return new Common();
	}

	@Bean
	Bean1 bean1() {
		return new Bean1(common());
	}

	@Bean
	Bean2 bean2() {
		return new Bean2(common());
	}

}
```

이렇게 설정된 의존 관계는 다른 설정 정보의 일관성을 해칠 수 있으므로, 스프링 컨테이너는 @Configuration이 붙은 클래스가 싱글톤으로 만들어지도록 프록시를 둔다. 다음과 같이 @Configuration 붙은 클래스인 MyConfig을 스프링 컨테이너에 등록하고 받아와서 사용하면, MyConfig는 프록시로 만들어져 빈들을 싱글톤으로 관리한다.

```java
@Test
    public void proxiedConfig() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
        ac.register(MyConfig.class);
        ac.refresh();

        MyConfig myConfig = ac.getBean(MyConfig.class);
        Bean1 bean1 = myConfig.bean1(); // 프록시
        Bean2 bean2 = myConfig.bean2(); // 프록시
        Assertions.assertThat(bean1.common).isSameAs(bean2.common);
    }
```

프록시는 싱글톤 관리의 일관성을 추구하기 위한 방법이지만, 한편으로는 프록시 생성에 드는 비용을 생각하면 프록시가 꼭 필요하지 않은 경우는 사용할 필요가 없다. 빈 설정 정보 안에서 싱글톤으로 주입해야하는 경우가 아니라면 프록시 설정을 끄고 사용하는 것이 더 경제적이다. 그래서 앞에서 `@MyAutoConfiguration`에 `@Configuration(proxyBeanMethods = false)` 설정을 붙인 것도 `@MyAutoConfiguration`를 사용하는 설정 정보 빈이 의존관계를 필요로 하지 않으므로 프록시 없이 단독 생성한 것이다.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration(proxyBeanMethods = false)
public @interface MyAutoConfiguration {
}

@MyAutoConfiguration
public class TomcatWebServerConfig {

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

}

@MyAutoConfiguration
public class DispatcherServletConfig {

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet(); // spring container 필요
    }

}
```