package com.hyx.advancededition._01_complatablefuture_interaction;

import com.hyx.utils.CommonUtils;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class ApplyToEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 异步任务交互,applyToEither() 把两个异步任务做比较，异步任务先到结果的，就对先到的结果进行下一步的操作。
        // 异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int i = new Random().nextInt(30);
            CommonUtils.sleepSeconds((long) i);
            CommonUtils.printThreadLog("任务1耗时" + i + "秒");
            return i;
        });

        // 异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int g = new Random().nextInt(3);
            CommonUtils.sleepSeconds((long) g);
            CommonUtils.printThreadLog("任务2耗时" + g + "秒");
            return g;
        });

        // 哪个异步任务结果先到达，使用哪个异步任务的结果
        CompletableFuture<Integer> future3 = future1.applyToEither(future2, result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
            return result;
        });

        Integer result = future3.get();
        System.out.println("result:" + result);
        /**
         * 异步任务指两个交互任务，哪个结果先到，就使用哪个结果（ 先到先用 ）
         */
    }

}
