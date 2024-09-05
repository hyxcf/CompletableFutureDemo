package com.hyx.introduce._02_completablefuture_create;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;

/**
 * hyx
 * .
 * @author 32596
 */
public class RunAsyncDemo2 {

    public static void main(String[] args) {
        // 需求：使用多线程异步读取 words.txt 中的敏感词汇，并打印输出。
        CommonUtils.printThreadLog("main start");

        CompletableFuture.runAsync(() -> {
            CommonUtils.printThreadLog("读取文件");
            String content = CommonUtils.readFile("news.txt");
            System.out.println(content);
        });

        CommonUtils.printThreadLog("here is not blocked main continue");
        CommonUtils.sleepSeconds(4L);
        CommonUtils.printThreadLog("main end");
        /**
         * 异步任务是并发执行还是并行执行？
         * 如果是单核 CPU，那么异步任务之间就是并发执行。如果是多核 CPU 异步任务就是并行执行
         * 重点（敲黑板）
         * 作为开发者，我们只需要清楚如何开启异步任务，CPU 硬件会把异步任务合理的分配给 CPU 上的核运行
         */


    }

}
