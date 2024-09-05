package com.hyx.advancededition._01_complatablefuture_interaction;

import com.hyx.utils.CommonUtils;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * @author hyx
 */
public class RunAfterEitherDemo {
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

        future1.runAfterEither(future2,()->{
            CommonUtils.printThreadLog("有一个异步任务完成");
        });
        CommonUtils.sleepSeconds(4L);
    }
}
