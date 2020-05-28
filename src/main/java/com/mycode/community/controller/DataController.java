package com.mycode.community.controller;

import com.mycode.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    // 访问统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    // /dataUv接收的是一个post的请求，转发过来的也同样是post请求，所以说，这个方法就为什么需要能接收RequestMethod.POST，否则就不行
    public String getDataPage () {
        return "/site/admin/data";
    }

    // 统计网站UV
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV (@DateTimeFormat(pattern = "yyyy-MM-dd") Date uvStart,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date uvEnd, Model model) {
        // 传入的date格式的字符串，服务器是不知道的，所以不能帮你转换成响应的格式，
        // 我们需要告诉服务器我们传入的date格式是什么样的，@DateTimeFormat(pattern = "yyyy-MM-dd")

        long uv = dataService.calculateUnionUV(uvStart, uvEnd);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", uvStart);
        model.addAttribute("uvEndDate", uvEnd);

        // 可return "/site/admin/data"; 将model和view通过dispatcherServlet返回给模板渲染网页
        // 也可如下方式，转发-
        // 声明当前方法只能处理某些功能的一半，还需要另一个方法继续处理请求，另外一个方法时和它同级的，也可处理请求的方法，不是模板
        // "forward:/data":交给/data路径下的方法继续处理
        // 好处：在/data下如果有其他逻辑，那么本方法可以起到一个逻辑复用的效果
        return "forward:/data";
    }

    // 统计网站DAU
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU (@DateTimeFormat(pattern = "yyyy-MM-dd") Date dauStart,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date dauEnd, Model model) {
        // 传入的date格式的字符串，服务器是不知道的，所以不能帮你转换成响应的格式，
        // 我们需要告诉服务器我们传入的date格式是什么样的，@DateTimeFormat(pattern = "yyyy-MM-dd")

        long dau = dataService.calculateUnionDAU(dauStart, dauEnd);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", dauStart);
        model.addAttribute("dauEndDate", dauEnd);

        return "forward:/data";
    }
}
