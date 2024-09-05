package com.hyx.introduce._04_completablefuture_arrange;

import com.hyx.utils.CommonUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author hyx
 */
public class ThenComposeDemo {

    public static CompletableFuture<String> readFileFuture(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            String filterWordsContent = CommonUtils.readFile(fileName);
            return filterWordsContent;
        });
    }

    public static CompletableFuture<String[]> splitFuture(String context) {
        return CompletableFuture.supplyAsync(() -> {
            String[] filterWords = context.split(",");
            return filterWords;
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 编排2个依赖关系的异步任务 thenCompose()
//        回顾需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组让主线程待用。
/*        // 使用 thenApply
        CompletableFuture<CompletableFuture<String[]>> future = readFileFuture("filter_words.txt")
                .thenApply((context) -> {
                    return splitFuture(context);
                });*/

        // 使用 thenCompose
//        readFileFuture("filter_words.txt").thenCompose(content -> splitFuture(content));
//        readFileFuture("filter_words.txt").thenCompose(ThenComposeDemo2::splitFuture);
        CompletableFuture<String[]> completableFuture = readFileFuture("filter_words.txt").thenCompose(new Function<String, CompletionStage<String[]>>() {
            @Override
            public CompletionStage<String[]> apply(String context) {
                return splitFuture(context);
            }
        });
        String[] filterWords = completableFuture.get();
        CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));

        /**
         * thenApply(Function<T,R>)
         * 重心在于对上一步异步任务的结果T进行应用转换，经Function回调转换后的结果R是一个简单的值
         *
         * thenCompose(Function<T,CompletableFuture<R>>)
         * 重心在于对上一步异步任务的结果T进行应用，经Function回调转换后的结果R是一个CompletableFuture<R>对象
         * 结论：
         * 编排两个带有依赖关系的异步任务(CompletableFuture 对象)，请使用 thenCompose() 方法
         */
    }
}
