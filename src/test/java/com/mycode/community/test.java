package com.mycode.community;

import com.mycode.community.util.CommunityUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class test {

    public static void main(String[] args) {

        System.out.println(new Date(System.currentTimeMillis()));
        System.out.println(new Date(System.currentTimeMillis() + 100 * 24 * 3600 * 1000));
        System.out.println(System.currentTimeMillis() + 100 * 24 * 3600 * 1000);

        Calendar now=Calendar.getInstance();

        now.add(Calendar.SECOND,60 * 60 * 24 * 30);
        System.out.println(new Date(now.getTimeInMillis()));

        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 18);
        System.out.println(CommunityUtil.getJSONString(0, "ok", map));

    }

}
