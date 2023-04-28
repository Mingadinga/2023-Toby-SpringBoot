# 테스트 코드를 이용한 테스트

지금까지는 수동 테스트를 사용하여 기능이 제대로 동작하는지 확인했다. 수동 테스트는 서버를 직접 띄우고 웹 브라우저나 http 요청 커맨드로 기능을 트리거해서 결과를 직접 눈으로 확인하고 성공 여부를 판단했다. 수동 테스트는 기능 확인 절차가 복잡해지면 그 절차를 개발자가 직접 수행해야하므로 번거롭다. 또한 결과를 읽어보고 성공 여부를 판단해야하므로 번거롭고 실수할 가능성도 있다. 그래서 보통은 테스트 코드를 작성하여 기능을 테스트하는 자동 테스트를 사용한다.

우리가 작성한 hello api가 잘 동작하는지 확인하기 위해 테스트 코드를 작성하자. hello api 요청을 코드로 보내기 위해 TestRestTemplate을 사용하고, 받아온 결과를 assertj로 판단한다. 서버를 띄우고, api 테스트를 실행시킨다.

```java
public class HelloApiTest {

    @Test
    public void helloApi() {
        // http://localhost:8080/hello?name=Spring
        TestRestTemplate rest = new TestRestTemplate();
        ResponseEntity<String> res = rest.getForEntity("http://localhost:8080/hello?name={name}", String.class, "Spring");

        // status code 200
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        // header content-type text/plain
        assertThat(res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).startsWith(MediaType.TEXT_PLAIN_VALUE);

        // body Hello Spring
        assertThat(res.getBody()).isEqualTo("Hello Spring");

    }

}
```

# DI와 단위 테스트

api 테스트는 서블릿 컨테이너의 디스패처 서블릿을 거쳐 url로 매핑되고, 파라미터 검증, 응답 생성이 일어나는 컨트롤러와 비즈니스 로직을 처리하는 서비스가 모두 참여한다. 테스트가 실패한다면 테스트 실패 원인을 바로 찾기도 어렵고, 특정 컴포넌트만 테스트하고 싶은데 시간이 오래 걸리고 리소스도 많이 사용한다. 컨트롤러나 서비스는 일부 애노테이션을 제외하면 일반적인 자바 객체와 같다. 그러므로 해당 객체의 기능만 테스트할 수 있도록 단위 테스트를 작성한다.

인삿말을 만들어주는 SimpleHelloService의 기능만 테스트해보자. 서비스의 기능에만 집중할 수 있고, 테스트가 빠르게 실행되므로 부담없이 여러번 실행할 수 있다.

```java
public class HelloServiceTest {
    @Test
    public void simpleHelloService() {
        SimpleHelloService helloService = new SimpleHelloService();
        String res = helloService.sayHello("Test");
        assertThat(res).isEqualTo("Hello Test");
    }
}
```

이번엔 컨트롤러에 대한 단위 테스트를 만들어보자. 컨트롤러는 응답 파라미터 요청, 응답 생성과 함께 서비스를 호출한다. 이때 테스트 관심 대상은 컨트롤러의 기능이므로 서비스의 기능 문제가 컨트롤러 테스트에 영향을 미치지 않도록 테스트 스텁을 사용할 수 있다. HelloController의 생성자에 HelloService를 람다로 구현해 넘겨주면 된다.

```java
public class HelloControllerTest {
    
    @Test
    public void helloController() {
        HelloController helloController = new HelloController(name -> name); // 고립 테스트 - 서비스 테스트 스텁
        String ret = helloController.hello("Test");
        assertThat(ret).isEqualTo("Test");
    }

    @Test
    public void failsHelloController() {
        HelloController helloController = new HelloController(name -> name);
        Assertions.assertThatThrownBy(() -> {
            String ret = helloController.hello(null);
        }).isInstanceOf(IllegalArgumentException.class);

        Assertions.assertThatThrownBy(() -> {
            String ret = helloController.hello("");
        }).isInstanceOf(IllegalArgumentException.class);
    }

}
```

이렇게 테스트 스텁을 사용해 외부 환경에 의존하지 않는 테스트를 만들 수 있다. 고립 테스트에서 사용하는 테스트 스텁은 런타임 의존성 정해지므로 의존성 주입의 원리를 사용했다고 본다. 자동 DI는 스프링 컨테이너가 의존성을 주입해주지만, 수동 DI는 개발자가 직접 의존성을 주입해주는 방식이다. 테스트 스텁을 사용하는 것은 테스트에서 사용되는 수동 DI이다.