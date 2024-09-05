package com.hyx.demo._03_completablefuture_callback;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * hyx
 * @author 32596
 */
public class ThenAcceptDemo {

    public static void main(String[] args) {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，然后打印敏感词数组

        CommonUtils.printThreadLog("main start");

        CompletableFuture.supplyAsync(() -> {
            // 读取文件
            CommonUtils.printThreadLog("读取filter_words.txt文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenApply(content -> {
            // 转化成敏感词汇
            CommonUtils.printThreadLog("把文件内容转换成敏感词汇");
            String[] filterWordsContent = content.split(",");
            return filterWordsContent;
        }).thenAccept(filterWords -> { // 入参一个消费型接口
            CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSeconds(4L);
        CommonUtils.printThreadLog("main end");

        /**
         * 总结
         * thenAccept(Consumer<T> c) 可以对异步任务的结果进行消费使用
         * 返回返回一个不带结果的completableFuture对象
         */

    }

}
