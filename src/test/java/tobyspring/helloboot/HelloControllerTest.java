package tobyspring.helloboot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
