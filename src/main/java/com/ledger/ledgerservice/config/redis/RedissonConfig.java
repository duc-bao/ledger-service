package com.ledger.ledgerservice.config.redis;

import com.ledger.ledgerservice.config.properties.RedissonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(RedissonProperties redissonProperties) {
        Config config = new Config();
        config.useSingleServer().setAddress(
                        "redis://" + redissonProperties.getHost() + ":" + redissonProperties.getPort() + "/"
                ).setDatabase(redissonProperties.getDatabase())
                .setTimeout(redissonProperties.getTimeout());
        if (redissonProperties.getPassword() != null && !redissonProperties.getPassword().isEmpty()) {
            config.useSingleServer().setPassword(redissonProperties.getPassword());
        }
        return Redisson.create(config);
    }
}
