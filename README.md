# CompletableFuture入门

## 学习目标

- 了解 CompletableFuture 的优点
- 掌握创建异步任务
  - 创建异步任务的2种方式
  - 知道异步任务中线程池的作用
  - 理解异步编程思想
- 掌握异步任务回调
  - thenApply / thenAccept / thenRun 3类方法使用和区别
  - 解锁一系列Async版本回调（thenXxxAsync）
- 掌握异步任务编排
  - 会对2个异步任务的依赖关系、并行关系进行编排
  - 会对n个任务的合并进行编排
- 掌握异步任务的异常处理
  - 会对异步任务进行异常处理
  - 会对回调链上对单个异步任务的异常进行现场恢复

## 课程学习说明

- 熟悉多线程理论知识
- 接触过 Future 和 线程池 的经历
- 会使用Lambda表达式和 Stream-API

### 1、Future vs CompletableFuture

#### 1.1 准备工作

为了便于后续更好地调试和学习，我们需要定义一个工具类辅助我们对知识的理解。

```Java
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    public static String readFile(String pathToFile) {
        try {
            return Files.readString(Paths.get(pathToFile));
        } catch(Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void sleepMillis(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleepSecond(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printThreadLog(String message) {
        String result = new StringJoiner(" | ")
                .add(String.valueOf(System.currentTimeMillis()))
                .add(String.format("%2d",Thread.currentThread().getId()))
                .add(String.valueOf(Thread.currentThread().getName()))
                .add(message)
                .toString();
        System.out.println(result);
    }
}
```

#### 1.2 Future 的局限性

需求：替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中

```Java
public class FutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(5);
        // 1. 读取敏感词汇 -> thread1
        Future<String[]> filterWordsFuture = executor.submit(() -> {
            String str = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = str.split(",");
            return filterWords;
        });

        // 2.读取新闻稿 -> thread2
        Future<String> filterNewsFuture = executor.submit(() -> {
            return CommonUtils.readFile("news.txt");
        });

        // 3.替换操作 -> thread3
        Future<String> replaceFuture = executor.submit(() -> {
            String[] words = filterWordsFuture.get();
            String news = filterNewsFuture.get();
            for (String word : words) {
                if (news.indexOf(word) >= 0) {
                    news = news.replace(word, "**");
                }
            }
            return news;
        });

        // 4.打印输出替换后的新闻稿 -> thread main
        String replaceContent = replaceFuture.get();
        System.out.println(replaceContent);
    }
}
```

通过上面的代码，我们会发现，Future相比于所有任务都直接在主线程处理，有很多优势，但同时也存在不足，至少表现如下：

- **在没有阻塞的情况下，无法对Future的结果执行进一步的操作**。Future不会告知你它什么时候完成，你如果想要得到结果，必须通过一个get()方法，该方法会阻塞直到结果可用为止。 它不具备将回调函数附加到Future后并在Future的结果可用时自动调用回调的能力。
- **无法解决任务相互依赖的问题**。filterWordFuture和newsFuture的结果不能自动发送给replaceFuture，需要在replaceFuture中手动获取，所以使用Future不能轻而易举地创建异步工作流。
- **不能将多个Future合并在一起**。假设你有多种不同的Future，你想在它们全部并行完成后然后再运行某个函数，Future很难独立完成这一需要。
- **没有异常处理**。Future提供的方法中没有专门的API应对异常处理，还是需要开发者自己手动异常处理。

#### 1.3 CompletableFuture 的优势

