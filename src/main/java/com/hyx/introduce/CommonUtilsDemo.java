package com.hyx.introduce;

import com.hyx.utils.CommonUtils;

/**
 * hyx
 * .
 * @author 32596
 */
public class CommonUtilsDemo {
    public static void main(String[] args) {
        // 测试 CommonUtils 工具类
        String content = CommonUtils.readFile("news.txt");
        CommonUtils.printThreadLog(content);
    }
}
