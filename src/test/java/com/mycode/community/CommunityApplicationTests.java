package com.mycode.community;

import com.mycode.community.config.AlphaConfig;
import com.mycode.community.dao.AlphaDao;
import com.mycode.community.dao.UserMapper;
import com.mycode.community.entity.User;
import com.mycode.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)	//在测试代码中也希望与正式代码中的配置类是一样的，所以使用
class CommunityApplicationTests implements ApplicationContextAware {
	//而ioc容器是被自动创建的，测试类中哪个类想得到容器，则实现ApplicationContextAware接口

	//记住容器变量
	private ApplicationContext applicationContext;

	//需要实现的方法，ApplicationContext applicationContext就是这个容器，ApplicationContext的顶层是BeanFactory
	//这是我们可以暂存和使用这个容器
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		//自动做记录，而后可使用这个容器
		this.applicationContext = applicationContext;
	}

	/**
	 * 通过ioc实例化的好处：
	 * 	例子：当项目处在某一进展时，当我们发现使用mybatis比hibernate好用时，需要替换，这时我们则不需要
	 * 		删掉原来的，只需要再添加使用mybatis的方法的bean，实现相应接口方法即可，我们调用的地方完全不用改变
	 * 		（这时我们回发现AlphaDao.class无法识别使用哪一方法类，此时只需要再@Repoistory下添加@Primary注解表明优先调用）
	 * 		（见AlphaDaoMybatisImpl）
	 */
	@Test
	public void testApplicationContext () {
		//证明容器是存在的
		System.out.println(applicationContext);
		//如何使用容器管理bean
		//通过容器获取对象，可通过对象类型，也可通过对象名，一般使用对象类型
		AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
		System.out.println(alphaDao.select());

		//接着上诉例子，若是使用@Primary后，在某个特定场景自需要用到hibernate
		//解决方法：可通过bean的名字强制使用
		alphaDao = applicationContext.getBean("alphaHibernate", AlphaDao.class);
		System.out.println(alphaDao.select());
	}

	@Test
	public void testBeanManagement () {
		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);

		alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
	}

	@Test
	public void testBeanConfig () {
		//实例化SimpleDateFormat方法
		SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}

	//以上方法帮助了解ioc的运作方式
	//-------------------------------------------------


	/**
	 * 如何方便使用，什么叫依赖注入，基本的使用方式
	 */

	//spring容器能够把AlphaDap注入给alphaDao这个属性，那么就可以直接使用该属性即可
	@Autowired	//依赖注入
	private AlphaDao alphaDao;

	//如果希望AlphaDao注入的不是mybatis优先级，而是希望hibernate
	//使用@Qualifier("alphaHibernate") 添加名字
	@Autowired
	@Qualifier("alphaHibernate")
	private AlphaDao alphaDao1;

	@Autowired
	private AlphaService alphaService;

	@Autowired
	private AlphaConfig alphaConfig;

	@Test
	public void testDI () {	//测试依赖注入
		System.out.println(alphaDao);
		System.out.println(alphaDao.select());
		System.out.println(alphaDao1.select());
		System.out.println(alphaService);
		System.out.println(alphaConfig);
	}

}
