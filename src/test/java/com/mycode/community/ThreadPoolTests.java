package com.mycode.community;

import com.mycode.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    /**
     *  在使用线程去完成某些功能时，最好通过log去记录一些内容
     *  因为在log输出内容时，会带上线程id、时间等信息，让信息更加完整
     */
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);


    // 实例化 JDK普通线程池 ExecutorService
    // jdk的线程池都是通过一个工厂来实例化的：Executors
    // 该线程池反复复用5个线程
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // 实例化 JDK可执行定时任务的线程池 SchedulerExecutorService
    private ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(5);

    // sprin会自动帮我们将线程池被初始化好，并将初始化好的线程池放入spring容器中
    // 我们使用spring线程池只需要注入线程池即可

    // Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    /**
     *  注意：在test方法中的线程是和main方法线程不一样的，
     *      在main方法中，启动了一个线程，如果这个线程挂了，mian会等待线程执行完，不会立刻结束
     *
     *      但是在test的方法中，启用了一个线程，由于启动的子线程和当前的线程时并发的，
     *      test方法中，若是该线程执行时后面没有逻辑需要执行了，这个方法就会立刻结束，不会管启用的线程的任务有没有完成
     *
     *      如何解决？
     *          在test的方法，启用完线程后，等待该线程任务执行完，需要将主线程（当前线程）sleep，阻塞一会
     *  封装sleep方法
     *      每次调用sleep方法，让当前主线程sleep
     */
    private void sleep (long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *  1、JDK普通线程池-executorService
     */
    @Test
    public void testExecutorService () {
        // 线程池-需要给一个任务，让他去执行，他会分配一个线程来执行这个任务
        // 而这个任务通常就是线程体，
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 执行的具体的任务的逻辑
                logger.debug("Hello ExecutorService");
            }
        };

        // 使用线程池去运行上面的任务
        for (int i = 0; i < 10; i++) {
            // 调用submit方法，该线程池就会分配一个线程去执行这个线程体
            executorService.submit(task);
        }

        // 让当前主线程进入阻塞，防止test线程直接不等待就结束
        sleep(10000);
    }


    /**
     *  2、JDK定时任务线程池-SchedulerExecutorService
     */
    @Test
    public void testSchedulerExecutorService () {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 执行的具体的任务的逻辑
                logger.debug("Hello SchedulerExecutorService");
            }
        };

        // scheduleAtFixedRate(任务，延迟多少时间再执行该任务，开始执行后时间间隔，时间的单位):以固定的频率去执行，可多次执行
        // scheduleWithFixedDelay:以固定的延迟去执行，只能执行一次，执行完就结束任务，可设定时间，推迟多长时间再执行
        scheduledService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);

        sleep(30000);

    }


    /**
     *  3、Spring普通线程池-threadPoolTaskExecutor
     *      相对于jdk自带的普通线程池，spring的普通线程池可配置核心线程数、最大线程数、任务队列大小
     *      很大程度的提高了线程池的效率和性能，而且使用简单，更灵活
     */
    @Test
    public void testThreadPoolTaskExecutor () {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 执行的具体的任务的逻辑
                logger.debug("Hello ThreadPoolTaskExecutor");
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }

        sleep(10000);
    }


    /**
     *  4、Spring定时任务线程池-ThreadPoolTaskScheduler
     */
    @Test
    public void testThreadPoolTaskScheduler () {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                // 执行的具体的任务的逻辑
                logger.debug("Hello ThreadPoolTaskScheduler");
            }
        };

        // 当前时间的10秒后
        Date startDate = new Date(System.currentTimeMillis() + 10000);
        // 与jdk自带的方法，在参数上有区别，第二个参数是任务的延迟时间，是一个Date
        // 默认时间单位是毫秒
        taskScheduler.scheduleAtFixedRate(task, startDate, 1000);

        sleep(30000);

    }


    /**
     *  spring的这两个线程池，在使用时，有更简便的调用方式
     *      ：在任意的bean里声明的个方法，该什么逻辑就是什么逻辑，只要在方法之上添加注解，
     *      这个方法就可以在spring线程池的环境下运行，
     *      或者说，在bean中写的方法就可以作为一个线程体，一个任务
     *
     *      见：alphaService
     */

    /**
     *  5、spring普通线程池的简化方式
     */
    @Test
    public void testThreadPoolTaskExecutorSimple () {

        for (int i = 0; i < 10; i++) {
            // spring底层，会以多线程的方式去调用该方法，该方法被当做线程体被执行
            alphaService.execute1();
        }

        sleep(10000);
    }

    /**
     *  6、spring定时任务线程池的简化方式
     */
    @Test
    public void testThreadPoolTaskSchedulerSimple () {
        // 只需要让程序启动起来，execute2方法会自动被调用，只要有程序在跑，就会自动被执行
        // alphaService.execute2();
        sleep(30000);

    }

}
