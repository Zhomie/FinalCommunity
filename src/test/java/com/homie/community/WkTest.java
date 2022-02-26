package com.homie.community;

import java.io.IOException;

public class WkTest {
    public static void main(String[] args) {
        String cmd = "D:/Program Files/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com D:/wkhtmltopdfData/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok..");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
