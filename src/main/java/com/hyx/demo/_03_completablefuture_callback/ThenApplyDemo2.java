package com.hyx.demo._03_completablefuture_callback;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * hyx
 * @author 32596
 * 通过链式操作使用 thenApply
 */
public class ThenApplyDemo2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组( 敏感词数组 )，异步任务返回敏感词数组
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words.txt文件");
            return CommonUtils.readFile("filter_words.txt");
        }).thenApply(content -> {
            CommonUtils.printThreadLog("转化成敏感词汇");
            String[] filterWords = content.split(",");
            return filterWords;
        });
        CommonUtils.printThreadLog("here is not blocked main continue");
        String[] filterWords = filterWordsFuture.get();
        CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
    }

}
