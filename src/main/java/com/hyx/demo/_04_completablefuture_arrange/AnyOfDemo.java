package com.hyx.demo._04_completablefuture_arrange;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class AnyOfDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.sleepSeconds(2L);
            return "Future1的结果";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.sleepSeconds(1L);
            return "Future2的结果";
        });

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.sleepSeconds(3L);
            return "Future3的结果";
        });

        CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(future1, future2, future3);

        // 输出Future2的结果
        System.out.println(anyOfFuture.get());
    }
}