![img](https://ecncy6fcvw5p.feishu.cn/space/api/box/stream/download/asynccode/?code=NGVlNjViNWE1MzdkMDljNjE0MjAyNzEwOWUxY2JjYWJfMUpRWmRjR093SjJLalRBY05HSkpaVkpIaXVsNGpQVzBfVG9rZW46SGw2QWJ0NXJEb3pGQUR4U2VzWGNoRHNCbkljXzE3MjU2MzUwMTY6MTcyNTYzODYxNl9WNA)

**CompletableFuture** 实现了**Future**和**CompletionStage**接口

> CompletionStage直译是完成阶段，如果你发现你有这么一个需求：一个大的任务可以拆分成多个子任务，并且子任务之间有明显的先后顺序或者一个子任务依赖另一个子任务完成的结果时，那么CompletionStage是一个不错的选择，有点像聚合任务的特点，但Completion可以实现比聚会任务复杂得多的任务交互，CompletionStage就是实现了将一个大任务分成若个子任务，这些子任务基于一定的并行、串行组合形成任务的不同阶段，CompletionStage接口实现了对这些子任务之间的关系定义，

CompletableFuture 相对于 Future 具有以下优势：

- 为快速创建、链接依赖和组合多个Future提供了大量的便利方法。
- 提供了适用于各种开发场景的回调函数，它还提供了非常全面的异常处理支持。
- 无缝衔接和亲和 lambda 表达式 和 Stream - API 。
- 我见过的真正意义上的异步编程，把异步编程和函数式编程、响应式编程多种高阶编程思维集于一身，设计上更优雅。

### 2、创建异步任务

#### 2.1 runAsync

如果你要异步运行某些耗时的后台任务,并且不想从任务中返回任何内容，则可以使用`CompletableFuture.runAsync()`方法。它接受一个Runnable接口的实现类对象，方法返回`CompletableFuture<Void>` 对象

```Java
static CompletableFuture<Void> runAsync(Runnable runnable);
```

演示案例：开启一个不从任务中返回任何内容的CompletableFuture异步任务

```Java
public class RunAsyncDemo {
    public static void main(String[] args) {
        // runAsync 创建异步任务
        CommonUtils.printThreadLog("main start");
        // 使用Runnable匿名内部类
        CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                CommonUtils.printThreadLog("读取文件开始");
                // 使用睡眠来模拟一个长时间的工作任务(例如读取文件，网络请求等)
                CommonUtils.sleepSecond(3);
                CommonUtils.printThreadLog("读取文件结束");
            }
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSecond(4); //  此处休眠为的是等待CompletableFuture背后的线程池执行完成。
        CommonUtils.printThreadLog("main end");
    }
}
```

我们也可以以Lambda表达式的形式传递Runnable接口实现[类对象](https://so.csdn.net/so/search?q=类对象&spm=1001.2101.3001.7020)

```Java
public class RunAsyncDemo2 {
    public static void main(String[] args) {
        // runAsync 创建异步任务
        CommonUtils.printThreadLog("main start");
        // 使用Lambda表达式
        CompletableFuture.runAsync(() -> {
            CommonUtils.printThreadLog("读取文件开始");
            CommonUtils.sleepSecond(3);
            CommonUtils.printThreadLog("读取文件结束");
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

需求：使用CompletableFuture开启异步任务读取 news.txt 文件中的新闻稿，并打印输出。

```Java
public class RunAsyncDemo3 {
    public static void main(String[] args) {
        // 需求：使用多线程异步读取 words.txt 中的敏感词汇，并打印输出。
        CommonUtils.printThreadLog("main start");

        CompletableFuture.runAsync(()->{
            String news = CommonUtils.readFile("news.txt");
            CommonUtils.printThreadLog(news);
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

在后续的章节中，我们会经常使用Lambda表达式。

#### 2.2 supplyAsync

CompletableFuture.runAsync() 开启不带返回结果异步任务。但是，如果您想从后台的异步任务中返回一个结果怎么办？此时，CompletableFuture.supplyAsync()是你最好的选择了。

```Java
static CompletableFuture<U> supplyAsync(Supplier<U> supplier)
```

它入参一个 Supplier 供给者，用于供给带返回值的异步任务 并返回`CompletableFuture<U>`，其中U是供给者给程序供给值的类型。

需求：开启异步任务读取 news.txt 文件中的新闻稿，返回文件中内容并在主线程打印输出

```Java
public class SupplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                String news = CommonUtils.readFile("news.txt");
                return news;
            }
        });

        CommonUtils.printThreadLog("here are not blocked,main continue");
        // 阻塞并等待newsFuture完成
        String news = newsFuture.get();
        CommonUtils.printThreadLog("news = " + news);
        CommonUtils.printThreadLog("main end");
    }
}
```

如果想要获取newsFuture结果，可以调用completableFuture.get()方法，get()方法将阻塞，直到newsFuture完成。

我们依然可以使用Java 8的Lambda表达式使上面的代码更简洁。

```Java
CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
    String news = CommonUtils.readFile("news.txt");
    return news;
});
```

#### 2.3 异步任务中的线程池

大家已经知道，`runAsync()`和`supplyAsync()`方法都是开启单独的线程中执行异步任务。但是，我们从未创建线程对吗？ 不是吗！

CompletableFuture 会从全局的`ForkJoinPool.commonPool()` 线程池获取线程来执行这些任务

当然，你也可以创建一个线程池，并将其传递给`runAsync()`和`supplyAsync()`方法，以使它们在从您指定的线程池获得的线程中执行任务。

CompletableFuture API中的所有方法都有两种变体,一种是接受传入的`Executor`参数作为指定的线程池，而另一种则使用默认的线程池 (`ForkJoinPool.commonPool()` ) 。

```Java
// runAsync() 的重载方法 
static CompletableFuture<Void>  runAsync(Runnable runnable)
static CompletableFuture<Void>  runAsync(Runnable runnable, Executor executor)
// supplyAsync() 的重载方法 
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

需求：指定线程池，开启异步任务读取 news.txt 中的新闻稿，返回文件中内容并在主线程打印输出

```Java
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletableFuture<String> newsFuture = CompletableFuture.supplyAsync(() -> {
    CommonUtils.printThreadLog("异步读取文件开始");
    String news = CommonUtils.readFile("news.txt");
    CommonUtils.printThreadLog("异步读取文件完成");
    return news;
},executor);
```

> 最佳实践：创建属于自己的业务线程池
>
> 如果所有CompletableFuture共享一个线程池，那么一旦有异步任务执行一些很慢的 I/O 操作，就会导致线程池中所有线程都阻塞在 I/O 操作上，从而造成线程饥饿，进而影响整个系统的性能。
>
> 所以，强烈建议你要根据不同的业务类型创建不同的线程池，以避免互相干扰。
>
> *不同类型的任务跑在不同的线程池，防止伪死锁*

#### 2.4 异步编程思想

​     综合上述，看到了吧，我们没有显式地创建线程，更没有涉及线程通信的概念，整个过程根本就没涉及线程知识吧，以上专业的说法是：**线程的创建和线程负责的任务进行解耦，它给我们带来的好处线程的创建和启动全部交给线程池负责，具体任务的编写就交给程序员，专人专事。**

​       **异步编程**是可以让程序并行( 也可能是并发 )运行的一种手段，其可以让程序中的一个工作单元作为异步任务与主线程分开独立运行，并且在异步任务运行结束后，会通知主线程它的运行结果或者失败原因，毫无疑问，一个异步任务其实就是开启一个线程来完成的，使用异步编程可以提高应用程序的性能和响应能力等。

作为开发者，只需要有一个意识：

​      开发者只需要把耗时的操作交给CompletableFuture开启一个异步任务，然后继续关注主线程业务，当异步任务运行完成时会通知主线程它的运行结果。我们把具备了这种编程思想的开发称为**异步编程思想**。

### 3、异步任务回调

​      `CompletableFuture.get()`方法是阻塞的。调用时它会阻塞等待 直到这个Future完成，并在完成后返回结果。 但是，很多时候这不是我们想要的。

​       对于构建异步系统，我们应该能够将**回调**附加到CompletableFuture上，当这个Future完成时，该回调应自动被调用。 这样，我们就不必等待结果了，然后在Future的回调函数内编写完成Future之后需要执行的逻辑。 您可以使用`thenApply()`，`thenAccept()`和`thenRun()`方法，它们可以把回调函数附加到CompletableFuture

#### 3.1 thenApply

使用 `thenApply()` 方法可以处理和转换CompletableFuture的结果。 它以Function<T，R>作为参数。 Function<T，R>是一个函数式接口，表示一个转换操作，它接受类型T的参数并产生类型R的结果

```Java
CompletableFuture<R> thenApply(Function<T,R> fn)
```

需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组( 敏感词数组 )，异步任务返回敏感词数组

```Java
/**
 * hyx
 * @author 32596
 */
public class ThenApplyDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组( 敏感词数组 )，异步任务返回敏感词数组
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> readFilesFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words.txt文件");
            return CommonUtils.readFile("filter_words.txt");
        });
        CompletableFuture<String[]> filterWordsFuture = readFilesFuture.thenApply(content -> {
            CommonUtils.printThreadLog("读取过滤词");
            String[] filterWords = content.split("-");
            return filterWords;
        });
        CommonUtils.printThreadLog("here is not blocked main continue");
        String[] filterWords = filterWordsFuture.get();
        CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * thenApply(Function<T,R>) 可以对异步任务的结果进一步应用Function转换
         * 转换后的结果可以在主线程获取，也可以进行下一步的操作。
         */
    }

}
```

你还可以通过附加一系列`thenApply()`回调方法，在CompletableFuture上编写一系列转换序列。一个`thenApply()`方法的结果可以传递给序列中的下一个，如果你对链式操作很了解，你会发现结果可以在链式操作上传递。

```Java
/**
 * hyx
 * @author 32596
 * 通过链式操作使用 thenApply
 */
public class ThenApplyDemo2 {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，把内容转换成数组( 敏感词数组 )，异步任务返回敏感词数组
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordsFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words.txt文件");
            return CommonUtils.readFile("filter_words.txt");
        }).thenApply(content -> {
            CommonUtils.printThreadLog("读取过滤词");
            String[] filterWords = content.split(",");
            return filterWords;
        });
        CommonUtils.printThreadLog("here is not blocked main continue");
        String[] filterWords = filterWordsFuture.get();
        CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
    }

}
```

#### 3.2 thenAccept

如果你不想从回调函数返回结果，而只想在Future完成后运行一些代码，则可以使用`thenAccept()`

这些方法是入参一个 Consumer<T>，它可以对异步任务的执行结果进行消费使用，方法返回CompletableFuture。

```Java
CompletableFuture<Void> thenAccept(Consumer<T> action)
```

通常用作回调链中的最后一个回调。

需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，然后打印敏感词数组

```Java
/**
 * hyx
 * @author 32596
 */
