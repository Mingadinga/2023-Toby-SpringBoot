# 외부 설정을 이용한 자동 구성 요약

스프링의 자동구성으로 등록 대상이 되는 빈은 속성값을 변경할 수 있다. application.properties나 환경 변수처럼 외부 환경 변수를 주입하는 방법을 사용하는데, 스프링은 환경 변수를 얻어오는 환경을 Environment로 추상화하여 다룬다. 컨피그 클래스를 등록하는 빈에서 환경 변수 값을 사용하는데, 프로퍼티를 클래스로 분리하고 빈 후처리기 빈으로 등록해 바인딩 작업을 한다. 애노테이션에 prefix를 추가하여 네임스페이스를 지정할 수 있다. 스프링이 자동구성정보로 등록하는 빈의 설정 정보가 궁금하다면 프로퍼티 클래스를 확인하면 된다.

- Environment
- 프로퍼티 클래스
- 빈 후처리기 `BeanPostProcessor`
- env와 프로퍼티 바인딩 작업 `Binder`


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

# @Value와 PropertySourcesPlaceholderConfigurer

이제부터 추상화된 env에서 프로퍼티 값을 읽어오는 부분을 재사용할 수 있도록 추상화해본다. 빈 클래스에 필드를 만들어두고 프로퍼티에서 읽어온 값을 주입해보자. contextPath를 필드로 분리하고 @Value를 사용하여 치환자로 값을 읽어오도록 설정했다.

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
public class TomcatWebServerConfig {

    @Value("${contextPath}") // 치환자, 빈 후처리기로 설정 파일에서 값 주입
    String contextPath;

    @Bean("tomcatWebServerFactory")
    @ConditionalOnMissingBean
    public ServletWebServerFactory servletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setContextPath(this.contextPath);
        return factory;
    }

}
```

@Value만으로는 설정파일의 값을 읽어올 수 없다. 이 작업은 빈 후처리기를 사용하여 값을 주입받는 부가기능을 적용해야 한다. 빈 후처리기는 빈이 생성된 후 추가적인 작업을 하는 모듈이다. 빈 후처리기를 구현하는 `PropertySourcesPlaceholderConfigurer`를 빈으로 등록한다. 그리고 imports 파일에 풀 패키지 경로를 추가하여 빈 등록 후보로 설정한다.

```java
@MyAutoConfiguration
public class PropertyPlaceHolderConfig {
    @Bean
    PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer(); // 추상화된 env로부터 치환자의 값을 지정
    }
}

// tobyspring.config.MyAutoConfiguration.imports
tobyspring.config.autoconfig.PropertyPlaceHolderConfig
```

# 프로퍼티 클래스의 분리

프로퍼티 개수가 많아지면 관리가 어렵고, 재사용할 수 없다. 또한 컨피그 클래스의 응집도가 떨어진다. 프로퍼티를 별도의 클래스로 분리하고 빈으로 등록해서 컨피그 클래스에서 사용한다. 그리고 env로부터 값을 받아서 프로퍼티 클래스에 주입하고 프로퍼티를 빈으로 등록해주는 컨피그 클래스도 필요하다. 이 컨피그는 스프링이 제공하는 Binder를 사용하면 편리하게 작성할 수 있다.

```java
public class ServerProperties {
    private String contextPath;
    private int port;
		// getter, setter
}

@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
public class TomcatWebServerConfig {

    @Bean("tomcatWebServerFactory")
    @ConditionalOnMissingBean
    public ServletWebServerFactory servletWebServerFactory(ServerProperties serverProperties) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setContextPath(serverProperties.getContextPath());
        factory.setPort(serverProperties.getPort());
        return factory;
    }

}

