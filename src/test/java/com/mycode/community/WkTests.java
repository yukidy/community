package com.mycode.community;

import java.io.IOException;

public class WkTests {

    public static void main(String[] args) {

        // java使用wkhtmltoimage
        String cmd = "d:/work/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/work/data/wk-images/2.png";

        try {
            // 该命令将执行操作交给操作系统
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