public class ThenAcceptDemo {

    public static void main(String[] args) {
        // 需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，然后打印敏感词数组

        CommonUtils.printThreadLog("main start");

        CompletableFuture.supplyAsync(() -> {
            // 读取文件
            CommonUtils.printThreadLog("读取filter_words.txt文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenApply(content -> {
            // 转化成敏感词汇
            CommonUtils.printThreadLog("把文件内容转换成敏感词汇");
            String[] filterWordsContent = content.split(",");
            return filterWordsContent;
        }).thenAccept(filterWords -> { // 入参一个消费型接口
            CommonUtils.printThreadLog("filterWords=" + Arrays.toString(filterWords));
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSeconds(4L);
        CommonUtils.printThreadLog("main end");

        /**
         * 总结
         * thenAccept(Consumer<T> c) 可以对异步任务的结果进行消费使用
         * 返回返回一个不带结果的completableFuture对象
         */
    }
}
```

#### 3.3 thenRun

前面我们已经知道，通过thenApply( Function<T,R> ) 对链式操作中的上一个异步任务的结果进行转换，返回一个新的结果；通过thenAccept( Consumer ) 对链式操作中上一个异步任务的结果进行消费使用，不返回新结果；

如果我们只是想从CompletableFuture的链式操作得到一个完成的通知，甚至都不使用上一步链式操作的结果，那么 CompletableFuture.thenRun() 会是你最佳的选择，它需要一个Runnable并返回`CompletableFuture<Void>`。

```Java
CompletableFuture<Void> thenRun(Runnable action);
```

演示案例：我们仅仅想知道 filter_words.txt 的文件是否读取完成

```Java
/**
 * hyx
 *
 * @author 32596
 */
public class ThenRunDemo {
    public static void main(String[] args) {
        // 演示案例：我们仅仅想知道 filter_words.txt 的文件是否读取完成
        CommonUtils.printThreadLog("main start");

        CompletableFuture.supplyAsync(() -> {
            // 读取文件
            CommonUtils.printThreadLog("开始读取filter_words.txt文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenRun(() -> {
            CommonUtils.printThreadLog("文件读取完成");
        });

        CommonUtils.printThreadLog("main continue");
        CommonUtils.sleepSeconds(4L);
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * thenRun(Runnable action)
         * 当异步任务完成后，只想得到一个完成的通知，不使用上一步异步任务的结果，就可以使用thenRun
         * 通常会把它用在链式操作的末端
         */
    }
}
```

#### 3.4 更进一步提升并行化

CompletableFuture 提供的所有回调方法都有两个异步变体

```Java
CompletableFuture<U> thenApply(Function<T,U> fn)
// 回调方法的异步变体(异步回调)
CompletableFuture<U> thenApplyAsync(Function<T,U> fn)
CompletableFuture<U> thenApplyAsync(Function<T,U> fn, Executor executor)
```

注意：这些带了Async的异步回调 **通过在单独的线程中执行回调任务** 来帮助您进一步促进并行化计算。

回顾需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组，主线程获取结果打印输出这个数组

```Java
/**
 * hyx
 *
 * @author 32596
 */
public class ThenApplyAsyncDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {

/*            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;*/
            
            // 此时，立即返回结果
            return "尼玛, NB, tmd";
        }).thenApply((content) -> {
            /**
             * 一般而言，thenApply任务的执行和supplyAsync()任务执行可以使用同一线程执行
             * 如果supplyAsync()任务立即返回结果，则thenApply的任务在主线程中执行
             */
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        CommonUtils.printThreadLog("main continue");

        String[] filterWords = filterWordFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * 一般而言，commonPool 为了提高性能
         * thenApply 中回调任务和 supplyAsync 中的异步任务使用的是同一个线程
         * 特殊情况：
         * 如果 supplyAsync 中的任务是立即返回结果（不是耗时的任务），那么 thenApply 回调任务也会在主线程执行。
         */
    }
}
```

要更好地控制执行回调任务的线程，可以使用异步回调。如果使用`thenApplyAsync()`回调，那么它将在从`ForkJoinPool.commonPool()` 获得的另一个线程中执行

```Java
public class ThenApplyAsyncDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取filter_words文件");
            String filterWordsContent = CommonUtils.readFile("filter_words.txt");
            return filterWordsContent;
        }).thenApplyAsync((content) -> {
            CommonUtils.printThreadLog("把内容转换成敏感词数组");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        CommonUtils.printThreadLog("main continue");

        String[] filterWords = filterWordFuture.get();
        CommonUtils.printThreadLog("filterWords = " + Arrays.toString(filterWords));
        CommonUtils.printThreadLog("main end");
    }
}
```

以上程序一种可能的运行结果（需要多运行几次）：

```Java
1672885914481 |  1 | main | main start
1672885914511 | 16 | ForkJoinPool.commonPool-worker-1 | 读取filter_words.txt文件
1672885914511 |  1 | main | main continue
1672885914521 | 17 | ForkJoinPool.commonPool-worker-2 | 把内容转换成敏感词数组
1672885914521 |  1 | main | filterWords = [尼玛, NB, tmd]
1672885914521 |  1 | main | main end
```

此外，如果将Executor传递给`thenApplyAsync()`回调，则该回调的异步任务将在从Executor的线程池中获取的线程中执行;

```Java
ExecutorService executor = Executors.newFixedThreadPool(5);
CompletableFuture<String[]> filterWordFuture = CompletableFuture.supplyAsync(() -> {
    CommonUtils.printThreadLog("读取filter_words文件");
    String filterWordsContent = CommonUtils.readFile("filter_words.txt");
    return filterWordsContent;
}).thenApplyAsync((content) -> {
    CommonUtils.printThreadLog("把内容转换成敏感词数组");
    String[] filterWords = content.split(",");
    return filterWords;
},executor);
executor.shutdown();
```

其他两个回调的变体版本如下：

```Java
// thenAccept和其异步回调
CompletableFuture<Void>        thenAccept(Consumer<T> action)
CompletableFuture<Void>        thenAcceptAsync(Consumer<T> action)
CompletableFuture<Void>        thenAcceptAsync(Consumer<T> action, Executor executor)

// thenRun和其异步回调
CompletableFuture<Void>        thenRun(Runnable action)
CompletableFuture<Void>        thenRunAsync(Runnable action)
CompletableFuture<Void>        thenRunAsync(Runnable action, Executor executor)
```

### 4、异步任务编排

#### 4.1 编排2个依赖关系的异步任务 thenCompose()

回顾需求：异步读取 filter_words.txt 文件中的内容，读取完成后，转换成敏感词数组让主线程待用。

关于读取和解析内容，假设使用以下的 readFileFuture(String) 和 splitFuture(String) 方法完成。

```Java
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
```

现在，让我们先了解如何使用`thenApply()` 结果会发生什么

```Java
CompletableFuture<CompletableFuture<String[]>> future = readFileFuture("filter_words.txt")
    .thenApply((context) -> {
       return splitFuture(context);
    });
```

回顾在之前的案例中，`thenApply(Function<T,R>)`中Function回调会对上一步任务结果转换后得到一个简单值 ，但现在这种情况下，最终结果是嵌套的CompletableFuture，所以这是不符合预期的，那怎么办呢？

我们想要的是：把上一步异步任务的结果，转成一个CompletableFuture对象，这个CompletableFuture对象中包含本次异步任务处理后的结果。也就是说，**我们想组合上一步异步任务的结果到下一个新的异步任务中, 结果由这个新的异步任务返回**

此时，你需要使用`thenCompose()`方法代替，我们可以把它理解为 异步任务的组合

```Java
CompletableFuture<R> thenCompose(Function<T,CompletableFuture<R>> func)
```

所以，`thenCompose()`用来连接两个有依赖关系的异步任务，结果由第二个任务返回

```Java
CompletableFuture<String[]> future = readFileFuture("filter_words.txt")
    .thenCompose((context) -> { 
        return splitFuture(context);
    });
```

因此，这里积累了一个经验：

如果我们想连接( 编排 ) 两个依赖关系的异步任务( CompletableFuture 对象 ) ,请使用 thenCompose() 方法

当然，thenCompose 也存在异步回调变体版本：

```Java
CompletableFuture<R> thenCompose(Function<T,CompletableFuture<R>> fn)
    
CompletableFuture<R> thenComposeAsync(Function<T,CompletableFuture<R>> fn)
CompletableFuture<R> thenComposeAsync(Function<T,CompletableFuture<R>> fn, Executor executor)
```

#### 4.2 编排2个非依赖关系的异步任务 thenCombine()

我们已经知道，当其中一个Future依赖于另一个Future，使用`thenCompose()`用于组合两个Future。如果两个Future之间没有依赖关系，你希望两个Future独立运行并在两者都完成之后执行回调操作时，则使用`thenCombine()`;

```Java
// T是第一个任务的结果 U是第二个任务的结果 V是经BiFunction应用转换后的结果
CompletableFuture<V> thenCombine(CompletableFuture<U> other, BiFunction<T,U,V> func)
```

需求：替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中

```Java
/**
 * @author hyx
 */
public class ThenCombineDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 需求：替换新闻稿 ( news.txt ) 中敏感词汇 ，把敏感词汇替换成*，敏感词存储在 filter_words.txt 中
        CommonUtils.printThreadLog("main start");

