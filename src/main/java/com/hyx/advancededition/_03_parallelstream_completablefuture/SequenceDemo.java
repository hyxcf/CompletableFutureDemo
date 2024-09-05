package com.hyx.advancededition._03_parallelstream_completablefuture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author hyx
 */
public class SequenceDemo {
    public static void main(String[] args) {
        // 并行流的局限性
        // 需求：创建10个 MyTask 耗时的任务,统计他们执行完的总耗时
        // 方案一：在主线程中使用串行执行
        // 1.创建10个 MyTask 对象，每个对象持续1s，存入List集合
        // {0,1,2,3,4,5,6,7,8,9}
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).toList();

        // 2.执行10个 MyTask ，统计总耗时
        long start = LocalDateTime.now().getSecond();
//        List<Long> collect = tasks.stream().map(MyTask::doWork).toList();
        List<Long> collect = tasks.stream().map(myTask -> {
            return myTask.doWork();
        }).toList();
        long end = LocalDateTime.now().getSecond();
        double costTime = end - start;

        System.out.printf("processed %d tasks %.2f second",tasks.size(),costTime);
    }
}




