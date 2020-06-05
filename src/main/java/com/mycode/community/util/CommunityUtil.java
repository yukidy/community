package com.mycode.community.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    // 通用唯一识别码，UUID 的目的是让分布式系统中的所有元素都能有唯一的识别信息
    public static String generateUUID () {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密(只能加密不能解密)
    // (key)hello -> abc123def456(假设)  每次加密的结果都是这个值，如果密码过于简单，同样不安全
    // (key)hello + 3e4a8 -> abc123def456abc(假设)  密码的后面再加随机字符串，更安全
    public static String md5 (String key) {
        //判断key是否为空，空格和null都视为空
        if (StringUtils.isBlank(key)) {
            return null;
        }
        //调用spring自带的工具DigestUtils，md5DigestAsHex把传入的参数（要求传入的是byte类型）加密成16进制的字符串
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 获取JSON字符串
     * @param code 编码/编号
     * @param msg 提示信息
     * @param map 封装的业务数据
     * @return json格式的字符串
     */
    public static String getJSONString (int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);

        if (map != null) {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }

        return jsonObject.toJSONString();
    }

    //重载方法
    public static String getJSONString (int code, Map<String, String> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);

        if (map != null) {
            for (String key : map.keySet()) {
                jsonObject.put(key, map.get(key));
            }
        }

        return jsonObject.toJSONString();
    }

    public static String getJSONString (int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString (int code) {
        return getJSONString(code, null, null);
    }

}
