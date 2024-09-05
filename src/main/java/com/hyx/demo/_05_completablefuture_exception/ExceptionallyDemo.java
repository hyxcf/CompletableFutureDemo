package com.hyx.demo._05_completablefuture_exception;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author hyx
 */
public class ExceptionallyDemo {
    public static void main(String[] args) {
        // exceptionally 处理回调链上的异常
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
//             int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {

            String str = null;
            int length = str.length();

            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).exceptionally(ex -> {
            System.out.println("出现异常"+ex.getMessage());
            return "UnKnown";
        });
    }
}
