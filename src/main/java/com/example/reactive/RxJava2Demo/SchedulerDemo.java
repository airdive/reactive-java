package com.example.reactive.RxJava2Demo;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @description : Scheduler测试
 * @author: liuchuang
 * @date: 2018/8/24 下午5:31
 * @modified by:
 */
@Slf4j
public class SchedulerDemo {

    /**
     * Scheduler种类
     * Schedulers.io( )：
     * 用于IO密集型的操作，例如读写SD卡文件，查询数据库，访问网络等，具有线程缓存机制，在此调度器接收到任务后，先检查线程缓存池中，
     * 是否有空闲的线程，如果有，则复用，如果没有则创建新的线程，并加入到线程池中，如果每次都没有空闲线程使用，可以无上限的创建新线程。
     *
     * Schedulers.newThread( )：
     * 在每执行一个任务时创建一个新的线程，不具有线程缓存机制，因为创建一个新的线程比复用一个线程更耗时耗力，虽然使用Schedulers.io( )的地方，
     * 都可以使用Schedulers.newThread( )，但是，Schedulers.newThread( )的效率没有Schedulers.io( )高。
     *
     * Schedulers.computation()：
     * 用于CPU 密集型计算任务，即不会被 I/O 等操作限制性能的耗时操作，例如xml,json文件的解析，Bitmap图片的压缩取样等，
     * 具有固定的线程池，大小为CPU的核数。不可以用于I/O操作，因为I/O操作的等待时间会浪费CPU。
     *
     * Schedulers.trampoline()：
     * 在当前线程立即执行任务，如果当前线程有任务在执行，则会将其暂停，等插入进来的任务执行完之后，再将未完成的任务接着执行。
     *
     * Schedulers.single()：
     * 拥有一个线程单例，所有的任务都在这一个线程中执行，当此线程中有任务执行时，其他任务将会按照先进先出的顺序依次执行。
     *
     * Scheduler.from(@NonNull Executor executor)：
     * 指定一个线程调度器，由此调度器来控制任务的执行策略。
     *
     * AndroidSchedulers.mainThread()：
     * 在Android UI线程中执行任务，为Android开发定制。
     *
     */

    /**
     * 不设置Scheduler
     */
    @Test
    public void demo() {
        Observable
                .create((e) -> {
                        for (int i = 0; i < 5; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                })
                .subscribe((i) -> {
                        System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                });
    }


    /**
     * 为Observable和Observer设置Scheduler
     * 不能为为Observable设置Scheduler，Why？
     */
    @Test
    public void demo1() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        for (int i = 0; i < 5; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                })
                //为Observable设置Scheduler
//                .subscribeOn(Schedulers.newThread())
                //为Observer设置ScheScheduler
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer i) throws Exception {
                        System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                    }
                });
    }

    /**
     * 多次设置Observer的Scheduler，每次都生效，以最后一次为准
     * 使用observeOn设置Scheduler后，后面的map和Observer若没有设置Scheduler，则以上一步的Scheduler为准
     */
    @Test
    public void demo2() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        for (int i = 0; i < 5; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                })
                //为map设置Scheduler
                .observeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer i) throws Exception {
                        System.out.println("处理线程:" + Thread.currentThread().getName() + "---->" + "处理:" + i);
                        return i;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer i) throws Exception {
                        System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                    }
                });
    }


    /**
     * 为map和Observer分别设置Scheduler
     */
    @Test
    public void demo3() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        for (int i = 0; i < 5; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                })
                //为map设置Scheduler
                .observeOn(Schedulers.io())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer i) throws Exception {
                        System.out.println("处理线程:" + Thread.currentThread().getName() + "---->" + "处理:" + i);
                        return i;
                    }
                })
                //为Observer设置ScheScheduler
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer i) throws Exception {
                        System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                    }
                });
    }

    /**
     * trampoline()
     */
    @Test
    public void demo4() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        for (int i = 0; i < 5; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                })
                .observeOn(Schedulers.trampoline())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer i) throws Exception {
                        Thread.sleep(2000);//休息2s后再处理数据
                        System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                    }
                });
    }

    /**
     * single()，单线程实例
     */
    @Test
    public void demo5() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        for (int i = 0; i < 3; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                })
                //指定map操作符在Schedulers.single()的线程中处理数据
                .observeOn(Schedulers.single())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer i) throws Exception {
                        System.out.println("处理线程:" + Thread.currentThread().getName() + "---->" + "处理:" + i);
                        return i;
                    }
                })
                //设置观察者在Schedulers.single()的线程中接收数据
                .observeOn(Schedulers.single())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer i) throws Exception {
                        System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                    }
                });
    }

    /**
     * io()，单线程实例
     * 使用Observable，当上下游运行在不同的线程中时，会产生背压问题，造成数据丢失
     */
    @Test
    public void demo6() {
        Observable
                .create(e -> {
                        for (int i = 0; i < 3; i++) {
                            System.out.println("发射线程:" + Thread.currentThread().getName() + "---->" + "发射:" + i);
                            Thread.sleep(1000);
                            e.onNext(i);
                        }
                        e.onComplete();
                })
                //指定map操作符在Schedulers.io()的线程中处理数据
//                .observeOn(Schedulers.io())
                .map(i -> {
                        System.out.println("处理线程:" + Thread.currentThread().getName() + "---->" + "处理:" + i);
                        Thread.sleep(2000);
                        return i;
                })
                //设置观察者在Schedulers.io()的线程中接收数据
//                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    Thread.sleep(1000);
                    System.out.println("接收线程:" + Thread.currentThread().getName() + "---->" + "接收:" + i);
                });
    }


}
