package com.ledger.ledgerservice.config.thread;

import com.ledger.ledgerservice.config.properties.ThreadPoolProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
public class ThreadPoolExecutorConfig {
    private final ThreadPoolProperty poolProp;

    @Bean
    public Executor requestLogExecutor() {
        ThreadPoolProperty.RequestLogProperty property = poolProp.getRequestLog();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(property.getPoolCoreSize());
        executor.setMaxPoolSize(property.getPoolMaxSize());
        executor.setQueueCapacity(property.getQueueCapacity());
        executor.setThreadNamePrefix(property.getThreadNamePrefix());
        executor.setKeepAliveSeconds(property.getKeepAliveSeconds());
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // TODO tìm hiểu thêm

        executor.initialize();
        return executor;
    }

    @Bean
    public Executor businessLogExecutor() {
        ThreadPoolProperty.BusinessLogProperty property = poolProp.getBusinessLog();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(property.getPoolCoreSize());
        executor.setMaxPoolSize(property.getPoolMaxSize());
        executor.setQueueCapacity(property.getQueueCapacity());
        executor.setThreadNamePrefix(property.getThreadNamePrefix());
        executor.setKeepAliveSeconds(property.getKeepAliveSeconds());
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // TODO tìm hiểu thêm

        executor.initialize();
        return executor;
    }

    @Bean
    public Executor statisticsExecutor() {
        ThreadPoolProperty.StatisticsProperty property = poolProp.getStatistics();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(property.getPoolCoreSize());
        executor.setMaxPoolSize(property.getPoolMaxSize());
        executor.setQueueCapacity(property.getQueueCapacity());
        executor.setThreadNamePrefix(property.getThreadNamePrefix());
        executor.setKeepAliveSeconds(property.getKeepAliveSeconds());
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // TODO tìm hiểu thêm

        executor.initialize();
        return executor;
    }
}
