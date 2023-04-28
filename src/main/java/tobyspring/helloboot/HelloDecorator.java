package tobyspring.helloboot;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary // HelloService 타입이 두개 이상일 때 이 빈을 주입해라
public class HelloDecorator implements HelloService {

    private final HelloService target;

    public HelloDecorator(HelloService target) {
        this.target = target;
    }

    @Override
    public String sayHello(String name) {
        return "*" + target.sayHello(name) + "*";
    }
}
