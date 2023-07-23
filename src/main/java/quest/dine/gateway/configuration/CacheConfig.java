package quest.dine.gateway.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quest.dine.gateway.services.ConsulKV;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Autowired
    private ConsulKV consulKV;

    @Value("${dine.quest.isProduction}")
    private boolean isProduction;

    @Bean
    public CacheManager cacheManager() {
        String isConsulProduction = consulKV.getKeyValue("isProduction");

        if (isConsulProduction == null) {
            if (!isProduction) {
                consulKV.setKeyValue("isProduction", "false");
            }
        }

        if (isConsulProduction != null) {
            if (Boolean.parseBoolean(isConsulProduction)) {

                CaffeineCacheManager cacheManager = new CaffeineCacheManager();
                cacheManager.setCaffeine(Caffeine.newBuilder()
                        .initialCapacity(100)
                        .maximumSize(500)
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .weakKeys()
                        .recordStats());
                return cacheManager;
            }
        }

        return new SimpleCacheManager();
    }
}
