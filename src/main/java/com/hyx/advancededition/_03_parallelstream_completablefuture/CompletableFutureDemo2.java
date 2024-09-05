package com.hyx.advancededition._03_parallelstream_completablefuture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author 32596
 */
public class CompletableFutureDemo2 {
    public static void main(String[] args) {
        // CompletableFuture 在流式操作的优势
        // 需求：创建10MyTask耗时的任务，统计它们执行完的总耗时
        // 方案三：使用CompletableFuture
        // step 1: 创建10个MyTask对象，每个任务持续1s，存入List集合
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).toList();

        // 准备线程池
        int N_CPU = Runtime.getRuntime().availableProcessors();// 获取 cpu 的内核数
        // 设置线程池中的线程的数量至少为10
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(tasks.size(), N_CPU * 2));

        // step 2: 根据MyTask对象构建10个耗时的异步任务
        long start = LocalDateTime.now().getSecond();
        List<CompletableFuture<Long>> futures = tasks
                .stream()
                .map(myTask -> CompletableFuture.supplyAsync(myTask::doWork,executor))
                .toList();

        // step 3: 当所有任务完成时，获取每个异步任务的执行结果，存入List集合中
        List<Long> results = futures
                .stream()
                .map(CompletableFuture::join)
                .toList();
        long end = LocalDateTime.now().getSecond();

        double costTime = end - start;
        System.out.printf("processed %d tasks cost %.2f second", tasks.size(), costTime);

        // 关闭线程池
        executor.shutdown();
        /**
         * 总结：
         * CompletableFutures 可以控制更多的线程数量，而 ParallelStream 不能
         */
    }
}
