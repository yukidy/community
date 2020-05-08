package com.mycode.community.controller;

import com.mycode.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData () {
        return alphaService.find();
    }

    /**
     * 在spring mvc框架下，如何获得请求对象，如何获得响应对象
     * 请求对象怎么样封装处理请求数据，响应对象怎么样封装处理响应数据
     * 而spring mvc对这个比较底层的过程做了一定的封装，使得更加简便
     */

    //如何获得请求对象，如何获得响应对象，底层的过程
    //演示直接使用底层的对象，对底层对象直观的了解
    @RequestMapping("/http")
    public void http (HttpServletRequest request, HttpServletResponse response) {

        //获取请求数据 request
        //这两行其实是请求行，请求的第一行的数据
        System.out.println(request.getMethod()); //获取请求方法
        System.out.println(request.getServletPath()); //获取请求路径
        //枚举器，是一个接口，不是类，与Iterator迭代器相似，当前该枚举不推荐使用
        //虽然枚举器可能过时而被迭代器所取代，但是很多servlet还用到
        /**
         * 1)Iterator和Enumeration的区别
         * Iterator是用来替代Enumeration的,Enumeration中只定义了两个方法,
         * 具备判断是否含有下一元素，返回下一元素，不具备删除功能.
         * 2)调用next()方法后才可以调用remove()方法,而且每次调用next()后最多只能调用一次remove()方法,
         * 否则抛出IllegalStateException异常.
         */
        //这里其实是请求的消息头，若干行
        Enumeration<String> enumeration = request.getHeaderNames(); //获取所有请求行的key,value
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ":" + value);
        }
        //请求的消息体
        System.out.println(request.getParameter("code"));


        //返回响应数据 response
        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("<h1>my community</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * spring mvc对这个比较底层的过程做了一定的封装，使得更加简便
     * 下面为spring mvc封装之后，更加简便的处理请求的方式是如何做的
     */
    /**
     *  //处理浏览器的请求分两个方面：接收请求的数据（基于request），返回响应数据（基于response）
     */
    ////接收请求的数据演示

    //GET请求，一般用于向服务器获取某些数据，默认发送的请求就是GET请求
    /**
     *有两种存参方式：
     * 1、/students?current=1&limit=20，将参数通过？拼到路径
     * 2、/student/123把参数拼到路径里
     */

    //方式一：eg.  /students?current=1&limit=20
    //该例子注重请求，所以这边响应用简单@ResponseBody
    //method = RequestMethod.GET明确该方法接收什么样的请求，意味只能接收GET请求，更加合理
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents (
            //意为request中 名为current的 给int current这个参数,@RequestParam可以对参数进行更加详细的声明
            //有时候，如果刚访问页面，可能会没有current（页面数），limit（数据行）的参数
            //此时我们可以添加required = false, defaultValue = "1"
            //意思我们可以不用添加该参数进来，并设置默认值，这里为1,limit参数同理
            @RequestParam(name = "current", required = false, defaultValue = "1") int current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //方式二：eg.  /student/123 获取id为123的学生的信息
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent (
            //@PathVariable路径变量名为id赋值给Int id
            @PathVariable("id") int id) {
        System.out.println(id);
        return "student";
    }


    //POST请求，浏览器向服务器提交请求数据
    //如何获取浏览器提交的数据
    @RequestMapping(path = "/addStudent", method = RequestMethod.POST)
    @ResponseBody
    public String addStudent (
            //这里也可以添加@RequestParam注解声明一下，但是通常只要名称和表单名对应就行
            String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }




    ////响应HTML数据的演示

    /**
     *对比来看方式二比方式一更加简洁，建议使用方式一
     */

    //方式一
    //eg. 浏览器请求需要查询某老师，服务器查询到该老师的数据，响应给浏览器
    @RequestMapping(path = "teacher", method = RequestMethod.GET)
    //此时需要返回html，就不用加@ResponseBody注解，不加注解默认返回html
    public ModelAndView getTeacher () {
        //返回的数据对象ModelAndView为封装Model和View视图数据，
        //这里就是由前端控制器dispacterServlet调度返回的两份数据
        ModelAndView mav = new ModelAndView();
        mav.addObject("name", "张三");
        mav.addObject("age", 18);
        //为对象设置模板的路径和名字
        //此时需要在对应的Templates模板demo中有对应的文件
        mav.setViewName("/demo/view");  //因为模板文件中的格式是固定的，所以名字不需要后缀即可，表示view.html
        return mav;
    }

    //方式二，方式二和方式一本质上是一样的，方式一更加直观，把model和view都装到了一个对象当中
    //而方式二是将model数据封装到 Model model这个参数中，把view视图则直接返回，
    // 返回的值给与了dispacterServlet,model的引用dispacterServlet同样持有着
    //eg. 查询学校
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool (Model model) {
        model.addAttribute("name","北京大学");
        model.addAttribute("age", 98);
        return "/demo/view";
    }




    //向浏览器响应json数据
    /**
     * 什么时候需要响应json数据？
     *      通常是在异步请求当中
     *      客户端需要服务器返回一个局部验证的结果，是否成功失败
     *      比如说：注册Bilibili,输入各数据，当输入昵称后，失去鼠标焦点时，B站立刻响应昵称是否被占用
     *      显然，判断该昵称是否被占用，是访问了服务器和数据库才能得出这种结论的，但是我们的注册页面
     *      并没有被刷新，即当前网页不刷新，访问了服务器，叫做异步请求
     *          而，很显然，本次响应的不是一个html
     *
     *
     * java对象 -> json字符串 -> js对象
     *  起到一个衔接作用，json字符串时很通用的一种字符串，很多语言都可以解析为json字符串
     *  这样的话，各种语言的对象就可以相互的进行转换，方便，跨语言
     */

    //eg. 查询某员工
    //json字符串格式：{"name":"zhangsna","salary":14000.0,"age":28}
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody  //如果想要向浏览器返回的是一个json字符串，需要添加该注解，不然默认的是html
    //当前端控制器识别到这个方法有@ResponseBody，返回的字符串时Map格式时，会将对象自动转换json格式的字符串
    public Map<String, Object> getEmp () {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsna");
        map.put("age", 28);
        map.put("salary", 14000.00);
        return map;
    }

    //eg. 查询所有员工，需要返回多个对象
    //集合形式的json字符串：[{"name":"zhangsna","salary":14000.0,"age":28},{"name":"lisi","salary":7000.0,"age":25}]
    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody  //如果想要向浏览器返回的是一个json字符串，需要添加该注解，不然默认的是html
    //当前端控制器识别到这个方法有@ResponseBody，返回的字符串时Map格式时，会将对象自动转换json格式的字符串
    public List<Map<String, Object>> getEmps () {

        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsna");
        map.put("age", 28);
        map.put("salary", 14000.00);
        list.add(map);

        map = new HashMap<>();
        map.put("name", "lisi");
        map.put("age", 25);
        map.put("salary", 7000.00);
        list.add(map);

        return list;
    }

}
