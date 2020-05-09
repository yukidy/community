package com.mycode.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

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

}
