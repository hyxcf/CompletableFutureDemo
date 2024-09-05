package com.hyx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * 有空看看java11新特性
 * hyx
 * 异步辅助工具类
 *
 * @author 32596
 */
public class CommonUtils {

    // 读取指定路径的文件
    public static String readFile(String pathToFile) {
        try {
            return Files.readString(Paths.get(pathToFile));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 休眠指定的毫秒数
    public static void sleepMills(Long mills) {
        try {
            TimeUnit.MILLISECONDS.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 休眠指定的秒数
    public static void sleepSeconds(Long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * StringJoiner 是 Java 8 引入的一个类，用于简化字符串连接操作。它允许你将多个字符串或对象连接在一起，并可以设置分隔符、前缀和后缀。
     * 主要特点
     * 类: java.util.StringJoiner
     * 构造函数:
     * StringJoiner(CharSequence delimiter) — 仅设置分隔符。
     * StringJoiner(CharSequence delimiter, CharSequence prefix, CharSequence suffix) — 设置分隔符、前缀和后缀。
     */
    // 打印输出带线程的信息的日志
    public static void printThreadLog(String message) {
        // 时间戳 | 线程id | 线程名 | 日志信息
        String result = new StringJoiner(" | ")
                .add(getCurrentTime())
                .add(String.format("%2d", Thread.currentThread().getId()))
                .add(Thread.currentThread().getName())
                .add(message)
                .toString();
        System.out.println(result);
    }
    private static String getCurrentTime() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("[HH:mm:ss.SSS]"));
    }
}
