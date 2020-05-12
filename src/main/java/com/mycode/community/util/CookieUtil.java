package com.mycode.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    /**
     * 通过key获取请求中对应的cookie的值
     * @param request
     * @param key
     * @return 返回cookie中需要的值
     */
    public static String getValue (HttpServletRequest request, String key) {
        //空值处理
        if (request == null || key == null) {
            throw new IllegalArgumentException("参数为空!");
        }

        //一个请求可能包含多个cookie，需要装入数组后遍历
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (key.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

}
