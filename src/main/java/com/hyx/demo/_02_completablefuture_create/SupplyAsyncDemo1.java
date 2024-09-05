package com.hyx.demo._02_completablefuture_create;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * hyx
 * 它入参一个 Supplier 供给者，用于供给带返回值的异步任务
 * 并返回CompletableFuture<U>，其中U是供给者给程序供给值的类型。
 * @author 32596
 */
public class SupplyAsyncDemo1 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：开启异步任务读取 news.txt 文件中的新闻稿，返回文件中内容并在主线程打印输出
        CommonUtils.printThreadLog("main start");

/*        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                return CommonUtils.readFile("news.txt");
            }
        });*/

        // 使用 lambda 表达式
        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("异步读取文件开始");
            return CommonUtils.readFile("news.txt");
        });

        CommonUtils.printThreadLog("here is not blocked main continue");
        String content = newsFuture.get(); // 阻塞获取,直到 newsFuture 完成
        System.out.println("news:" + content);
        CommonUtils.printThreadLog("main end");

        /**
         * 疑问：get方法会阻塞的，会不会影响程序性能
         * 回调函数
         */

    }

}
