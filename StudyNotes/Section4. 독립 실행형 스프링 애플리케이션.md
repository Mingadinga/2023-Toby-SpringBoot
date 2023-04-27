서블릿과 관련된 코드를 핵심 비즈니스 로직과 완전히 분리하여, 애플리케이션을 개발할 때는 서블릿과 관련된 코드를 신경쓰지 않아도 되게끔 만들어보자.

# 스프링 컨테이너 도입

기존 코드는 프론트 컨트롤러 서블릿이 애플리케이션 빈인 helloController을 new로 직접 생성해서 사용했다. 이번에는 **helloController의 생성과 의존성 주입**을 Spring Container에게 위임한다. 스프링 컨테이너는 애플리케이션 기능을 담은 POJO와 설정 정보를 받아서 빈을 생성하고 서버 애플리케이션으로 띄운다.

```java
// Spring Container에 helloController 빈을 등록하고
// 프론트 컨트롤러에서 빈을 사용하기
public class HellobootApplication {

	public static void main(String[] args) {

		// 스프링 컨테이너 받고 빈 등록
		GenericApplicationContext applicationContext = new GenericApplicationContext(); // 코드로 받은 스프링 컨테이너
		applicationContext.registerBean(HelloController.class); // 구성 정보 등록
		applicationContext.refresh(); // 구성 정보를 바탕으로 컨테이너 초기화

		ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
		WebServer webServer = serverFactory.getWebServer(servletContext -> {

			servletContext.addServlet("frontcontroller", new HttpServlet() {

				@Override
				protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

					if (req.getRequestURI().equals("/hello") && req.getMethod().equals(HttpMethod.GET.name())) {
						// req
						String name = req.getParameter("name");

						// 스프링 컨테이너로부터 HelloController 빈 받기
						HelloController helloController = applicationContext.getBean(HelloController.class);
						String ret = helloController.hello(name);

						// resp
						resp.setContentType(MediaType.TEXT_PLAIN_VALUE);
						resp.getWriter().println(ret);
					}
					else {
						resp.setStatus(HttpStatus.NOT_FOUND.value());
					}

				}
			}).addMapping("/*"); // 중앙 처리
		});

		webServer.start();
	}

}
```

애플리케이션 빈의 생성과 의존성 주입을 스프링 컨테이너에 위임했다. 빈의 생성과 관리는 이제 스프링 컨테이너의 책임이다. 우리는 스프링 컨테이너가 애플리케이션 동작에 필요한 빈을 생성하고 관리할 것을 기대한다. 어떻게 생성하고 어떻게 관리할지는 스프링 컨테이너가 결정하는데, 스프링 컨테이너는 빈을 싱글톤 방식으로 관리하고 빈을 이름이나 타입으로 구분한다. new를 사용해 빈을 생성 사용하는 것과 스프링 컨테이너를 두는 것의 차이는 빈의 생성과 관리를 위임함으로써 앞으로 애플리케이션 빈으로 관리될 수많은 객체들을 일관된 방법으로 관리할 수 있는 구조를 만든 것이다.

# Controller와 Service의 분리

컨트롤러에서는 웹 클라이언트의 요청을 검증하고, 응답을 만드는데 필요한 비즈니스 로직을 처리하고, 웹 클라이언트의 응답을 만든다. 이때 비즈니스 로직을 실제로 실행하는 부분은 Service로 분리하고 Controller에서 Service에게 요청을 보내 실행하도록 한다. 결과적으로 컨트롤러는 요청 검증과 응답 생성을 하는 책임을 가지고, 비즈니스 로직을 처리할 수 있는 객체를 아는 책임을 가진다. 분리 전보다 더 단순한 책임을 갖는다.

```java
// 인삿말을 만드는 비즈니스 로직
public class SimpleHelloService {
    String sayHello(String name) {
        return "Hello " + name;
    }
}

// 요청값 name의 유효성 검사
// 인삿말 만드는 비즈니스 로직은 서비스에 요청
public class HelloController {
    public String hello(String name) {
        SimpleHelloService service = new SimpleHelloService();
        return service.sayHello(Objects.requireNonNull(name));
    }
}
```

# Dependency Injection

