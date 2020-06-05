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

        int i = 1;
        switch(i){
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("1");
            case 2:
                System.out.println("2");
            default:
                System.out.println("default");
        }

        System.out.println(3 / 2);
        System.out.println(5 / 3);
        System.out.println(5 / 2);

        long k = 2;
        int l = 2;
        long k2 = 155;
        int l2 = 155;
        Long k3 = k2;
        Long k4 = new Long(2);
        Long k6 = new Long(2);
        Long k5 = new Long(255);
        Long k7 = new Long(255);
        Integer i2 = 155;
        Integer i3 = 155;
        System.out.println(k == l); // t
        System.out.println(k == k4); // t
        System.out.println(k2 == l2); // t
        System.out.println(k3 == k2); // t
        System.out.println(k4 == k); // t
        System.out.println(k5 == k2); // t
        System.out.println(k5 == k3); // f
        System.out.println(k5 == l2); // t
        System.out.println(k4 == k6); // f
        System.out.println(k4.equals(k6)); // t
        System.out.println(k5.equals(k3)); // t
        System.out.println(k3.getClass());
        System.out.println(k7.equals(k5)); // t
        System.out.println(i2 == i3); // 2-t  155-f

    }

}
