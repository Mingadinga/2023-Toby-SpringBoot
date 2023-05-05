package tobyspring.helloboot;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(SpringExtension.class) // 스프링 컨테이너 사용
@ContextConfiguration(classes = HellobootApplication.class) // 빈 로딩 정보
@TestPropertySource("classpath:/application.properties") // 테스트에서의 바인딩 작업 (보통 빈 생성이 끝난 후에 일어남)
@Transactional
public @interface HelloBootTest {
}
