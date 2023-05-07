지금까지 살펴본 스프링부트의 자동 구성 정보 원리를 실제 스프링부트의 동작에서 알아본다.

# 스프링 부트의 자동 구성과 테스트로 전환

- `@JdbcTest` : embedded dataSource를 사용한다. 테스트용 db보다 훨씬 빠르게 테스트 가능하다. 기본적으로 트랜잭션이 적용되어 성공적으로 실행하면 롤백함.
- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.*NONE*)` : 스프링 애플리케이션 컨텍스트를 로드하되, 서블릿 컨테이너는 시작하지 않는다. db나 외부 종속성이 없는 서비스 계층의 테스트에서 사용할 수 있다. 스프링 트랜잭션 설정을 하지 않으므로 db를 사용하는 테스트는 롤백되지 않을 수 있음. 그래서 `@Transactional`을 붙여야한다.
- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.*DEFINED_PORT*)` : 애플리케이션을 실행할 때 임의의 포트 대신 지정된 포트를 사용하도록 설정한다.

# 스프링 부트의 자동 구성 흐름

![스크린샷 2023-05-07 오전 11.03.23.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/d06f9265-a2e7-4edb-9c05-7c7a12f11e2a/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-05-07_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_11.03.23.png)

1. 사용 기술 선택 : 자바 or 코틀린, Web or Mobile, Servlet(스레드 기반 동기 - spring mvc) or Reactive(비동기 - spring webflux, 데이터 액세스 방식, 보안, 캐싱, 클라우드 등등
2. Spring Initializr : 프로젝트 템플릿 생성
3. build.gradle에 클래스 및 라이브러리 추가 : Springboot Starter과 Dependencies 등
4. 스프링의 자동 구성 빈 준비
    1. 자동구성 후보 로딩 : @AutoConfiguration이 붙은 인프라 스트럭처 빈들의 컨피그 클래스
    2. 조건부 자동구성 : @Conditional의 매칭 조건이 참이면 빈으로 등록. 임포트된 라이브러리에 클래스 존재 유무나 컨테이너 빈 등록 여부에 따라 조건 작성.
    3. 디폴트 자동 구성 인프라 빈 : 인프라 빈이 디폴트 속성 정보를 가진 상태로 준비됨.
    4. 외부 환경 설정값 주입 : 외부의 다양한 프로퍼티 소스를 추상화된 env로 읽어서 인프라 빈에 설정값을 세팅함.
5. 사용자 구성 빈 준비 - 컴포넌트 스캔 방식
    1. 애플리케이션 로직 빈 : @Component
    2. 커스톰 인프라 빈 : @Configuration
    3. 추가 인프라 빈 : 스프링에서 사용하는 서드파티 기술의 인프라 스트럭처 빈
6. 자동 구성 인프라스트럭처 빈 : 커스톰 인프라 빈이 자동구성 인프라 빈을 대체하여 등록됨
7. 유저 구성 애플리케이션 빈 : 애플리케이션 로직 빈, 추가 인프라 빈 등록

그래서 어떤 기술을 선택하고, 그 기술을 제공하는 스프링의 인프라 스트럭처 빈은 무엇이고, 어떤 설정값을 주입할 수 있고, 어떤 조건으로 등록되는가? 스프링부트가 등록해주는 빈들을 살펴보면 백엔드 개발에 필요한 다양한 기술이 무엇인지 알 수 있다. 거기에서 시작해 기술을 익히고 활용하면 더 풍성한 개발이 가능하다.

# 자동 구성 분석 방법

스프링부트가 제공하는 자동구성이 어떤 것이 적용되는지, 어떻게 이용할 수 있는지 그 방법을 알아보자. 이 방법을 통해 스프링부트의 코어 모듈(web, jdbc) 없이 포함되는 자동구성은 어떤 것이 있는지 알아보자.

1. 자동구성 클래스 Condition 결과 로그 : 스프링이 제공하는 imports 파일의 모든 등록 후보 빈을 보여준다.
2. 자동구성 클래스 Condition 결과 빈 ConditionEvaluationReport : 직접 코드를 작성해서 자동구성의 조건을 확인해본다. 원하는 빈만 확인할 수 있다.
3. 최종으로 등록된 빈 확인 ListableBeanFactory : 스프링 컨테이너에 포함된 인터페이스.
4. 컨피그 빈에 해당하는 문서에서 관련 기술, 자동구성, 프로퍼티 확인
5. 라이브러리 코드에서 자동 구성 클래스와 조건, 빈 확인 : @AutoConfiguration, @Conditional, Condition, @Bean
6. 프로퍼티 클래스와 바인딩 정보 확인 : Properties, Bind, Customizer(오브젝트의 기본 설정을 바꾸는 객체. 프로퍼티 클래스에 주입한다), Configurer(스프링 fw의 주요 설정을 바꿀 수 있는 인터페이스)

그외의 TIP

- 자동구성되는 빈(스프링 fw, 자바 표준, 오픈소스 기술 등)을 잘 모르는 경우, 용도 정도만 공부해두면 나중에 활용할 수 있음.
- 프로퍼티가 enum 타입인 경우, 어떤 옵션을 사용할 수 있는지 스프링 레퍼런스 확인. 라이브러리가 추가되는 경우에 따라 조건을 다르게 줄 수도 있다.

# 자동 구성 조건 결과 확인

스프링부트가 자동구성을 어떤 식으로 준비하는지 확인해보자. 어떠한 dependency도 추가하지 않은 스프링부트는 스프링 컨테이너만 stand alone으로 동작한다. 스프링부트의 core만 띄웠을 때 어떤 자동구성 빈들이 올라오는지 알아보자.

VM 옵션에 debug를 사용하여 애플리케이션을 다시 실행해보자.

![스크린샷 2023-05-08 오전 7.18.08.png](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/3f9f0b14-f783-408b-81ce-b94bc2915176/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-05-08_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_7.18.08.png)

다음과 같이 등록 후보 빈들이 콘솔 창에 찍힌다.

- Positive matches : Condition을 통과한 컨피그 빈. Aop, 캐시, Jmx(Java Management Extension), 라이프 사이클, property place holder, Sql, TaskExecution
- Negative matches : 조건을 통과하지 못한 컨피그 빈. 클래스가 존재하지 않아서 조건을 통과하지 못함.

그런데 디버깅을 사용하는 방식은 모든 빈이 출력되어 방대하고, 관심 있는 부분만 보기 어렵다는 단점이 있다. 이번엔 ConditionEvaluationReport 객체를 사용해 로그를 추출하거나 조작해보자.

```java
@SpringBootApplication
public class SpringbootAcApplication {

    @Bean
    ApplicationRunner run(ConditionEvaluationReport report) {
        return args -> {
            // 조건을 통과했고 Jmx 문자열이 포함되지 않음
            report.getConditionAndOutcomesBySource().entrySet().stream()
                    .filter(co -> co.getValue().isFullMatch())
                    .filter(co -> co.getKey().indexOf("Jmx") < 0)
                    // 통과한 빈과 조건 출력
                    .forEach(co -> {
                        System.out.println(co.getKey());
                        co.getValue().forEach(c -> System.out.println("\t"+c.getOutcome()));
                        System.out.println();
                    });
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringbootAcApplication.class, args);
    }

}
```

위의 예시처럼 자바 코드로 조건을 걸거나 원하는 정보를 로그(ConditionEvaluationReport)로부터 출력할 수 있다. 조건을 filter로 걸고, 각 빈의 정보는 key나 valuef에서 추출할 수 있다.

# Core 자동 구성 살펴보기

## AopAutoConfiguration

설정값의 key는 [spring.aop.name](http://spring.aop.name) 이다. havingValue 값이 true이면 등록되고, 만약 만족하는 조건이 없다면 등록되도록 했다. 그래서 별다른 설정 없이도 Aop 컨피그가 등록되었다.

```java
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration { }
```

AopAutoConfiguration 내부에 후보로 있는 빈을 살펴보자. AspectJ 기반 Aop 컨피그는 Advice.class 클래스가 존재하는 경우 등록된다. 반면 클래스 기반 컨피그는 Advice.class 경로로 클래스를 찾을 수 없다면 등록된다. 그래서 클래스 기반의 aop 컨피그가 등록되어 프록시 기반으로 동작한다.

```java
@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Advice.class)
	static class AspectJAutoProxyingConfiguration {

		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = false)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false")
		static class JdkDynamicAutoProxyConfiguration {

		}

		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = true)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
				matchIfMissing = true)
		static class CglibAutoProxyConfiguration {

		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.aspectj.weaver.Advice")
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
			matchIfMissing = true)
	static class ClassProxyingConfiguration {

		@Bean
		static BeanFactoryPostProcessor forceAutoProxyCreatorToUseClassProxying() {
			return (beanFactory) -> {
				if (beanFactory instanceof BeanDefinitionRegistry) {
					BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
					AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
					AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
				}
			};
		}

	}

}
```

## 캐시

[Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching)

기본 구현체로 simple provider가 제공되며, 캐시를 설정하고 싶다면 `spring.cache.cache-names` 속성을 지정하면 된다.

스타터 모듈을 추가했을 때 사용 가능한 캐시 기술은 다음과 같고, CacheManager과 CacheResolver에 의존한다. (PSA)

1. [Generic](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.generic)
2. [JCache (JSR-107)](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.jcache) (EhCache 3, Hazelcast, Infinispan, and others)
3. [Hazelcast](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.hazelcast)
4. [Infinispan](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.infinispan)
5. [Couchbase](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.couchbase)
6. [Redis](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.redis)
7. [Caffeine](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.caffeine)
8. [Cache2k](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.cache2k)
9. [Simple](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.caching.provider.simple)

캐시를 적용하려면 다음과 같이 클래스나 메소드에 @Cacheable을 붙인다.

```java
@Component
public class MyMathService {

    @Cacheable("piDecimals")
    public int computePiDecimal(int precision) {
        ...
    }

}
```

## PropertyPlaceholderAutoConfiguration

@Value(치환자)에 해당하는 값을 외부 설정 파일로부터 찾아서 값을 주입하는 빈 후처리기다.

```java
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class PropertyPlaceholderAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(search = SearchStrategy.CURRENT)
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
```

- @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE) : 최우선으로 생성해라
- @ConditionalOnMissingBean(search = SearchStrategy.CURRENT) : 현재 컨텍스트에서만 PropertySourcesPlaceholderConfigurer 타입의 빈을 검색하고, 존재하지 않으면 이 빈을 생성한다.

[SearchStrategy (Spring Boot 3.0.6 API)](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/condition/SearchStrategy.html)

@ConditionalOnMissingBean의 SearchStrategy 타입으로 다음과 같은 값을 사용할 수 있다.

- **`[ALL](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/condition/SearchStrategy.html#ALL)` :** Search the entire hierarchy.
- **`[ANCESTORS](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/condition/SearchStrategy.html#ANCESTORS)`** : Search all ancestors, but not the current context.
- **`[CURRENT](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/condition/SearchStrategy.html#CURRENT)`** : Search only the current context.

## TaskExecutionAutoConfiguration

스레드 풀을 제공하는 인프라 스트럭처 빈.

이 컨피그에서 등록되는 빈은 applicationTaskExecutor, taskExecutorBuilder으로, applicationTaskExecutor이 등록될 때 빌더 빈을 주입 받아서 사용한다. 스프링에는 이처럼 빌더를 빈으로 등록해서 재사용 가능하도록 만드는 경우가 꽤 있다.

```java
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@AutoConfiguration
@EnableConfigurationProperties(TaskExecutionProperties.class)
public class TaskExecutionAutoConfiguration {

	/**
	 * Bean name of the application {@link TaskExecutor}.
	 */
	public static final String APPLICATION_TASK_EXECUTOR_BEAN_NAME = "applicationTaskExecutor";

	@Bean
	@ConditionalOnMissingBean
	public TaskExecutorBuilder taskExecutorBuilder(TaskExecutionProperties properties,
			ObjectProvider<TaskExecutorCustomizer> taskExecutorCustomizers,
			ObjectProvider<TaskDecorator> taskDecorator) {
		TaskExecutionProperties.Pool pool = properties.getPool();
		TaskExecutorBuilder builder = new TaskExecutorBuilder();
		builder = builder.queueCapacity(pool.getQueueCapacity());
		builder = builder.corePoolSize(pool.getCoreSize());
		builder = builder.maxPoolSize(pool.getMaxSize());
		builder = builder.allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout());
		builder = builder.keepAlive(pool.getKeepAlive());
		Shutdown shutdown = properties.getShutdown();
		builder = builder.awaitTermination(shutdown.isAwaitTermination());
		builder = builder.awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod());
		builder = builder.threadNamePrefix(properties.getThreadNamePrefix());
		builder = builder.customizers(taskExecutorCustomizers.orderedStream()::iterator);
		builder = builder.taskDecorator(taskDecorator.getIfUnique());
		return builder;
	}

	@Lazy
	@Bean(name = { APPLICATION_TASK_EXECUTOR_BEAN_NAME,
			AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME })
	@ConditionalOnMissingBean(Executor.class)
	public ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutorBuilder builder) {
		return builder.build();
	}

}
```

빌더를 만드는데 사용되는 Properties이다. 참고로 `ThreadPoolTaskExecutor`에서는 코어 사이즈를 1로 바꿔서 사용한다. 이처럼 스프링부트에서 제공하는 클래스의 기본값을 알고 값을 변경해 사용할 수 있어야 한다.

```java
@ConfigurationProperties("spring.task.execution")
public class TaskExecutionProperties {

	private final Pool pool = new Pool();

	private final Shutdown shutdown = new Shutdown();

	private String threadNamePrefix = "task-";

	public static class Pool {

		/**
		 * Queue capacity. An unbounded capacity does not increase the pool and therefore
		 * ignores the "max-size" property.
		 */
		private int queueCapacity = Integer.MAX_VALUE;

		/**
		 * Core number of threads.
		 */
		private int coreSize = 8;

		/**
		 * Maximum allowed number of threads. If tasks are filling up the queue, the pool
		 * can expand up to that size to accommodate the load. Ignored if the queue is
		 * unbounded.
		 */
		private int maxSize = Integer.MAX_VALUE;

		/**
		 * Whether core threads are allowed to time out. This enables dynamic growing and
		 * shrinking of the pool.
		 */
		private boolean allowCoreThreadTimeout = true;

		/**
		 * Time limit for which threads may remain idle before being terminated.
		 */
		private Duration keepAlive = Duration.ofSeconds(60);
}
```

