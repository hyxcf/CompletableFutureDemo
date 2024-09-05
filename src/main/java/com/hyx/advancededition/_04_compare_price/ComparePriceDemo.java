package com.hyx.advancededition._04_compare_price;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author 32596
 */
public class ComparePriceDemo {
    public static void main(String[] args) {
        // 方案一测试：串行方式操作商品比价 6.39
/*        ComparePriceService service = new ComparePriceService();
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice("iPhone");
        long end = System.currentTimeMillis();
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        System.out.println("priceResult = " + priceResult);*/

        // 方案二测试：使用Future+线程池增强并行 提高了任务处理的并行性 2.07
/*        ComparePriceService service = new ComparePriceService();
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice2("iPhone");
        long end = System.currentTimeMillis();
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        System.out.println("priceResult = " + priceResult);*/

        // 方案三测试：使用 CompletableFuture 进一步增强并行 1.06 !!!
/*        ComparePriceService service = new ComparePriceService();
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice3("iPhone");
        long end = System.currentTimeMillis();
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        System.out.println("priceResult = " + priceResult);*/
    }
}
