package com.mycode.community.controller;

import com.mycode.community.entity.Event;
import com.mycode.community.event.EventProducer;
import com.mycode.community.util.CommunityConstant;
import com.mycode.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    /**
     *  前端访问，服务端生成分享的图片，生成图片的过程会比较耗时，这个过程一定是异步的方式
     *  一般习惯使用-事件驱动
     *      一个分享事件，Controller只需要将这个分享事件丢给kafka，后续由kafka异步实现即可。
     *      如果等待的话，处理时间会比较长，客户端响应较慢，体验差。
     */

    @Autowired
    private EventProducer eventProducer;

    // 还需要用到应用程序的域名，和项目的访问路径,图片的存储路径
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value(("${wk.image.storage}"))
    private String imageStorage;

    /**
     * 分享请求
     * @param htmlUrl
     * @return
     */
    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String shareImages (String htmlUrl) {

        // 文件名
        String fileName = CommunityUtil.generateUUID();

        // 异步生成长图，启动分享事件
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回访问路径
        Map<String, Object> map = new HashMap<>();
        map.put("shareUrl", domain + contextPath + "/share/images/" + fileName);
        return CommunityUtil.getJSONString(0, null, map);
    }


    /**
     *  获取
     */
    @RequestMapping(path = "/share/images/{fileName}", method = RequestMethod.GET)
    public void getShareImages (@PathVariable("fileName") String fileName, HttpServletResponse response) {

        if (StringUtils.isBlank(fileName))
            throw new IllegalArgumentException("文件名不能为空!");

        // 输出图片
        response.setContentType("image/png");
        // 生成文件，读取本地文件
        File file = new File(imageStorage + "/" + fileName + ".png");

        try {
            // 输出流
            OutputStream os = response.getOutputStream();
            // 将文件转换为输入流
            FileInputStream fis = new FileInputStream(file);
            // 每次读取，需要一个缓冲区，增加读取效率
            byte[] buffer = new byte[1024];
            // 游标
            int b = 0;
            // 每次读取到的数据都存到buffer中，再赋值给b,b不为-1，表示还有数据可读
            while ((b = fis.read(buffer)) != -1) {
                // 游标的作用就是，每次读取，最多只有buffer那么多，但是最后一次，可能达不到1024，所以用b来作为游标记录每次的读取范围
                // 从缓冲区buffer的第0位读到第b位
                os.write(buffer, 0, b);
            }

        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }
    }

}
