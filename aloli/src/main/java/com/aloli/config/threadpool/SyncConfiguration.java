package com.aloli.config.threadpool;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;



@Configuration
@EnableAsync  // 开启异步任务支持
public class SyncConfiguration {
    @Bean(name="syncPoolTaskExecutor")
    public ThreadPoolTaskExecutor syncPool() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数
        taskExecutor.setCorePoolSize(12);
        //线程池维护线程的最大数量  只有在缓冲队列满了之后才会申请超过核心线程数的数量
        taskExecutor.setMaxPoolSize(100);
        //缓存队列
        taskExecutor.setQueueCapacity(50);
        //允许的空闲时间,当超过了核心线程数之外的线程在空显时间到达后都会被销毁
        taskExecutor.setKeepAliveSeconds(200);
        //异步方法内部线程名称
        taskExecutor.setThreadNamePrefix("sync--");
        //当线程池的任务缓存已满并且线程池中的线程数目达到maximumPoolSize   如果还有任务到来就会采取 任务拒绝策略
        //通常有以下四种策略
        //ThreadPoolExecutor.AbortPolicy: 丢弃任务并抛出RejectdExecutionException 异常
        //ThreadPoolExecutor.DiscardPolicy: 丢弃任务 不抛出异常
        //ThreadPoolExecutor.DiscardOldestPolicy 丢弃队列最前面的任务 然后重新执行任务 重复此过程
        //ThreadPoolExecutor.CallerRunsPolicy  重试添加当前任务  自动重复调用execute方法 直到成功为止
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }


}