@MyAutoConfiguration
public class ServerPropertiesConfig {
    @Bean
    public ServerProperties serverProperties(Environment env) {
//        ServerProperties properties = new ServerProperties();
//        properties.setContextPath(env.getProperty("contextPath"));
//        properties.setPort(Integer.parseInt(env.getProperty("port")));
//        return properties;

        // env의 key와 ServerProperties의 setter 이름이 같으면 주입
        return Binder.get(env).bind("", ServerProperties.class).get();
    }
}
```

스프링 자동구성빈에 등록되는 설정값을 확인하려면 어떤 프로퍼티 클래스를 사용하는가를 확인해보아야한다. 예를 들어 스프링부트가 Tomcat을 띄울 때 ServerProperties 파일을 사용한다. 프로퍼티 클래스에 포함된 객체는 . 연산자로 연결해서 들어갈 수 있다.

```java
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {

	private Integer port;
	private InetAddress address;
	@NestedConfigurationProperty
	private final ErrorProperties error = new ErrorProperties();
	private ForwardHeadersStrategy forwardHeadersStrategy;
	private String serverHeader;
	private DataSize maxHttpHeaderSize = DataSize.ofKilobytes(8);
	private Shutdown shutdown = Shutdown.IMMEDIATE;

	@NestedConfigurationProperty
	private Ssl ssl;

	@NestedConfigurationProperty
	private final Compression compression = new Compression();

	@NestedConfigurationProperty
	private final Http2 http2 = new Http2();

	private final Servlet servlet = new Servlet();

	private final Reactive reactive = new Reactive();

	private final Tomcat tomcat = new Tomcat();

	private final Jetty jetty = new Jetty();

	private final Netty netty = new Netty();

	private final Undertow undertow = new Undertow();

	public Integer getPort() {
		return this.port;
	}
	// ..
}
```

# 프로퍼티 빈의 후처리기 도입

앞에서 프로퍼티 클래스를 만들고 env에서 값을 주입(바인딩)하는 컨피그 클래스를 등록해서 자동등록되는 빈에 설정값을 부여했다. 그런데 이 방법에는 문제가 있다.

자동구성으로 등록하는 기술 종류가 많아지면 프로퍼티 클래스가 많아지고, 값을 주입하는 컨피그 클래스도 만들어야 한다. 게다가 이 설정정보도 조건부 자동등록에 적용되는데, 컨피그 클래스에 조건을 다는게 복잡하다. (톰캣 또는 제티가 사용되고 있으면 프로퍼티 빈을 등록해라 등..)

프로퍼티에 값을 주입하는 바인딩 작업을 컨피그 클래스 대신 빈 후처리기를 사용해서 개선해보자.

우선 프로퍼티 클래스를 빈으로 등록하자. 우선 빈 등록은 @Component나 @Import를 사용한다.

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
@Import(ServerProperties.class)
public class TomcatWebServerConfig {}

// 혹은
@Component
public class ServerProperties {}
```

바인딩 작업을 하는 `PropertyPostProcessorConfig` 빈 후처리기를 등록한다. (설정정보에도 풀 패키지 경로를 추가해야한다) 이 빈에서는 `@MyConfigurationProperties` 이 붙은 프로퍼티 클래스에 바인딩 작업을 해준다. 마지막으로 해당 애노테이션을 프로퍼티 클래스에 붙여서 바인딩 작업이 되도록 변경하고, 프로퍼티를 빈으로 등록하는 @Component를 메타 애노테이션으로 가지도록 했다.

```java
// 마킹 애노테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface MyConfigurationProperties {
}

// 빈 후처리기 등록
@MyAutoConfiguration
public class PropertyPostProcessorConfig {
    @Bean
    BeanPostProcessor propertyPostProcessor(Environment environment) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // MyConfigurationProperties이 붙어있으면
                MyConfigurationProperties annotation = AnnotationUtils.findAnnotation(bean.getClass(), MyConfigurationProperties.class);
                if (annotation == null) return bean; // 애노테이션 없으면 그냥 반환
                return Binder.get(environment).bindOrCreate("", bean.getClass()); // 바인딩
            }
        };
    }
}

@MyConfigurationProperties
public class ServerProperties { }
```

외부 설정 값에 prefix를 붙여서 사용할 수 있다. prefix는 프로퍼티의 네임스페이스 역할을 한다. 애노테이션에 prefix 속성을 추가하고, 빈 후처리기에서 prefix 값을 얻어와 Binder에 추가한다.

```java
@MyConfigurationProperties(prefix = "server") // 프로퍼티의 네임스페이스
public class ServerProperties { }

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface MyConfigurationProperties {
    String prefix();
}

@MyAutoConfiguration
public class PropertyPostProcessorConfig {
    @Bean
    BeanPostProcessor propertyPostProcessor(Environment environment) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                MyConfigurationProperties annotation = AnnotationUtils.findAnnotation(bean.getClass(), MyConfigurationProperties.class);
                if (annotation == null) return bean; // 애노테이션 없으면 그냥 반환

                // prefix 확인
                Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
                String prefix = (String) attributes.get("prefix");

                // prefix로 바인딩
                return Binder.get(environment).bindOrCreate(prefix, bean.getClass()); 
            }
        };
    }
}
```

이때 컨피그 클래스에 붙어있는 @Import를 재사용하기 위해 @Enable..로 시작하는 애노테이션으로 분리한다. 스프링부트가 사용하는 스타일이다. `@EnableMyConfigurationProperties`이 `@Import`를 메타 애노테이션으로 가지고, 불러올 등록 후보는 `Selector` 클래스를 구현하여 만든다.

```java
@MyAutoConfiguration
@ConditionalMyOnClass("org.apache.catalina.startup.Tomcat")
@EnableMyConfigurationProperties(ServerProperties.class)
public class TomcatWebServerConfig {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(MyConfigurationPropertiesImportSelector.class)
public @interface EnableMyConfigurationProperties {
    Class<?> value();
}

public class MyConfigurationPropertiesImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        MultiValueMap<String, Object> attr = importingClassMetadata.getAllAnnotationAttributes(EnableMyConfigurationProperties.class.getName());
        Class propertyClass = (Class) attr.getFirst("value");
        return new String[] {propertyClass.getName()};
    }
}
```