        // 1.读取敏感词汇的文件并解析到数组中
        CompletableFuture<String[]> future1 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取敏感词汇的文件并解析到数组中");
            String content = CommonUtils.readFile("filter_words.txt");
            String[] filterWords = content.split(",");
            return filterWords;
        });

        // 2.读取news文件内容
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            CommonUtils.printThreadLog("读取news文件内容");
            String content = CommonUtils.readFile("news.txt");
            return content;
        });

        CompletableFuture<String> combineFuture = future1.thenCombine(future2, (words, content) -> {
            // 3.替换操作
            CommonUtils.printThreadLog("替换操作");
            for (String word : words) {
                if (content.contains(word)) {
                    content = content.replace(word, "***");
                }
            }
            return content;
        });
        CommonUtils.printThreadLog("main continue");
        String content = combineFuture.get();
        System.out.println("content=" + content);
        CommonUtils.printThreadLog("main end");
        /**
         * 总结
         * thenCombine 用于合并两个没有依赖关系的异步任务
         */
    }
}
```

注意：当两个`Future`都完成时，才将两个异步任务的结果传递给`thenCombine()`的回调函数做进一步处理。

和以往一样，thenCombine 也存在异步回调变体版本

```Java
CompletableFuture<V> thenCombine(CompletableFuture<U> other, BiFunction<T,U,V> func)
CompletableFuture<V> thenCombineAsync(CompletableFuture<U> other, BiFunction<T,U,V> func)
CompletableFuture<V> thenCombineAsync(CompletableFuture<U> other, BiFunction<T,U,V> func,Executor executor)
```

#### 4.3 合并多个异步任务 allOf / anyOf

我们使用`thenCompose()`和`thenCombine()`将两个CompletableFuture组合和合并在一起。

如果要编排任意数量的CompletableFuture怎么办？可以使用以下方法来组合任意数量的CompletableFuture

```Java
public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs)
public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs)
```

`CompletableFuture.allOf()`用于以下情形中：有多个需要独立并行运行的Future，并在所有这些Future 都完成后执行一些操作。

需求：统计news1.txt,news2.txt,news3.txt 文件中包含 CompletableFuture 关键字的文件的个数

```Java
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
```

顾名思义，当给定的多个异步任务中的有任意Future一个完成时，需要执行一些操作，可以使用 anyOf 方法

演示案例：anyOf 执行过程

```Java
public class AnyOfDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            Tools.sleepMillis(2);
            return "Future1的结果";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            Tools.sleepMillis(1);
            return "Future2的结果";
        });

        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            Tools.sleepMillis(3);
            return "Future3的结果";
        });

        CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(future1, future2, future3);

        // 输出Future2的结果
        System.out.println(anyOfFuture.get());
    }
}
```

在上面的示例中，当三个CompletableFuture中的任意一个完成时，anyOfFuture就完成了。 由于future2的睡眠时间最少，因此它将首先完成，最终结果将是"Future2的结果"。

注意：

- `anyOf()` 方法返回类型必须是 `CompletableFuture <Object>`。
- `anyOf()`的问题在于，如果您拥有返回不同类型结果的CompletableFuture，那么您将不知道最终CompletableFuture的类型。

### 5、异步任务的异常处理

在前面的章节中，我们并没有更多地关心异常处理的问题，其实，CompletableFuture 提供了优化处理异常的方式。

首先，让我们了解**异常如何在回调链中传播**。

```Java
/**
 * @author hyx
 */
