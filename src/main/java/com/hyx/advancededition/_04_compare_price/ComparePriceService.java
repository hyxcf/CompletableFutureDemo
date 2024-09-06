package com.hyx.advancededition._04_compare_price;

import com.hyx.utils.CommonUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hyx
 */
public class ComparePriceService {

    public PriceResult batchComparePrice(List<String> products) {
        // step 1: 遍历每个商品，根据商品开启异步任务获取最终价，然后归集到List集合
        List<CompletableFuture<PriceResult>> futureList = products.stream()
                .map(product -> CompletableFuture
                        .supplyAsync(() -> HttpRequest.getTaoBaoPrice(product))// 获取商品的价格                         执行回调
                        .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getTaoBaoDiscount(product)), this::computeRealPrice))
                .toList();

        // step 2: 把多个商品的最终价进行排序比较获取最小值
        return futureList
                .stream()
                .map(CompletableFuture::join) // 获取商品的最终价格
                .sorted(Comparator.comparing(PriceResult::getRealPrice))
                .findFirst()
                .get();
    }

    // 方案三：使用 CompletableFuture 进一步增强并行 6个线程同时操作 真强啊！！！我勒个老豆
    public PriceResult getCheapestPlatformPrice3(String productName) {
        // 获取淘宝平台的价格和优惠
        CompletableFuture<PriceResult> taoBaoCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getTaoBaoPrice(productName)) // 1s
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getTaoBaoDiscount(productName)), this::computeRealPrice);// 1s

        // 获取京东平台的价格和优惠
        CompletableFuture<PriceResult> jDongCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getJDongPrice(productName)) // 1s
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getJDongDiscount(productName)), this::computeRealPrice); // 1s
        // 获取拼多多平台的价格和优惠
        CompletableFuture<PriceResult> pddCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getPDDPrice(productName)) // 1s
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getPDDDiscount(productName)), this::computeRealPrice); // 1s

        return Stream.of(taoBaoCF, jDongCF, pddCF)
                .map(CompletableFuture::join)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();

    }

    // 计算商品的最终价格 = 平台价格 - 优惠价
    public PriceResult computeRealPrice(PriceResult priceResult, int discount) {
        priceResult.setRealPrice(priceResult.getPrice() - discount);
        priceResult.setDiscount(discount);
        CommonUtils.printThreadLog(priceResult.getPlatform() + "最终价格计算完成:" + priceResult.getRealPrice());
        return priceResult;
    }
}
/*    // 方案二：使用Future+线程池增强并行 3个线程同时操作
    public PriceResult getCheapestPlatformPrice2(String productName) {
        // 线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // 获取淘宝平台的价格和优惠
        Future<PriceResult> taoBaoFuture = executor.submit(() -> {
            PriceResult priceResult = HttpRequest.getTaoBaoPrice(productName);
            int discount = HttpRequest.getTaoBaoDiscount(productName);
            return this.computeRealPrice(priceResult, discount);
        });

        // 获取京东平台的价格和优惠
        Future<PriceResult> jDongFuture = executor.submit(() -> {
            PriceResult priceResult = HttpRequest.getJDongPrice(productName);
            int discount = HttpRequest.getJDongDiscount(productName);
            return this.computeRealPrice(priceResult, discount);
        });

        // 获取拼多多平台的价格和优惠
        Future<PriceResult> pddFuture = executor.submit(() -> {
            PriceResult priceResult = HttpRequest.getPDDPrice(productName);
            int discount = HttpRequest.getPDDDiscount(productName);
            return this.computeRealPrice(priceResult, discount);
        });

        // 比较计算最便宜的平台和价格
        return Stream.of(taoBaoFuture, jDongFuture, pddFuture)
                .map(future -> {
                    try {
                        // 超时等待时间为5秒
                        return future.get(5, TimeUnit.SECONDS); // Future 是没有 join 方法的
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    } finally {
                        executor.shutdown();
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();
    }*/




/*    // 方案一：串行方式操作商品比价
    public PriceResult getCheapestPlatformPrice(String productName) {
        PriceResult priceResult;
        int discount;

        // 获取淘宝平台的价格和优惠
        priceResult = HttpRequest.getTaoBaoPrice(productName);
        discount = HttpRequest.getTaoBaoDiscount(productName);
        PriceResult taoBaoPriceResult = this.computeRealPrice(priceResult, discount);

        // 获取京东平台的价格和优惠
        priceResult = HttpRequest.getJDongPrice(productName);
        discount = HttpRequest.getJDongDiscount(productName);
        PriceResult jDongPriceResult = this.computeRealPrice(priceResult, discount);

        // 获取拼多多平台的价格和优惠
        priceResult = HttpRequest.getPDDPrice(productName);
        discount = HttpRequest.getPDDDiscount(productName);
        PriceResult pddPriceResult = this.computeRealPrice(priceResult, discount);


        // 计算最优的平台和价格
        return Stream.of(taoBaoPriceResult, jDongPriceResult, pddPriceResult)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();
    }*/