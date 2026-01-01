package bpmn.com.bpmn.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Railway DATABASE_URL environment variable'ını parse edip
 * Spring Boot datasource ayarlarına çevirir.
 * ApplicationContext başlamadan önce çalışır.
 */
public class RailwayDatabaseConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                // Railway DATABASE_URL format: postgresql://user:password@host:port/database
                URI dbUri = new URI(databaseUrl.replace("postgresql://", "http://"));
                String userInfo = dbUri.getUserInfo();
                
                if (userInfo != null && userInfo.contains(":")) {
                    String username = userInfo.split(":")[0];
                    String password = userInfo.split(":")[1];
                    String host = dbUri.getHost();
                    int port = dbUri.getPort();
                    String database = dbUri.getPath().replaceFirst("/", "");
                    
                    String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                    
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("spring.datasource.url", jdbcUrl);
                    properties.put("spring.datasource.username", username);
                    properties.put("spring.datasource.password", password);
                    
                    environment.getPropertySources().addFirst(
                        new MapPropertySource("railway-database", properties)
                    );
                    
                    System.out.println("✅ Railway DATABASE_URL parsed successfully");
                    System.out.println("   JDBC URL: " + jdbcUrl);
                }
            } catch (Exception e) {
                System.err.println("⚠️  DATABASE_URL parse hatası: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️  DATABASE_URL environment variable bulunamadı, default ayarlar kullanılacak");
        }
    }
}

