package bpmn.com.bpmn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@EnableWebSecurity
@Configuration
public class WebConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
            var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
            corsConfiguration.setAllowedOriginPatterns(List.of(
                    "http://localhost:3031",
                    "http://localhost:5173",
                    "https://psikohekimfrontend.pages.dev",
                    "https://*.psikohekimfrontend.pages.dev",
                    "https://*.iyihislerapp.com",
                    "https://iyihislerapp.com",
                    "https://www.iyihislerapp.com"
            ));
            corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            corsConfiguration.setAllowedHeaders(List.of("*"));
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        }));


        http.csrf(csrf -> csrf.disable());

        // Tüm isteklere izin ver
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll());

        // Oturum yönetimi
        http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

