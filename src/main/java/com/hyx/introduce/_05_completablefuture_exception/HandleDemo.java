package com.hyx.introduce._05_completablefuture_exception;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class HandleDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // handle():常常被用来恢复回调链中的一次特定的异常，回调链恢复后可以进一步向下传递。
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            int i = 1 / 0;
            return "result1";
        }).handle((result, ex) -> {
            CommonUtils.printThreadLog("上一步异常的恢复");
            if (ex != null) {
                CommonUtils.printThreadLog("出现异常：" + ex.getMessage());
                return "UnKnown";
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
