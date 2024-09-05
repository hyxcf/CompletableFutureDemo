package com.hyx.demo._04_completablefuture_arrar;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class ThenCombineDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中
        CommonUtils.printThreadLog("main start");

        // 1.读取敏感词汇的文件并解析到数组中
        CompletableFuture<String[]> future1 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取敏感词汇的文件并解析到数组中");
            String content = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        // 2.读取news文件内容
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取news文件内容");
            String content = CommonUtils.readFile("news.txt");
            return content;
        });

        CompletableFuture<String> combineFuture = future1.thenCombine(future2, (words, content) -> {
            // 3.替换操作
            CommonUtils.printThreadLog("替换操作");
            for (String word : words) {
                if (content.contains(word)) {
                    content = content.replace(word, "***");
                }
            }
            return content;
        });
        CommonUtils.printThreadLog("main continue");
        String content = combineFuture.get();
        System.out.println("content=" + content);
        CommonUtils.printThreadLog("main end");

        /**
         * 总结
         * thenCombine 用于合并两个没有依赖关系的异步任务
         */
    }
}
