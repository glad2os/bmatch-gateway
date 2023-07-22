package quest.dine.gateway.configuration;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsulConfig {

    @Value("${spring.cloud.consul.host}")
    private String consulHost;

    @Value("${spring.cloud.consul.port}")
    private int consulPort;

    @Bean
    public ConsulClient consulClient() {
        return new ConsulClient(consulHost, consulPort);
    }
}
