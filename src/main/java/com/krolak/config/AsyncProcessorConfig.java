package com.krolak.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class AsyncProcessorConfig {
    @Value("${app.async-processor.executor.pool.size}")
    private int poolSize;

    @Bean(name = "asyncProcessorExecutor", destroyMethod = "destroy")
    public ThreadPoolTaskExecutor asyncProcessorExecutor() {
        log.info("asyncProcessorExecutor pool size: {}", poolSize);

        var result = new ThreadPoolTaskExecutor();

        result.setCorePoolSize(poolSize);
        result.setMaxPoolSize(poolSize);
        result.setThreadNamePrefix("AsyncProcessorExecutor-");
        result.initialize();

        return result;
    }
}