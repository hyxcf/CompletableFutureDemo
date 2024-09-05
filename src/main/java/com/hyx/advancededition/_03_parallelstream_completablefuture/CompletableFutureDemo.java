package com.hyx.advancededition._03_parallelstream_completablefuture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author 32596
 */
public class CompletableFutureDemo {
    public static void main(String[] args) {
        // CompletableFuture 在流式操作的优势
        // 需求：创建10MyTask耗时的任务，统计它们执行完的总耗时
        // 方案三：使用CompletableFuture
        // step 1: 创建10个MyTask对象，每个任务持续1s，存入List集合
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).toList();

        // step 2: 根据MyTask对象构建10个耗时的异步任务
        long start = LocalDateTime.now().getSecond();
        List<CompletableFuture<Long>> futures = tasks
                .stream()
                .map(myTask -> CompletableFuture.supplyAsync(myTask::doWork))
                .toList();

        // step 3: 当所有任务完成时，获取每个异步任务的执行结果，存入List集合中
        List<Long> results = futures
                .stream()
                .map(CompletableFuture::join)
                .toList();
        long end = LocalDateTime.now().getSecond();

        double costTime = end - start;
        System.out.printf("processed %d tasks cost %.2f second", tasks.size(), costTime);
        /**
         * 观察发现
         * 使用CompletableFuture和使用并行流的时间大致一样
         * CompletableFutures 比 ParallelStream 优点之一是你可以指定Executor去处理任务。你能选择更合适数量的线程。
         */
    }
}
