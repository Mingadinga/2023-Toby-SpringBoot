package tobyspring.helloboot;

import java.util.Objects;

public class HelloController {
    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    public String hello(String name) {
        SimpleHelloService service = new SimpleHelloService();
        return service.sayHello(Objects.requireNonNull(name));
    }
}
