package tobyspring.helloboot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// db 초기화 루틴을 돌리기 위해 작성한 스프링 빈을 컨테이너로 로딩.
// 단 이 테스트를 진행할 때 웹 환경은 필요하지 않다
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class HelloRepositoryTest {
    @Autowired HelloRepository helloRepository;

    @Test
    void findHelloFailed() {
        Assertions.assertThat(helloRepository.findHello("Toby")).isNull();
    }

    @Test
    void increaseCount() {
        Assertions.assertThat(helloRepository.countOf("Toby")).isEqualTo(0);

        helloRepository.increaseCount("Toby");
        Assertions.assertThat(helloRepository.countOf("Toby")).isEqualTo(1);

        helloRepository.increaseCount("Toby");
        Assertions.assertThat(helloRepository.countOf("Toby")).isEqualTo(2);

    }

}