public class ExceptionChainDemo {
    public static void main(String[] args) {
        // 异常如何在回调链中传播
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            // int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {
            CommonUtils.printThreadLog(result);

            String str = null;
            int length = str.length();

            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).thenAccept(result -> {
            CommonUtils.printThreadLog(result);
        });
        /**
         * 如果回调链中出现任何异常，回调链不再向下执行，立即转入异常处理
         */
    }
}
```

如果在 supplyAsync 任务中出现异常，后续的 thenApply 和 thenAccept 回调都不会执行，CompletableFuture 将转入异常处理

如果在第一个 thenApply 任务中出现异常，第二个 thenApply 和 最后的 thenAccept 回调不会被执行，CompletableFuture 将转入异常处理，依次类推。

#### 5.1 exceptionally()

exceptionally 用于处理回调链上的异常，回调链上出现的任何异常，回调链不继续向下执行，都在exceptionally中处理异常。

```Java
// Throwable表示具体的异常对象e
CompletableFuture<R> exceptionally(Function<Throwable, R> func)
/**
 * @author hyx
 */
public class ExceptionallyDemo {
    public static void main(String[] args) {
        // exceptionally 处理回调链上的异常
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
//             int r = 1 / 0;
            return "result1";
        }).thenApply(result -> {

            String str = null;
            int length = str.length();

            return result + " result2";
        }).thenApply(result -> {
            return result + " result3";
        }).exceptionally(ex -> {
            System.out.println("出现异常"+ex.getMessage());
            return "UnKnown";
        });
    }
}
```

因为exceptionally只处理一次异常，所以常常用在回调链的末端。

#### 5.2 handle()

CompletableFuture API 还提供了一种更通用的方法 `handle()` 表示从异常中恢复

`handle()`常常被用来恢复回调链中的一次特定的异常，回调链恢复后可以进一步向下传递。

```Java
CompletableFuture<R> handle(BiFunction<T, Throwable, R> fn)
/**
 * @author hyx
 */
public class HandleDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // handle():常常被用来恢复回调链中的一次特定的异常，回调链恢复后可以进一步向下传递。
        CommonUtils.printThreadLog("main start");

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            int i = 1 / 0;
            return "result1";
        }).handle((result, ex) -> {
            CommonUtils.printThreadLog("上一步异常的恢复");
            if (ex != null) {
                CommonUtils.printThreadLog("出现异常：" + ex.getMessage());
                return "UnKnown";
            }
            return result;
        });

        CommonUtils.printThreadLog("main continue");
        String rs = future.get();
        CommonUtils.printThreadLog("rs=" + rs);
        CommonUtils.printThreadLog("main end");
        /**
         * 异步任务不管是否发生异常，handle方法都会执行。
         * 所以 handle 方法的核心作用在于对上一步异步任务进行现场修复
         */
    }
}
```

如果发生异常，则res参数将为null，否则ex参数将为null。

需求：对回调链中的一次异常进行恢复处理

```Java
public class HandleExceptionDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            int r = 1 / 0;
            return "result1";
        }).handle((ret, ex) -> {
            if (ex != null) {
                System.out.println("我们得到异常：" + ex.getMessage());
                return "Unknown1";
            }
            return ret;
        }).thenApply(result -> {
            String str = null;
            int len = str.length();
            return result + " result2";
        }).handle((ret, ex) -> {
            if (ex != null) {
                System.out.println("我们得到异常：" + ex.getMessage());
                return "Unknown2";
            }
            return ret;
        }).thenApply(result -> {
            return result + " result3";
        });

        String ret = future.get();
        Tools.printThreadLog("最终结果:" + ret);
    }
}
```

和以往一样，为了提升并行化，异常处理可以方法单独的线程执行，以下是它们的异步回调版本

```Java
CompletableFuture<R> exceptionally(Function<Throwable, R> fn)
CompletableFuture<R> exceptionallyAsync(Function<Throwable, R> fn)  // jdk17+
CompletableFuture<R> exceptionallyAsync(Function<Throwable, R> fn,Executor executor) // jdk17+

