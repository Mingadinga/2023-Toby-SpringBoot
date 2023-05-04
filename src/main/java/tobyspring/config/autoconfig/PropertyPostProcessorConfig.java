package tobyspring.config.autoconfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import tobyspring.config.MyAutoConfiguration;
import tobyspring.config.MyConfigurationProperties;

import java.util.Map;

@MyAutoConfiguration
public class PropertyPostProcessorConfig {
    @Bean
    BeanPostProcessor propertyPostProcessor(Environment environment) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // MyConfigurationProperties이 붙어있으면
                MyConfigurationProperties annotation = AnnotationUtils.findAnnotation(bean.getClass(), MyConfigurationProperties.class);
                if (annotation == null) return bean; // 애노테이션 없으면 그냥 반환

                // prefix 확인
                Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
                String prefix = (String) attributes.get("prefix");

                // prefix로 바인딩
                return Binder.get(environment).bindOrCreate(prefix, bean.getClass());
            }
        };
    }
}
