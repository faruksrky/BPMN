package bpmn.com.bpmn.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            // Example: Add custom headers or authentication tokens here
            template.header("Content-Type", "application/json");
        };
    }


}
