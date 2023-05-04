package tobyspring.config.autoconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import tobyspring.config.MyAutoConfiguration;

@MyAutoConfiguration
public class PropertyPlaceHolderConfig {
    @Bean
    PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer(); // 추상화된 env로부터 치환자의 값을 지정
    }
}
