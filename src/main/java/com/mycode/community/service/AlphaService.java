package com.mycode.community.service;

import com.mycode.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
//@Scope("prototype") //每次调用会产生不同的实例
public class AlphaService {

    //实现依赖注入，service依赖于dao
    @Autowired
    private AlphaDao alphaDao;

    //构造器
    public AlphaService () {
        System.out.println("实例化AlphaService");
    }

    // 当容器实例化这个Bean后，在调用完构造器之后，该方法自动被调用
    // bean合适被初始化？ 在服务启动时被初始化，所以在服务器启动时，该方法就会被调用
    //要想让容器在适当的时候自动的调用这个方法，添加@PostConstruct注解
    @PostConstruct  //表明该方法会在构造器之后调用
    public void init () {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy  //表明在bean销毁之前去调用它
    public void destory () {
        //可在此释放某些资源
        System.out.println("销毁AlphaService");
    }

    public String find () {
        return alphaDao.select();
    }
}
