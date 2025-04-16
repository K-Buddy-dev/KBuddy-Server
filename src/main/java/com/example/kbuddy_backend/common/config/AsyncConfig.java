package com.example.kbuddy_backend.common.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailAsync")
    public Executor mailAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 항상 활성화되어 있는 최소 쓰레드 갯수
        executor.setMaxPoolSize(25); // 스케일 업 할 수 있는 최대 쓰레드 갯수
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("MailAsync-");
        executor.setKeepAliveSeconds(60); // idle 상태여도 60초 동안은 유지
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 작업 손실 방지
        executor.initialize();
        return executor;
    }
}