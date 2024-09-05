package com.hyx.demo._05_completablefuture_exception;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class HandleDemo2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：对回调链中的一次异常进行恢复处理
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            int i = 1 / 0;
            return "result1";
        }).handle((result, ex) -> {
            if (ex != null) {
                CommonUtils.printThreadLog("出现异常：" + ex.getMessage());
                return " UnKnown1";
            }
            return result;
        }).thenApply(result -> {

            String s = null;
            int i = s.length();

            return result + " result2";
        }).handle((result, ex) -> {
            if (ex != null) {
                CommonUtils.printThreadLog("出现异常：" + ex.getMessage());
                return " UnKnown2";
            }
            return result;
        }).thenApply(result -> {
            return result + " result3";
        }).handle((result, ex) -> {
            if (ex != null) {
                CommonUtils.printThreadLog("出现异常：" + ex.getMessage());
                return " UnKnown3";
            }
            return result;
        });

        CommonUtils.printThreadLog("main continue");
        String rs = future.get();
        CommonUtils.printThreadLog("rs=" + rs);
        CommonUtils.printThreadLog("main end");
        /**
         * 异步任务不管是否发生异常，handle方法都会执行。
         * 所以 handle 方法的核心作用在于对上一步异步任务进行现场修复
         */
    }
}
