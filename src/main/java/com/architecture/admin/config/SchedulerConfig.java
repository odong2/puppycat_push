package com.architecture.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        // 스케줄러 쓰레드 풀 사이즈
        int poolSize = 10;
        threadPoolTaskScheduler.setPoolSize(poolSize);
        threadPoolTaskScheduler.setThreadNamePrefix("myThread");
        threadPoolTaskScheduler.initialize(); //Set up the ExecutorService.

        //생성한 Thread pool을 작업용으로 등록
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }
}