변경에 유리한 구조를 만들기 위해서는 컴파일 시점의 의존성은 변하지 않으면서 런타임 시점의 의존성을 다르게 주입해야 한다. 인터페이스와 같은 역할을 사용하면 컴파일 시점의 의존성이 변하지 않도록 유지할 수 있으나, 실제 런타임 시점의 의존성을 어딘가에서 결정해주어야한다. 이 작업을 의존성 해결이라고 부르는데, 의존성 주입(DI)가 대표적인 의존성 해결 방법이다.

DI는 의존하는 객체를 외부에서 생성하고 주입하는 방법이다. DI를 하려면 객체 생성과 주입을 담당하는 제 3의 존재가 필요한데, 이를 어셈블러이자 스프링 컨테이너라고 한다. 생성자 수정자 메소드를 이용한 DI를 사용할 수 있다.

위의 컨트롤러 코드에서는 서비스를 생성자로 직접 생성해서 의존하고 있다. 스프링 컨테이너를 도입하여 서비스와 컨트롤러를 빈으로 등록하고 의존성을 주입받을 수 있도록 변경해보자.

확장 가능하도록 서비스 클래스가 인터페이스를 구현하도록 하고, 컨트롤러가 서비스의 인터페이스에 의존하도록 코드를 변경했다. 생성자를 통해 외부에서 의존성을 주입받는다.

```java
public interface HelloService {
    String sayHello(String name);
}

public class SimpleHelloService implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}

public class HelloController {
    private final HelloService helloService;

		public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }
		// ..
}
```

이제 의존성을 결정하는 외부를 살펴보자. 프론트 컨트롤러를 띄우기에 앞서 스프링 컨테이너를 얻고 빈을 등록하는 부분이 있었다. 여기에서 HelloController 생성자의 HelloService에 주입되는 구체적인 의존성을 결정한다. SimpleHelloService.class를 등록한다.

```java
public class HellobootApplication {

   public static void main(String[] args) {

      GenericApplicationContext applicationContext = new GenericApplicationContext();
      applicationContext.registerBean(HelloController.class);
      applicationContext.registerBean(SimpleHelloService.class);
      applicationContext.refresh();
```

이때 HelloService.class를 등록하지 않아도 HelloController에서 SimpleHelloService를 잘 찾는다. 스프링 컨테이너가 의존성을 주입할 때 HelloService 타입이 될 수 있는 빈을 찾아 주입하기 때문이다.


# DispatcherServlet 전환

우리의 목표는 서블릿과 관련된 코드 작성 없이 비즈니스 로직만 작성하는 것이다. 현재 서블릿 관련 코드에는 비즈니스 로직과 밀접한 관련이 있는 부분이 있다. 요청과 컨트롤러 매핑, 컨트롤러와 비즈니스 로직의 바인딩 부분이다.

```java
servletContext.addServlet("frontcontroller", new HttpServlet() {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			// 매핑
			if (req.getRequestURI().equals("/hello") && req.getMethod().equals(HttpMethod.GET.name())) {
				String name = req.getParameter("name");
				HelloController helloController = applicationContext.getBean(HelloController.class);
				// 바인딩
				String ret = helloController.hello(name);
```

## 디스패처 서블릿 등록

스프링은 프론트 컨트롤러 역할을 하면서 서블릿 관련 코드와 애플리케이션 빈을 분리하기 위해 DispatcherServlet을 제공한다. 우선 프론트 컨트롤러 역할을 하는 DispatcherServlet을 등록하자. 디스패처 서블릿은 웹 애플ㄹ리케이션 컨텍스트를 사용하므로 타입을 변경한다.

```java
GenericWebApplicationContext applicationContext = new GenericWebApplicationContext(); // 코드로 받은 스프링 컨테이너

WebServer webServer = serverFactory.getWebServer(servletContext -> {
	servletContext.addServlet("dispatcherServlet",
		new DispatcherServlet(applicationContext)
	).addMapping("/*"); // 중앙 처리
});
```

지금 등록한 디스패처 서블릿은 모든 url로 들어오는 요청을 받아들이기만 할 뿐, 어떠한 매핑이나 바인딩 작업을 하지 못하는 상태이다. 어떤 요청이 들어왔을 때 어떤 컨트롤러에게 요청을 전달할 것이고, 컨트롤러는 어떤 비즈니스 로직을 호출해야하는지 그 정보를 어딘가에서 알려주어야 한다.

## 매핑 정보 등록