CompletableFuture<R> handle(BiFunction<T,Throwable,R> fn)
CompletableFuture<R> handleAsync(BiFunction<T,Throwable,R> fn)
CompletableFuture<R> handleAsync(BiFunction<T,Throwable,R> fn, Executor executor)
```

# CompletableFuture进阶

## 学习内容

- 异步任务的交互
- get方法和join方法区别
- CompletableFuture 在流式编程（Stream API）的优势
- CompletableFutrue实战应用之大数据商品比价

## 学习目标

- 掌握异步任务的交互操作
- 了解get方法和join方法区别
- 掌握CompletableFuture 结合Stream API进阶应用
- 掌握CompletableFuture 在实战中的应用

### 1、[异步任务](https://so.csdn.net/so/search?q=异步任务&spm=1001.2101.3001.7020)的交互

异步任务交互指将异步任务获取结果的**速度相比较**，按一定的规则( **先到先用** )进行下一步处理。

#### 1.1 applyToEither

`applyToEither()` 把两个异步任务做比较，异步任务先到结果的，就对先到的结果进行下一步的操作。

```Java
CompletableFuture<R> applyToEither(CompletableFuture<T> other, Function<T,R> func)
```

演示案例：使用最先完成的异步任务的结果

```Java
/**
 * @author hyx
 */
public class ApplyToEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 异步任务交互,applyToEither() 把两个异步任务做比较，异步任务先到结果的，就对先到的结果进行下一步的操作。
        // 异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int i = new Random().nextInt(30);
            CommonUtils.sleepSeconds((long) i);
            CommonUtils.printThreadLog("任务1耗时" + i + "秒");
            return i;
        });

        // 异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int g = new Random().nextInt(3);
            CommonUtils.sleepSeconds((long) g);
            CommonUtils.printThreadLog("任务2耗时" + g + "秒");
            return g;
        });

        // 哪个异步任务结果先到达，使用哪个异步任务的结果
        CompletableFuture<Integer> future3 = future1.applyToEither(future2, result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
            return result;
        });

        Integer result = future3.get();
        System.out.println("result:" + result);
        /**
         * 异步任务指两个交互任务，哪个结果先到，就使用哪个结果（ 先到先用 ）
         */
    }
}
```

速记心法：任务1、任务2就像两辆公交，哪路公交先到，就乘坐(使用)哪路公交。

以下是applyToEither 和其对应的异步回调版本

```Java
CompletableFuture<R> applyToEither(CompletableFuture<T> other, Function<T,R> func)
CompletableFuture<R> applyToEitherAsync(CompletableFuture<T> other, Function<T,R> func)
CompletableFuture<R> applyToEitherAsync(CompletableFuture<T> other, Function<T,R> func,Executor executor)
```

#### 1.2 acceptEither

`acceptEither()` 把两个异步任务做比较，异步任务先到结果的，就对先到的结果进行下一步操作 ( 消费使用 )。

```Java
CompletableFuture<Void> acceptEither(CompletableFuture<T> other, Consumer<T> action)
CompletableFuture<Void> acceptEitherAsync(CompletableFuture<T> other, Consumer<T> action)  
CompletableFuture<Void> acceptEitherAsync(CompletableFuture<T> other, Consumer<T> action,Executor executor)
```

演示案例：使用最先完成的异步任务的结果

```Java
public class AcceptEitherDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 异步任务交互
        CommonUtils.printThreadLog("main start");
        // 开启异步任务1
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int x = new Random().nextInt(3);
            CommonUtils.sleepSecond(x);
            CommonUtils.printThreadLog("任务1耗时:" + x + "秒");
            return x;
        });

        // 开启异步任务2
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            int y = new Random().nextInt(3);
            CommonUtils.sleepSecond(y);
            CommonUtils.printThreadLog("任务2耗时:" + y + "秒");
            return y;
        });

        // 哪些异步任务的结果先到达，就使用哪个异步任务的结果
        future1.acceptEither(future2,result -> {
            CommonUtils.printThreadLog("最先到达的结果:" + result);
        });

        // 主线程休眠4秒，等待所有异步任务完成
        CommonUtils.sleepSecond(4);
        CommonUtils.printThreadLog("main end");
    }
}
```

#### 1.3 runAfterEither

如果不关心最先到达的结果，只想在有一个异步任务先完成时得到完成的通知，可以使用 `runAfterEither()` ，以下是它的相关方法：

```Java
CompletableFuture<Void> runAfterEither(CompletableFuture<T> other, Runnable action)
CompletableFuture<Void>        runAfterEitherAsync(CompletableFuture<T> other, Runnable action)
CompletableFuture<Void>        runAfterEitherAsync(CompletableFuture<T> other, Runnable action, Executor executor)
```

> 提示
>
> 异步任务交互的三个方法和之前学习的异步的回调方法 thenApply、thenAccept、thenRun 有异曲同工之妙。

### 2、get() 和 join() 区别

get() 和 join() 都是CompletableFuture提供的以阻塞方式获取结果的方法。

那么该如何选用呢？请看如下案例：

```Java
public class GetOrJoinDemo {
    public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "hello";
        });

        String ret = null;
        // 抛出检查时异常，必须处理
        try {
            ret = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("ret = " + ret);

        // 抛出运行时异常，可以不处理
        ret = future.join();
        System.out.println("ret = " + ret);
    }
}
```

使用时，我们发现，get() 抛出检查时异常 ，需要程序必须处理；而join() 方法抛出运行时异常，程序可以不处理。所以，join() 更适合用在流式编程中。

### 3、ParallelStream VS CompletableFuture

CompletableFuture 虽然提高了任务并行处理的能力，如果它和 Stream API 结合使用，能否进一步多个任务的并行处理能力呢？

同时，对于 Stream API 本身就提供了并行流ParallelStream，它们有什么不同呢？

我们将通过一个耗时的任务来体现它们的不同，更重要地是，我们能进一步加强 CompletableFuture 和 Stream API 的结合使用，同时搞清楚CompletableFuture 在流式操作的优势

需求：创建10个MyTask耗时的任务，统计它们执行完的总耗时

定义一个MyTask类，来模拟耗时的长任务

```Java
public class MyTask {
    private int duration;

    public MyTask(int duration) {
        this.duration = duration;
    }

