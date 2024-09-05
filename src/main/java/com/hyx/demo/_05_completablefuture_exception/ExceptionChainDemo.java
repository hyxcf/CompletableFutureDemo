package com.hyx.demo._05_completablefuture_exception;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;

/**
 * @author hyx
 */
public class ExceptionChainDemo {
    public static void main(String[] args) {
        // 异常如何在回调链中传播
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            // int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {

            CommonUtils.printThreadLog(result);

            String str = null;
            int length = str.length();

            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).thenAccept(result -> {
            CommonUtils.printThreadLog(result);
        });
        /**
         * 如果回调链中出现任何异常，回调链不再向下执行，立即转入异常处理
         */

    }
}