매핑 정보를 설정하는 방법은 여러가지가 있다. xml로 설정해서 기본 생성자와 수정자 주입을 사용하는 방법이 예전에 사용되던 방식인데, 최근에는 매핑 정보를 요청을 처리할 오브젝트에 애노테이션으로 설정하는 방법이다.

이 예제에서는 HelloController에 매핑 정보를 넣는다.

```java
@RequestMapping
public class HelloController {
    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello")
    public String hello(String name) {
        SimpleHelloService service = new SimpleHelloService();
        return service.sayHello(Objects.requireNonNull(name));
    }
}
```

디스패처 서블릿은 생성자로 받은 애플리케이션 컨텍스트를 뒤져서 웹 요청을 처리하는 애노테이션이 붙은 모든 빈을 찾고 컨트롤러로 인식한다. 이 빈에서 매핑 정보를 가진 메소드를 찾아 매핑 정보를 확인하여 매핑 테이블을 만든다. 그걸 참고해서 요청을 처리할 컨트롤러와 메소드를 찾는다. 이 예제에서는 @GetMapping을 통해 매핑 정보를 전달했다. 클래스 레벨에 붙은 @RequestMapping은 디스패처 서블릿이 매핑 정보를 스캔할 때 이 빈을 스캔하도록 알리는 역할이다. @RequestMapping에서 url 매핑을 설정할 수도 있다.

## 응답 정보 등록

디스패처 서블릿은 기본적으로 컨트롤러 메소드가 String을 반환하면 그 메소드 이름을 가진 뷰 템플릿을 찾아 응답을 만든다. 이번엔 뷰 대신 응답에 문자열을 그대로 보내는 방법을 사용하기 위해 @ResponseBody를 추가한다.

```java
@GetMapping("/hello")
@ResponseBody
public String hello(String name) {
	SimpleHelloService service = new SimpleHelloService();
	return service.sayHello(Objects.requireNonNull(name));
}
```

스프링은 컨트롤러가 매핑과 바인딩 책임에만 충실할 수 있도록 많은 관례를 사용하여 애노테이션 정보를 생략할 수 있다. (별다른 설정이 없다면 응답으로 뷰를 찾아서 반환하는 등등) 그래서 스프링 mvc를 사용할 때는 코드를 봤을 때 디스패처 서블릿이 어떻게 요청을 받아들이고 응답을 만드는지 알 수 있어야 한다.

# 스프링 컨테이너

main을 살펴보면 스프링 컨테이너 초기화와 디스패처 서블릿을 등록하는 서블릿 컨테이너 초기화 코드가 있다.

```java
public class HellobootApplication {

	public static void main(String[] args) {

		GenericWebApplicationContext applicationContext = new GenericWebApplicationContext(); // 코드로 받은 스프링 컨테이너
		applicationContext.registerBean(HelloController.class);
		applicationContext.registerBean(SimpleHelloService.class);
		applicationContext.refresh(); // 구성 정보를 바탕으로 컨테이너 초기화

		ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
		WebServer webServer = serverFactory.getWebServer(servletContext -> {
			servletContext.addServlet("dispatcherServlet",
					new DispatcherServlet(applicationContext)
			).addMapping("/*"); // 중앙 처리
		});

		webServer.start();
	}

}
```

이번에는 서블릿 컨테이너 초기화 코드를 스프링 컨테이너가 초기화되는 과정에서 일어나도록 바꿔보자. 스프링 부트가 그렇게 동작하기 때문이다.

스프링 컨테이너의 초기화 작업은 refresh 메소드에서 일어난다. refresh는 전형적인 템플릿 메소드인데, onRefresh 콜백을 오버라이딩하면 스프링 컨테이너가 초기화되는 중에 부가적으로 수행할 작업을 전달할 수 있다.

# 팩토리 메소드로 구성 정보 제공

스프링 컨테이너는 빈을 생성하고 의존성을 주입한다. 이때 어떤 객체를 빈으로 관리하고 어느 의존성을 주입해줄지 결정하는 것은 개발자의 몫이다. 이 구성 정보를 만들어서 스프링 컨테이너에 전달해야 제대로 동작한다.

예전에는 xml 파일에 빈 태그를 사용해 구성 정보를 만드는 방법을 사용했다. 요즘엔 구성 정보를 하나의 파일로 모아두지 않고 객체에 애노테이션을 붙이는 방법을 사용한다. 이번에는 팩토리 메소드를 작성하여 구성 정보를 만들어본다.

