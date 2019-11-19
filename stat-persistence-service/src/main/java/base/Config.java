package base;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Profile("cloud")
public class Config {

    @Bean
    public ScheduledExecutorService makeScheduledExecutor() {
        return Executors.newScheduledThreadPool(1);
    }

}
