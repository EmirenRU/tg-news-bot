package ru.emiren.tg_news.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
@EnableTransactionManagement
public class AsyncConfig {
    private final Environment env;

    public AsyncConfig(Environment env) {
        this.env = env;
    }

    @Bean("asyncTaskExecutor")
    @Primary
    public Executor asyncTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Integer.parseInt(Objects.requireNonNull(env.getProperty("number.of.threads"))));
        executor.setQueueCapacity(150);
        executor.setMaxPoolSize(Integer.parseInt(Objects.requireNonNull(env.getProperty("number.of.threads"))));
        executor.setThreadNamePrefix("Async-Executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }


}