팩토리 메소드는 빈 오브젝트를 생성하고 의존관계를 주입하는 로직을 가지고 있다. 팩토리 메소드는 빈을 생성하고 의존관계에 있는 빈을 주입하여 오브젝트를 리턴한다. 이 오브젝트를 스프링 빈으로 등록하라고 스프링 컨테이너에게 알려준다. 주로 빈 초기화 작업이 복잡한 경우, 설정 파일로 작성하는 것보다 자바 코드를 사용하는 것이 더 직관적일 때 사용한다.

다음과 같은 팩토리 메소드를 작성한다. HelloController는 HelloService에 대한 의존성 주입이 필요한데, 스프링 컨테이너에게 HelloService 타입의 의존성을 매개변수로 주입할 것을 요청한다.

```java
public HelloController helloController(HelloService helloService) {
	return new HelloController(helloService);
}

public HelloService helloService() {
	return new SimpleHelloService();
}
```

추가로 스프링 컨테이너가 빈으로 관리할 빈이 이 팩토리 메소드가 반환하는 오브젝트임을 알리기 위해 @Bean 애노테이션을 사용한다. 그리고 이 클래스가 팩토리 메소드를 가지고 있음을 알리는 @Configuration을 붙인다. 이제 스프링 컨테이너는 @Configuration가 붙은 클래스 안에서 @Bean이 붙은 팩토리 메소드를 스캔해서 빈 오브젝트를 만든다.

```java
@Configuration
public class HellobootApplication {

	@Bean
	public HelloController helloController(HelloService helloService) {
		return new HelloController(helloService);
	}

	@Bean
	public HelloService helloService() {
		return new SimpleHelloService();
	}
```

ㅇ애노테이션을 기반으로 빈을 스캔하는 스프링 컨테이너를 사용하려면 `AnnotationConfigWebApplicationContext` 을 사용해야 한다. 타입을 바꿔준다. 그리고 기존에는 컨테이너에 빈으로 등록되어야하는 클래스를 모두 추가해주었는데, 이제 설정 정보를 담은 클래스만 등록하면 된다. 스프링 컨테이너가 이 파일부터 빈 스캔을 시작한다.

```java
@Configuration
public class HellobootApplication {
	public static void main(String[] args) {
			AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
				// onRefresh()에서	
				// 서블릿 컨테이너 초기화
			};
	
			applicationContext.register(HellobootApplication.class);
	
		}
```

HellobootApplication 클래스는 스프링 컨테에너에 첫번째 빈으로 등록된다. HellobootApplication 안에 전체 애플리케이션을 등록하는데 필요한 정보를 넣어서 빈을 등록할 수 있다. 팩토리 메소드를 정의하여 자바 코드로 빈을 등록하는 방법을 알아보았다.

# Component Scan

스프링 빈을 등록하는 더 간결한 방법을 알아보자. 스프링으로 등록하고 싶은 빈의 클래스에 `@Component` 애노테이션을 붙이는 방식이다. 설정 정보 스캔을 시작하는 위치에 `@Configuration`을 붙일 때 `@ComponentScan`을 추가하면, 스프링 컨테이너가 빈 등록을 위해 스캔할 때 `@Component`가 붙은 모든 빈을 스프링 빈으로 등록한다.

```java
@Component
@RequestMapping
public class HelloController { }

@Component
public class SimpleHelloService implements HelloService { }

@Configuration @ComponentScan
public class HellobootApplication {}
```

## 의존관계 주입 방법 비교 - 설정 파일 vs 애노테이션

컴포넌트 스캔을 사용하는 방법이 관례가 되었다.

xml 설정 파일이나 팩토리 메소드를 사용하는 방법과 비교했을 때, Component Scan은 개별 모듈에 애노테이션 하나만 붙이면 되기 때문에 스프링 빈을 간결하게 등록할 수 있다는 장점이 있다. 하지만 빈의 개수가 엄청나게 많아지면 주입 관계를 파악하기 어렵다는 단점이 있다. 그럼에도 객체지향적으로 모듈화를 잘 해놓았다면 어느 빈이 어디서 등록되는지 파악하는 것이 어렵지 않다.

## @Bean vs @Component

`@Bean` 어노테이션은 **메서드**에 붙여서 해당 메서드가 생성한 객체를 스프링 애플리케이션 컨텍스트에 등록하도록 지정하는 역할을 한다. 주로 외부 라이브러리나 프레임워크에서 제공하는 객체를 스프링 애플리케이션 컨텍스트에 등록하고 싶을 때 사용한다.

