package com.hyx.introduce._04_completablefuture_arrange;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.*;

/**
 * @author hyx
 */
public class ThenComposeDemo2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // thenCompose()
        // 回顾需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组让主线程待用。
        CommonUtils.printThreadLog("main start");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CompletableFuture<String[]> filterWordsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenComposeAsync(content -> CompletableFuture.supplyAsync(() -> {
//                    String[] filterWords = content.split(",");
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            return content.split(",");
        }), executor); // thenCompose 的异步回调变体版本 thenComposeAsync

        CommonUtils.printThreadLog("main continue");
        String[] filterWords = filterWordsFuture.get();
        CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        // 关闭线程池
        executor.shutdown();
        CommonUtils.printThreadLog("main end");
    }
}
