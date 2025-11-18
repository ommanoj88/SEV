package com.evfleet.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Event Configuration for Inter-Module Communication
 *
 * Configures async event processing for domain events.
 * Events are processed asynchronously to prevent blocking and improve performance.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Configuration
@Slf4j
public class EventConfig implements AsyncConfigurer {

    /**
     * Configure async event multicaster
     * Events are published asynchronously by default
     */
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        log.info("Configured async event multicaster for domain events");
        return eventMulticaster;
    }

    /**
     * Configure async executor for @Async methods
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event-async-");
        executor.initialize();
        log.info("Configured async executor for event processing");
        return executor;
    }

    /**
     * Handle uncaught exceptions in async event listeners
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Uncaught exception in async event listener: {} - Method: {}",
                    throwable.getMessage(), method.getName(), throwable);
        };
    }
}
