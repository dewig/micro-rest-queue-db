package base;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Optional;
import java.util.function.Supplier;

@Configuration
@Profile("cloud")
public class Config extends AbstractCloudConfig {

    @Bean
    public ConnectionFactory rabbitConnectionFactory() {
        return connectionFactory().rabbitConnectionFactory();
    }

    @Bean
    public Supplier<Optional> makeCurrentTimeProducer() {
        return () -> Optional.of(System.currentTimeMillis());
    }

}
