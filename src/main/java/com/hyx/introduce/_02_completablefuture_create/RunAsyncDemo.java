package com.hyx.introduce._02_completablefuture_create;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;

/**
 * hyx
 * .
 * @author 32596
 */
public class RunAsyncDemo {

    public static void main(String[] args) {

        // runAsync 创建异步任务
        CommonUtils.printThreadLog("main start");
        // 使用Runnable匿名内部类
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                CommonUtils.printThreadLog("读取文件开始");
                // 使用睡眠来模拟一个长时间的工作任务(例如读取文件，网络请求等)
                CommonUtils.sleepSeconds(3L);
                CommonUtils.printThreadLog("读取文件结束");
            }
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSeconds(4L); //  此处休眠为的是等待CompletableFuture背后的线程池执行完成。
        CommonUtils.printThreadLog("main end");

    }

}
