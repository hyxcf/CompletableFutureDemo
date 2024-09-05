package com.hyx.advancededition._02_get_join;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author hyx
 */
public class GetOrJoinDemo {
    public static void main(String[] args) {
        // get or join
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "hello";
        });

        String rs = future.join();
        CommonUtils.printThreadLog("rs= " + rs);

/*        try {
            String rs = future.get();
            CommonUtils.printThreadLog("rs= " + rs);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }*/
        /**
         * 使用时，我们发现，get() 抛出检查时异常 ，需要程序必须处理
         * 而join() 方法抛出运行时异常，程序可以不处理。所以，join() 更适合用在流式编程中。
         */
    }

}
