package com.hyx.demo._04_completablefuture_arrange;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author hyx
 */
public class AllOfDemo {

    public static CompletableFuture<String> readFileFuture(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
//            String content = CommonUtils.readFile(filePath);
            return CommonUtils.readFile(filePath);
        });
    }

    public static void main(String[] args) {
        // 需求：统计news1.txt,news2.txt,news3.txt 文件中包含 CompletableFuture 关键字的文件的个数

        // 1.创建一个集合存储文件名
        List<String> fileList = Arrays.asList("news1.txt", "news2.txt", "news3.txt");
        // 2.根据文件名调用 readFileFuture 创建多个completableFuture 并存入list集合
        List<CompletableFuture<String>> readFileFutureList = fileList.stream().map(fileName -> {
            return readFileFuture(fileName);
        }).collect(Collectors.toList());


        // 3.把list集合转换成数组待用,以便传入 allOf 方法中
        int len = readFileFutureList.size();
        CompletableFuture[] readFileFutureArr = readFileFutureList.toArray(new CompletableFuture[len]);

        // 4.使用 allOf 方法合并多个异步任务
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(readFileFutureArr);

        // 5.当多个异步任务都完成后，使用回调操作文件结果，统计符合条件的文件个数
        CompletableFuture<Long> countFuture = allOfFuture.thenApply(v -> {
            return readFileFutureList.stream()
                    .map(future -> future.join())
                    .filter(content -> content.contains("CompletableFuture"))
                    .count();
        });

        // 6.主线程打印输出文件个数
        Long count = countFuture.join();
        System.out.println("count=" + count);
        /**
         * allOf 特别适合合并多个异步任务，当所有异步任务都完成时可以进一步操作
         */
    }
}
