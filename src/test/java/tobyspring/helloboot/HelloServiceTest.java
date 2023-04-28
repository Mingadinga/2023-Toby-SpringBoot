package tobyspring.helloboot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloServiceTest {

    @Test
    public void simpleHelloService() {
        SimpleHelloService helloService = new SimpleHelloService();
        String res = helloService.sayHello("Test");
        assertThat(res).isEqualTo("Hello Test");
    }

    @Test
    public void helloDecorator() {
        HelloDecorator decorator = new HelloDecorator(name -> name);
        String ret = decorator.sayHello("Test");
        assertThat(ret).isEqualTo("*Test*");
    }

}
