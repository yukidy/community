package com.mycode.community.config;

import com.mycode.community.quartz.AlphaJob;
import com.mycode.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 第一次被使用将配置信息初始化到 -> 数据库
// 此后，每次使用Quartz时都是从数据中 -> 调用
@Configuration
public class QuartzConfig {


    /**
     *  在spring中有很多地方都会有FactoryBean出现，
     *  这个FactoryBean和spring ioc的beanFactory有本质的区别。
     *  beanFactory是整个ioc容器的顶层接口
     *  FactoryBean和beanFactory是两码事：
     *      FactoryBean作用：可简化bean的实例化过程
     *              有些bean的实例化过程会很麻烦，但使用FactoryBean之后，可以简化这些实例化过程
     *              例如：JobDetailFactoryBean底层封装着JobDetail详细的实例化过程，然后对其实例化做了简化
     *        1、通过FactoryBean封装bean的实例化过程
     *        2、将FactoryBean装配到spring容器里
     *        3、将FactoryBean注入给其他的bean
     *        4、这些bean锁得到的是FactoryBean所管理的对象实例。
     */


    /**
     *  配置JobDetail和Trigger
     */

    /**
     * 配置JobDetail
     * @return
     */
    //@Bean
    public JobDetailFactoryBean alphaJobDetail () {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 声明需要管理的bean
        factoryBean.setJobClass(AlphaJob.class);
        // 声明这个Job的名字是什么，给任务取名
        factoryBean.setName("alphaJob");
        // 分组，多个任务可以同属一个组
        factoryBean.setGroup("alphaJobGroup");
        // 声明该任务是否长久、持久保存（哪怕将来没有了触发器，任务已经没有了，不再运行了，也会保留这个任务）
        factoryBean.setDurability(true);
        // 声明该任务是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    /**
     * 配置Trigger(SimpleTriggerFactoryBean简单的trigger,CronTriggerFactoryBean复杂的trigger)
     *      在初始化Trigger时，是依赖于JobDetail的，我们需要将JobDetail注入给Trigger使用
     * @return
     */
    //@Bean                                       // 接收注入进来的factoryBean中管理的JobDetail实例,这些实例是通过名字区分的
    public SimpleTriggerFactoryBean alphaTrigger (JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 注入alphaJobDetail
        factoryBean.setJobDetail(alphaJobDetail);
        // 声明trigger的名字
        factoryBean.setName("alphaTrigger");
        // 声明Trigger的组
        factoryBean.setGroup("alphaTriggerGroup");
        // 执行频率，多次时间执行一次任务
        factoryBean.setRepeatInterval(3000);
        // trigger底层需要存储job的一些状态，指定对象存储这些状态
        // 这里初始化了一个默认的类型来存储
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    /**
     * 刷新帖子分数的JobDetail
     * @return
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail () {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 声明需要管理的bean
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        // 声明这个Job的名字是什么，给任务取名
        factoryBean.setName("postScoreRefreshJob");
        // 分组，多个任务可以同属一个组
        factoryBean.setGroup("communityJobGroup");
        // 声明该任务是否长久、持久保存（哪怕将来没有了触发器，任务已经没有了，不再运行了，也会保留这个任务）
        factoryBean.setDurability(true);
        // 声明该任务是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    /**
     * 刷新帖子分数的Trigger
     * @return
     */
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger (JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        // 注入alphaJobDetail
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        // 声明trigger的名字
        factoryBean.setName("postScoreRefreshTrigger");
        // 声明Trigger的组
        factoryBean.setGroup("communityJobGroup");
        // 执行频率，多次时间执行一次任务 5分钟执行一次
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        // trigger底层需要存储job的一些状态，指定对象存储这些状态
        // 这里初始化了一个默认的类型来存储
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
