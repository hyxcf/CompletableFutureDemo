package com.hyx.demo._03_completablefuture_callback;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * hyx
 * @author 32596
 */
public class ThenApplyDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组( 敏感词数组 )，异步任务返回敏感词数组
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> readFilesFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words.txt文件");
            return CommonUtils.readFile("filter_words.txt");
        });
        CompletableFuture<String[]> filterWordsFuture = readFilesFuture.thenApply(content -> {
            CommonUtils.printThreadLog("读取过滤词");
            String[] filterWords = content.split("-");
            return filterWords;
        });
        CommonUtils.printThreadLog("here is not blocked main continue");
        String[] filterWords = filterWordsFuture.get();
        CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * thenApply(Function<T,R>) 可以对异步任务的结果进一步应用Function转换
         * 转换后的结果可以在主线程获取，也可以进行下一步的操作。
         */
    }

}
