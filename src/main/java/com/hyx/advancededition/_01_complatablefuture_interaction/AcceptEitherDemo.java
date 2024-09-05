package com.hyx.advancededition._01_complatablefuture_interaction;

import com.hyx.utils.CommonUtils;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class AcceptEitherDemo {
    public static void main(String[] args) {
        // 异步任务交互
        CommonUtils.printThreadLog("main start");
        // 开启异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int x = new Random().nextInt(3);
            CommonUtils.sleepSeconds((long) x);
            CommonUtils.printThreadLog("任务1耗时:" + x + "秒");
            return x;
        });

        // 开启异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int y = new Random().nextInt(3);
            CommonUtils.sleepSeconds((long) y);
            CommonUtils.printThreadLog("任务2耗时:" + y + "秒");
            return y;
        });

        // 哪些异步任务的结果先到达，就使用哪个异步任务的结果
        future1.acceptEither(future2, result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
        });

        // 主线程休眠4秒，等待所有异步任务完成
        CommonUtils.sleepSeconds(4L);
        CommonUtils.printThreadLog("main end");

    }
}