`@Component` 어노테이션은 해당 **클래스**를 스프링 컨테이너가 관리하는 빈으로 등록하는 역할을 합니다. `@Component` 어노테이션을 사용하면 해당 클래스를 **직접 생성자를 통해 객체를 생성하여 사용**할 수 있습니다.

## @Component를 확장한 계층형 컴포넌트

`@Component`을 가지고 있는 애노테이션을 붙여도 동일하게 해당 클래스를 빈으로 등록한다. `@Component` 를  메타 애노테이션으로 가지는 애노테이션에는 우리가 알고 있는 `@Service`, `@Controller` 등이 그 예이다. 이 클래스를 스프링 빈으로 등록하면서 특정 계층의 컴포넌트임을 알려주는 주석의 역할을 한다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
**@Component**
public @interface Controller { }

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
**@Component**
public @interface Service { }
```

이번에는 `@Controller`을 메타 애노테이션을 가지고 있는 `@RestController`을 살펴보자.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
**@Controller
@ResponseBody** 
```

RestController는 Controller을 메타 애노테이션으로 가지며, Controller은 Component를 메타 애노테이션으로 가지므로 컴포넌트로 등록된다. 동시에 @ResponseBody를 가지는데, 응답을 만들 때 뷰 리졸버를 찾는 대신 메시지 바디에 값을 작성하는 기능을 가진다. RestController를 붙이면 스프링 컨테이너가 이 클래스를 뒤져서 매핑된 메소드를 찾아 매핑 테이블을 만들기 때문에 기존 컨트롤러에 붙어있던 @RequestMapping이 없어도 잘 동작한다.

```java
@RestController
public class HelloController { }
```

# Bean의 생명주기 메소드

main에 남아있는 서블릿 컨테이너 팩토리와 디스패처 서블릿을 스프링 빈으로 등록해서 스프링 컨테이너가 관리하도록 하면, 유연한 구성이 가능해진다. 서버를 재시작하지 않고도 동적으로 빈을 수정하거나 추가할 수 있다.예를 들어, 스프링 빈으로 등록된 서블릿 컨테이너 팩토리를 사용하면, 동일한 서블릿 컨테이너 인스턴스에서 여러 웹 애플리케이션을 호스팅할 수 있다. 또한, 다른 프로토콜을 사용하거나 서버의 기본 구성을 변경하려면 스프링 빈으로 등록된 웹 서버 객체를 수정해서 사용한다.

이 main 코드에서 ServletWebServerFactory과 DispatcherServlet을 스프링 빈으로 등록해보자.

```java
@Configuration @ComponentScan
public class HellobootApplication {

	public static void main(String[] args) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
			@Override
			protected void onRefresh() {
				super.onRefresh();

				// 서블릿 컨테이너 초기화
				ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
				WebServer webServer = serverFactory.getWebServer(servletContext -> {
					servletContext.addServlet("dispatcherServlet",
							new DispatcherServlet(this)) // applicationContext
					.addMapping("/*"); // 중앙 처리
				});

				webServer.start();
			}
		};

		applicationContext.register(HellobootApplication.class);

	}

}
```

ServletWebServerFactory과 DispatcherServlet은 스프링이 제공하는 클래스이므로 컴포넌트 스캔을 사용할 수 없다. 팩토리 메소드로 만들고 @Bean으로 등록하자. 이때 DispatcherServlet은 애플리케이션 컨텍스트를 필요로 하므로, main 안에서 세터로 주입한다.

```java
@Configuration @ComponentScan
public class HellobootApplication {

	@Bean
	public ServletWebServerFactory servletWebServerFactory() {
		return new TomcatServletWebServerFactory();
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet(); // spring container 필요
	}

	public static void main(String[] args) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
			@Override
			protected void onRefresh() {
				super.onRefresh();

				// 서블릿 컨테이너 초기화
				ServletWebServerFactory serverFactory = this.getBean(ServletWebServerFactory.class);
				DispatcherServlet dispatcherServlet = this.getBean(DispatcherServlet.class);
				dispatcherServlet.setApplicationContext(this); // 생략 가능

				WebServer webServer = serverFactory.getWebServer(servletContext -> {
					servletContext.addServlet("dispatcherServlet", dispatcherServlet) // applicationContext
					.addMapping("/*"); // 중앙 처리
				});

				webServer.start();
			}
		};

		applicationContext.register(HellobootApplication.class);

	}

}
```

