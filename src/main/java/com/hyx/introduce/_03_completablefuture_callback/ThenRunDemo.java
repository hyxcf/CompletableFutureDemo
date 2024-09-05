package com.hyx.introduce._03_completablefuture_callback;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;

/**
 * hyx
 *
 * @author 32596
 */
public class ThenRunDemo {
    public static void main(String[] args) {
        // 演示案例：我们仅仅想知道 filter_words.txt 的文件是否读取完成
        CommonUtils.printThreadLog("main start");

        CompletableFuture.supplyAsync(() -> {
            // 读取文件
            CommonUtils.printThreadLog("开始读取filter_words.txt文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenRun(() -> {
            CommonUtils.printThreadLog("文件读取完成");
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSeconds(4L);
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * thenRun(Runnable action)
         * 当异步任务完成后，只想得到一个完成的通知，不使用上一步异步任务的结果，就可以使用thenRun
         * 通常会把它用在链式操作的末端
         */
    }
}
