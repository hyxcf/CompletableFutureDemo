package com.hyx.demo._03_completablefuture_callback;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.*;

/**
 * hyx
 *
 * @author 32596
 */
public class ThenApplyAsyncDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，主线程获取结果打印输出这个数组
        CommonUtils.printThreadLog("main start");
        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenApplyAsync((content) -> {
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        },executor);

        CommonUtils.printThreadLog("main continue");

        String[] filterWords = filterWordFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));

        // 关闭线程池
        executor.shutdown();

        CommonUtils.printThreadLog("main end");
    }
}