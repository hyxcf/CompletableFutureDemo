package com.hyx.demo._01_future_vs_completablefuture;

import com.hyx.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * hyx
 * CompletionStage 是一个代表异步计算阶段的接口，计算可以是一个任务或操作的结果。这个接口允许你在计算完成后进行额外的处理，而不必阻塞当前线程等待结果。
 * CompletionStage 可以被看作是一个异步计算的状态容器，当计算完成时，它会包含计算的结果或异常。
 * @author 32596
 */
public class FutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        // 1. 读取敏感词汇 -> thread1
        Future<String[]> filterWordsFuture = executor.submit(() -> {
            String str = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = str.split(",");
            return filterWords;
        });

        // 2.读取新闻稿 -> thread2
        Future<String> filterNewsFuture = executor.submit(() -> {
            return CommonUtils.readFile("news.txt");
        });

        // 3.替换操作 -> thread3
        Future<String> replaceFuture = executor.submit(() -> {
            String[] words = filterWordsFuture.get();
            String news = filterNewsFuture.get();
            for (String word : words) {
                if (news.contains(word)) {
                    news = news.replace(word, "**");
                }
            }
            return news;
        });

        // 4.打印输出替换后的新闻稿 -> thread main
        String replaceContent = replaceFuture.get();
        System.out.println(replaceContent);
    }

}