그런데 신기하게도 DispatcherServlet에 애플레케이션 컨텍스트를 주입하는 코드를 생략해도 의존관계 주입을 멀쩡하게 수행한다. 이것이 가능한 것은 우리 대신 스프링 컨테이너가 DispatcherServlet에 애플레케이션 컨텍스트를 주입해주기 때문이다. 이것을 이해하려면 빈의 라이프 사이클 메소드를 이해해야한다.

DispatcherServlet이 구현하는 인터페이스 중 ApplicationContextAware을 살펴보면 다음과 같이 ApplicationContext를 주입하는 세터 메소드가 있다. 빈을 컨테이너가 등록하고 관리하는 중에 컨테이너가 관리하는 오브젝트를 빈에 주입하는 라이프 사이클 메소드이다. 디스패처 서블릿이 스프링 컨테이너에 의해 빈으로 등록되는 시점에서, 스프링 컨테이너가 디스패처 서블릿이 ApplicationContextAware 인터페이스를 가지므로 애플리케이션 컨텍스트를 세터로 주입해준 것이다. 애플리케이션 컨텍스트는 자기 자신을 빈으로 등록해서 사용하는 것을 짐작해볼 수 있다.

```java
/**
 * Interface to be implemented by any object that wishes to be notified
 * of the {@link ApplicationContext} that it runs in.
 *
 * <p>Implementing this interface makes sense for example when an object
 * requires access to a set of collaborating beans. Note that configuration
 * via bean references is preferable to implementing this interface just
 * for bean lookup purposes.
 *
 */
public interface ApplicationContextAware extends Aware {

	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

}
```

최종적으로 디스패처 서블릿을 팩토리 메소드에서 애플리케이션 컨텍스트 주입 없이 생성자로 사용하더라도 의존관계 주입이 잘 일어나는 것을 알 수 있다.

# SpringBootApplication

이제 main 메소드에 있는 서블릿 컨테이너와 디스패처 등록 코드를 재사용 가능하도록 해보자. main 로직을 run() 메소드로 추출했고, 이 안에서 달라지는 부분은 스캔을 시작하는 클래스 파일이므로 매개변수로 받도록 변경했다. 이 코드를 `MySpringApplication`로 추출했고 main에서 이 클래스의 run을 호출하도록 했다.

```java
public class MySpringApplication {

    public static void run(Class<?> applicationClass, String... args) {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext() {
            @Override
            protected void onRefresh() {
                super.onRefresh();

                ServletWebServerFactory serverFactory = this.getBean(ServletWebServerFactory.class);
                DispatcherServlet dispatcherServlet = this.getBean(DispatcherServlet.class);

                WebServer webServer = serverFactory.getWebServer(servletContext -> {
                    servletContext.addServlet("dispatcherServlet", dispatcherServlet) // applicationContext
                            .addMapping("/*");
                });

                webServer.start();
            }
        };

        applicationContext.register(applicationClass); // 매번 달라짐
        applicationContext.refresh();
    }
}

@Configuration @ComponentScan
public class HellobootApplication {

	@Bean
	public ServletWebServerFactory servletWebServerFactory() {
		return new TomcatServletWebServerFactory();
	}

	@Bean
	public DispatcherServlet dispatcherServlet() {
		return new DispatcherServlet(); // spring container 필요
	}

	public static void main(String[] args) {
		MySpringApplication.run(HellobootApplication.class, args);
	}

}
```

HellobootApplication의 main을 보면 스프링 이니셜라이저로 만든 코드의 main과 크게 다르지 않다.

```java
@SpringBootApplication
public class HellobootApplication {

	public static void main(String[] args) {
		SpringApplication.run(HellobootApplication.class, args);
	}

}
```

물론 빈 등록이나 클래스에 붙은 애노테이션은 다르지만, 스프링 부트의 SpringApplication가 서블릿 컨테이너와 디스패처 서블릿을 등록하는 등의 서블릿 관련 작업을 포장해서 제공하는 것을 알 수 있다.

이로써 스프링 부트가 standalone으로 서블릿 컨테이너를 포함하는 스프링 애플리케이션을 동작시키는 원리를 살펴보았다.
