package com.hyx.introduce._02_completablefuture_create;

import com.hyx.utils.CommonUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * hyx
 * runAsync() 的重载方法
 * static CompletableFuture<Void>  runAsync(Runnable runnable)
 * static CompletableFuture<Void>  runAsync(Runnable runnable, Executor executor)
 * supplyAsync() 的重载方法
 * static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
 * static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
 * @author 32596
 */
public class SupplyAsyncDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：指定线程池，开启异步任务读取 news.txt 中的新闻稿，返回文件中内容并在主线程打印输出
        CommonUtils.printThreadLog("main start");

        ExecutorService executor = Executors.newFixedThreadPool(5);

        // 使用 lambda 表达式
        // 使用指定的线程池
        /*
        fixme:
         如果所有 CompletableFuture 共享一个线程池，那么一旦有异步任务执行一些很慢的 I/O 操作，
         就会导致线程池中所有线程都阻塞在 I/O 操作上，从而造成线程饥饿，进而影响整个系统的性能。
         所以，强烈建议你要根据不同的业务类型创建不同的线程池，以避免互相干扰。
         不同类型的任务跑在不同的线程池，防止伪死锁
        */
        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("异步读取文件开始");
            return CommonUtils.readFile("news.txt");
        },executor);

        CommonUtils.printThreadLog("here is not blocked main continue");
        String content = newsFuture.get(); // 阻塞获取,直到 newsFuture 完成
        System.out.println("news:" + content);
        // 关闭线程池
        executor.shutdown();
        CommonUtils.printThreadLog("main end");
    }
}
