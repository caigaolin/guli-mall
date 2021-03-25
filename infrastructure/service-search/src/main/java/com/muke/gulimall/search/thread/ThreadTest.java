package com.muke.gulimall.search.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author 木可
 * @version 1.0
 * @date 2021/3/15 9:09
 */
@Slf4j
public class ThreadTest {
    // 线程池
    public static ExecutorService execute = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main------------start");
        //new Thread01().start();

        //new Thread(new Thread02()).start();

        //Callable结合FutureTask开启线程，并可以得到线程执行结果返回值
        //FutureTask<Long> longFutureTask = new FutureTask<>(new Thread03());
        //new Thread(longFutureTask).start();
        // 如果需要获取线程的执行结果，程序会阻塞等待整个线程执行完成
        //System.out.println(longFutureTask.get());

        // 使用线程池创建线程
        //service.execute(new Thread02());

        //new ThreadPoolExecutor()

        // run方式创建异步编排对象
        /*CompletableFuture.runAsync(() -> {
            System.out.println("nice");
        }, execute);*/
        // supply方法创建异步编排对象
/*        CompletableFuture<Integer> supply = CompletableFuture.supplyAsync(() -> {
            System.out.println("supply");
            return 1;
        }, execute).whenCompleteAsync((result, e) -> {
            int i = 10 / 0;
            System.out.println("whenCompleteAsync.....");
        }, execute).exceptionally(e -> {
            System.out.println("handlerException....");
            return 2;
        });*/

        /**
         * whenCompleteAsync：可以得到前置线程的返回值和异常，但不能对异常进行处理
         * exceptionally：可以得到前置线程的异常，并进行处理
         * handleAsync：可以得到前置线程的返回值和异常，并可以对异常进行处理
         */
/*        CompletableFuture<Integer> supply = CompletableFuture.supplyAsync(() -> {
            System.out.println("supply");
            return 1;
        }, execute).whenCompleteAsync((result, e) -> {
            //int i = 10 / 0;
            System.out.println("whenCompleteAsync.....");
        }, execute).handleAsync((result, ex) -> {
            if (result != null) {
                return result;
            }
            if (ex != null) {
                return  2;
            }
            return 10;
        }, execute);*/

        // 线程串行化 thenRun 与 thenRunAsync区别：不指定线程池，就是用前置线程来执行
        /**
         * thenRunAsync: 前置线程执行结束，就执行该线程，不能接受参数，没有返回值
         * thenAcceptAsync:                        可以接受参数，没有返回值
         * thenApplyAsync:                         可以接受参数，可以有返回值
         */
  /*      CompletableFuture<String> supply = CompletableFuture.supplyAsync(() -> {
            System.out.println("supply");
            return 1;
        }, execute).thenRunAsync(() -> {
            System.out.println("run......");
        }, execute).thenAcceptAsync(res -> {
            System.out.println("accept " + res);
        }, execute).thenApplyAsync(result -> {
            System.out.println("apply " + result);
            return "nice";
        }, execute);*/

/*        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("future01...");
            return 1;
        }, execute);

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("future02...start");
            try {
                Thread.sleep(3000);
                System.out.println("future02...end");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        }, execute);*/

        /**
         * 当f1,f2执行完成，再执行当前线程;不能接受f1,f2的返回值，没有返回值
         */
        /*f1.runAfterBothAsync(f2, () -> {
            System.out.println("future03...");
        }, execute);*/
        /**
         * 当f1,f2执行完成，再执行当前线程；能接受f1,f2的返回值，没有返回值
         */
       /* f1.thenAcceptBothAsync(f2, (r1, r2) -> {
            System.out.println("future03..." + r1 + r2);
        }, execute);*/

        /**
         * 当f1,f2执行完成，再执行当前线程；能接受f1,f2的返回值，有返回值
         */
        /*CompletableFuture<String> combineAsync = f1.thenCombineAsync(f2, (r1, r2) -> {
            return "nice..." + r1 + "..." + r2;
        }, execute);*/

        /**
         * 当f1,f2其中一个执行完成，就会执行当前线程，不能感知参数，没有返回值
         */
        /*f1.runAfterEitherAsync(f2, () -> {
            System.out.println("f3执行。。。");
        }, execute);*/
        /*
        * 当f1,f2其中一个执行完成，就会执行当前线程，可以感知参数，没有返回值
        */
        /*f1.acceptEitherAsync(f2, r -> {
            System.out.println("f3执行。。。" + r);
        }, execute);*/
        /**
         * 当f1,f2其中一个执行完成，就会执行当前线程，可以感知参数，有返回值
         */
       /* CompletableFuture<String> eitherAsync = f1.applyToEitherAsync(f2, r -> {
            System.out.println("f3执行。。。" + r);
            return "nice..." + r;
        }, execute);*/


        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("商品图片执行。。。");
            return "hello.jpg";
        }, execute);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("商品属性执行。。。");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "黑色";
        }, execute);

        CompletableFuture<String> futureCate = CompletableFuture.supplyAsync(() -> {
            System.out.println("商品分类执行。。。");
            return "手机";
        }, execute);

        // allOf：所有线程执行完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureCate);
        System.out.println(futureImg.get());
        System.out.println(futureAttr.get());
        System.out.println(futureCate.get());
        // anyOf：某一个线程执行完成
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureCate);
        System.out.println(anyOf.get());
//        System.out.println(eitherAsync.get());
        System.out.println("main------------end");
    }

    /**
     * 通过继承Thread类的方式，开启线程
     */
    static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("thread----------" + Thread.currentThread().getName());
        }
    }

    /**
     * 通过实现Runnable接口的方式，开启线程
     */
    static class Thread02 implements Runnable {
        @Override
        public void run() {
            System.out.println("thread----------" + Thread.currentThread().getName());
        }
    }

    /**
     * 通过实现Callable接口，开启线程
     */
    static class Thread03 implements Callable<Long> {
        @Override
        public Long call() throws Exception {
            System.out.println("thread----------" + Thread.currentThread().getName());

            return Thread.currentThread().getId();
        }
    }

}
