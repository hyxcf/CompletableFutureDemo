package com.hyx.demo._03_completablefuture_callback;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * hyx
 *
 * @author 32596
 */
public class ThenApplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，主线程获取结果打印输出这个数组
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {

/*            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;*/

            // 此时，立即返回结果
            return "尼玛, NB, tmd";
        }).thenApply((content) -> {
            /**
             * 一般而言，thenApply任务的执行和supplyAsync()任务执行可以使用同一线程执行
             * 如果supplyAsync()任务立即返回结果，则thenApply的任务在主线程中执行
             */
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        CommonUtils.printThreadLog("main continue");

        String[] filterWords = filterWordFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * 一般而言，commonPool 为了提高性能
         * thenApply 中回调任务和 supplyAsync 中的异步任务使用的是同一个线程
         * 特殊情况：
         * 如果 supplyAsync 中的任务是立即返回结果（不是耗时的任务），那么 thenApply 回调任务也会在主线程执行。
         */
    }
}