    // 模拟耗时的长任务
    public int doWork() {
        CommonUtils.printThreadLog("doWork");
        CommonUtils.sleepSecond(duration);
        return duration;
    }
}
```

同时，我们创建10个任务，每个持续1秒。

```Java
IntStream intStream = IntStream.range(0, 10);
List<MyTask> tasks = intStream.mapToObj(item -> {
    return new MyTask(1);
}).collect(Collectors.toList());
```

#### 3.1 并行流的局限

我们先使用串行执行，让所有的任务都在主线程 main 中执行。

```Java
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
```

它花费了10秒, 因为每个任务在主线程一个接一个的执行。

因为涉及 Stream API，而且存在耗时的长任务，所以，我们可以使用 `parallelStream()`

```Java
public class ParallelStreamDemo {
    public static void main(String[] args) {
        // 并行流的局限性
        // 需求：创建10个 MyTask 耗时的任务,统计他们执行完的总耗时
        // 方案二：使用并行流
        // 1.创建10个 MyTask 对象，每个对象持续1s，存入List集合
        // {0,1,2,3,4,5,6,7,8,9}
        IntStream intStream = IntStream.range(0, 10);
        List<MyTask> tasks = intStream.mapToObj(item -> {
            return new MyTask(1);
        }).toList();

        // 2.执行10个 MyTask ，统计总耗时
        long start = LocalDateTime.now().getSecond();
//        List<Long> collect = tasks.stream().map(MyTask::doWork).toList();
        // 区别：使用了 parallelStream
        List<Long> collect = tasks.parallelStream().map(myTask -> {
            return myTask.doWork();
        }).toList();
        long end = LocalDateTime.now().getSecond();
        double costTime = end - start;

        System.out.printf("processed %d tasks %.2f second",tasks.size(),costTime);
    }
}
```

它花费了2秒多，因为此次并行执行使用了8个线程 (7个是ForkJoinPool线程池中的, 一个是 main 线程)，需要注意是：运行结果由自己电脑CPU的核数决定。

#### 3.2 CompletableFuture 在流式操作的优势

让我们看看使用`CompletableFuture`是否执行的更有效率

```Java
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
         */
    }
}
```

运行发现，两者使用的时间大致一样。能否进一步优化呢？

CompletableFutures 比 ParallelStream 优点之一是你可以指定Executor去处理任务。你能选择更合适数量的线程。我们可以选择大于Runtime.getRuntime().availableProcessors() 数量的线程，如下所示：

```Java
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
```

测试代码时，电脑配置是4核8线程，而我们创建的线程池中线程数最少也是10个，所以，每个线程负责一个任务( 耗时1s )，总体来说，处理10个任务总共需要约1秒。

#### 3.3 合理配置线程池中的线程数

正如我们看到的，CompletableFuture 可以更好地控制线程池中线程的数量，而 ParallelStream 不能。

问题1：如何选用 CompletableFuture 和 ParallelStream ？

如果你的任务是IO密集型的，你应该使用CompletableFuture；

如果你的任务是CPU密集型的，使用比处理器更多的线程是没有意义的，所以选择ParallelStream ，因为它不需要创建线程池，更容易使用。

问题2：IO密集型任务和CPU密集型任务的区别？

**CPU密集型**也叫计算密集型，此时，系统运行时大部分的状况是CPU占用率近乎100%，I/O在很短的时间就可以完成，而CPU还有许多运算要处理，CPU 使用率很高。比如说要计算1+2+3+…+ 10万亿、天文计算、圆周率后几十位等， 都是属于CPU密集型程序。

CPU密集型任务的特点：大量计算，CPU占用率一般都很高，I/O时间很短

**IO密集型**指大部分的状况是CPU在等I/O (硬盘/内存) 的读写操作，但CPU的使用率不高。

简单的说，就是需要大量的输入输出，例如读写文件、传输文件、网络请求。

IO密集型任务的特点：大量网络请求，文件操作，CPU运算少，很多时候CPU在等待资源才能进一步操作。

问题3：既然要控制线程池中线程的数量，多少合适呢？

**如果是CPU密集型任务，就需要尽量压榨CPU，参考值可以设为 Ncpu+1**

**如果是IO密集型任务，参考值可以设置为 2 \* Ncpu，其中Ncpu 表示 核心数。**

注意的是：以上给的是参考值，详细配置超出本次课程的范围，选不赘述。

### 4、大数据商品比价

#### 4.1 需求描述和分析

需求描述： 实现一个大数据比价服务，价格数据可以从京东、天猫、拼多多等平台去获取指定商品的价格、优惠金额，然后计算出实际付款金额 ( 商品价格 - 优惠金额 )，最终返回价格最优的平台与价格信息。

#### 4.2 构建工具类和实体类

定义价格实体类 PriceResult

```Java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceResult {
    private int price;                        // 平台价格
    private int discount;                // 折扣
    private int realPrice;                // 最终价
    private String platform;        // 商品平台

    public PriceResult(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "PriceResult{" +
                "平台=" + platform +
                ", 价格=" + price +
                ", 折扣=" + discount +
                ", 最终价=" + realPrice +
                '}';
    }
}
```

修改工具类 CommonUtils，添加 getCurrentTime() 方法获取当前时间并格式化，修改 printThreadLog() 方法，把时间戳替换成当前时间。

```Java
public class CommonUtils {

    private static String getCurrentTime() {
        LocalTime time = LocalTime.now();
        return time.format(DateTimeFormatter.ofPattern("[HH:mm:ss.SSS]"));
    }

    // 打印输出带线程信息的日志
    public static void printThreadLog(String message) {
        // 时间戳 | 线程id | 线程名 | 日志信息
        String result = new StringJoiner(" | ")
                .add(getCurrentTime())
                .add(String.format("%2d", Thread.currentThread().getId()))
                .add(Thread.currentThread().getName())
                .add(message)
                .toString();
        System.out.println(result);
    }
}
```

#### 4.3 构建 HttpRequest

HttpRequest 用于模拟网络请求 ( 耗时的操作 )

```Java
public class HttpRequest {

    // 获取指定商品的淘宝价
    public static PriceResult getTaoBaoPrice(String productName) {
        CommonUtils.printThreadLog("获取淘宝上" + productName + "价格");
        mockCostTimeOperation();

        PriceResult priceResult = new PriceResult("淘宝");
        priceResult.setPrice(5199);
        CommonUtils.printThreadLog("获取淘宝上" + productName + "价格完成：5199");
        return priceResult;
    }
    // 获取指定商品的淘宝优惠
    public static int getTaoBaoDiscount(String productName) {
        CommonUtils.printThreadLog("获取淘宝上" + productName + "优惠");
        mockCostTimeOperation();
        CommonUtils.printThreadLog("获取淘宝上" + productName + "优惠完成：-200");
        return 200;
    }

