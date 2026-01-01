package bpmn.com.bpmn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.net.URI;

@Configuration
public class RailwayDatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @EventListener(ApplicationReadyEvent.class)
    public void parseRailwayDatabaseUrl() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                // Railway DATABASE_URL format: postgresql://user:password@host:port/database
                URI dbUri = new URI(databaseUrl.replace("postgresql://", "http://"));
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String database = dbUri.getPath().replaceFirst("/", "");
                
                String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
                
                // System properties olarak set et (Spring Boot bunları kullanabilir)
                System.setProperty("spring.datasource.url", jdbcUrl);
                System.setProperty("spring.datasource.username", username);
                System.setProperty("spring.datasource.password", password);
                
                System.out.println("✅ Railway DATABASE_URL parsed successfully");
                System.out.println("   JDBC URL: jdbc:postgresql://" + host + ":" + port + "/" + database);
            } catch (Exception e) {
                System.err.println("⚠️  DATABASE_URL parse hatası: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️  DATABASE_URL environment variable bulunamadı, default ayarlar kullanılacak");
        }
    }
}

