package com.hyx.advancededition._03_parallelstream_completablefuture;

import com.hyx.utils.CommonUtils;

/**
 * @author hyx
 */
public class MyTask {

    private final int duration;
    public MyTask(int duration) {
        this.duration = duration;
    }

    // 模拟耗时的长任务
    public long doWork() {
        CommonUtils.printThreadLog("doWork");
        CommonUtils.sleepSeconds((long)duration);
        return duration;
    }
}