    // 获取指定商品的JD价
    public static PriceResult getJDongPrice(String productName) {
        CommonUtils.printThreadLog("获取京东上" + productName + "价格");
        mockCostTimeOperation();

        PriceResult priceResult = new PriceResult("京东");
        priceResult.setPrice(5299);
        CommonUtils.printThreadLog("获取京东上" + productName + "价格完成：5299");
        return priceResult;
    }
    // 获取指定商品的JD优惠
    public static int getJDongDiscount(String productName) {
        CommonUtils.printThreadLog("获取京东上" + productName + "优惠");
        mockCostTimeOperation();
        CommonUtils.printThreadLog("获取京东上" + productName + "优惠完成：-150");
        return 150;
    }


    // 获取指定商品的拼多多价
    public static PriceResult getPDDPrice(String productName) {
        CommonUtils.printThreadLog("获取拼多多上" + productName + "价格");
        mockCostTimeOperation();

        PriceResult priceResult = new PriceResult("拼多多");
        priceResult.setPrice(5399);
        CommonUtils.printThreadLog("获取拼多多上" + productName + "价格完成：5399");
        return priceResult;
    }
    // 获取指定商品的拼多多优惠
    public static int getPDDDiscount(String productName) {
        CommonUtils.printThreadLog("获取拼多多上" + productName + "优惠");
        mockCostTimeOperation();
        CommonUtils.printThreadLog("获取拼多多上" + productName + "优惠完成：-5300");
        return 5300;
    }

    // 模拟耗时的操作
    private static void mockCostTimeOperation() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

#### 4.4 使用串行方式操作商品比价

```Java
public class ComparePriceService {

    // 方案一：串行方式操作商品比价
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

        Stream<PriceResult> stream = Stream.of(taoBaoPriceResult, jDongPriceResult, pddPriceResult);
        Optional<PriceResult> minOpt = stream.min(Comparator.comparing(PriceResult::getRealPrice));
        return minOpt.get();
    }


    // 计算商品的最终价格 = 平台价格 - 优惠价
    public PriceResult computeRealPrice(PriceResult priceResult,int discount) {
        priceResult.setRealPrice(priceResult.getPrice() - discount);
        priceResult.setDiscount(discount);
        LogUtils.printLog(priceResult.getPlatform() + "最终价格计算完成:" + priceResult.getRealPrice());
        return priceResult;
    }
}
```

使用串行方式在main线程中执行的测试类

```Java
public class ComparePriceDemo {
    public static void main(String[] args) {
        // 方案一测试：串行方式操作商品比价
        ComparePriceService service = new ComparePriceService();

        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice("iPhone");
        long end = System.currentTimeMillis();
        
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        
        System.out.println("priceResult = " + priceResult);
    }
}
```

#### 4.5 使用Future+线程池增强并行

```Java
public class ComparePriceService {

    // 方案二：使用Future+线程池增强并行
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
                        return future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();
    }
}
```

使用Future+线程池的方式的测试类

```Java
public class ComparePriceDemo {
    public static void main(String[] args) {
        ComparePriceService service = new ComparePriceService();
        // 方案二测试：使用Future+线程池增强并行
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice2("iPhone");
        long end = System.currentTimeMillis();
        
        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);
        
        System.out.println("priceResult = " + priceResult);
    }
}
```

#### 4.6 使用 CompletableFuture 进一步增强并行

```Java
public class ComparePriceService {

    // 方案三：使用 CompletableFuture 进一步增强并行
    public PriceResult getCheapestPlatformPrice3(String productName) {
        // 获取淘宝平台的价格和优惠
        CompletableFuture<PriceResult> taoBaoCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getTaoBaoPrice(productName))
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getTaoBaoDiscount(productName)), this::computeRealPrice);

        // 获取京东平台的价格和优惠
        CompletableFuture<PriceResult> jDongCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getJDongPrice(productName))
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getJDongDiscount(productName)), this::computeRealPrice);
        // 获取拼多多平台的价格和优惠
        CompletableFuture<PriceResult> pddCF = CompletableFuture
                .supplyAsync(() -> HttpRequest.getPDDPrice(productName))
                .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getPDDDiscount(productName)), this::computeRealPrice);

        return Stream.of(taoBaoCF,jDongCF,pddCF)
                .map(CompletableFuture::join)
                .min(Comparator.comparing(PriceResult::getRealPrice))
                .get();

    }
}
```

使用CompletableFuture方案的测试类

```Java
public class ComparePriceDemo {
    public static void main(String[] args) {
        ComparePriceService service = new ComparePriceService();

        // 方案三测试：使用 CompletableFuture 进一步增强并行
        long start = System.currentTimeMillis();
        PriceResult priceResult = service.getCheapestPlatformPrice3("iPhone");
        long end = System.currentTimeMillis();

        double costTime = (end - start) / 1000.0;
        System.out.printf("cost %.2f second \n",costTime);

        System.out.println("priceResult = " + priceResult);
    }
}
```

#### 4.7 Stream API 操作批量商品比价

```Java
public class ComparePriceService {

    public PriceResult batchComparePrice(List<String> products) {
        // 遍历每个商品，根据商品开启异步任务获取最终价，然后归集到List集合
        List<CompletableFuture<PriceResult>> completableFutures = products.stream()
                .map(product -> {
                    return CompletableFuture
                            .supplyAsync(() -> HttpRequest.getTaoBaoPrice(product))
                            .thenCombine(CompletableFuture.supplyAsync(() -> HttpRequest.getTaoBaoDiscount(product)), this::computeRealPrice);
                }).collect(Collectors.toList());

        // 把多个商品的最终价进行排序比较获取最小值
        return completableFutures
                .stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(PriceResult::getRealPrice))
                .findFirst()
                .get();
    }
}
```

批量商品比价查询测试类

```Java
public class ComparePriceDemo {
    public static void main(String[] args) {
        ComparePriceService service = new ComparePriceService();
        // 测试在一个平台比较同款产品(iPhone14)不同色系的价格
        List<String> products = Arrays.asList("iPhone14黑色", "iPhone14白色", "iPhone14玫瑰红");
        PriceResult priceResult = service.batchComparePrice(products);
        System.out.println("priceResult = " + priceResult);
    }
}
```