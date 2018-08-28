package com.example.reactive.RxJava2Demo;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @description : Single测试
 * @author: liuchuang
 * @date: 2018/8/26 下午11:34
 * @modified by:
 */
@Slf4j
public class SingleDemo {

    /**
     * Single
     * 只发射一条单一的数据，或者一条异常通知，不能发射完成通知，只会发射第一条数据或异常，其余的会取消。
     *
     * Completable
     * 只发射一条完成通知，或者一条异常通知，不能发射数据，其中完成通知与异常通知只能发射一个
     *
     * Maybe
     * 可发射一条单一的数据，以及发射一条完成通知，或者一条异常通知，只会发射第一条数据或异常，其余的会取消。
     *
     */

    /**
     * 发射单条数据
     */
    @Test
    public void single_success() throws InterruptedException {
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        e.onSuccess(0);
                        e.onSuccess(1);
                    }
                })
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println(Thread.currentThread().getName()+"接收-----"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("异常通知");
                        e.printStackTrace();
                    }
                });
        Thread.sleep(2000);
    }

    /**
     * 发射异常通知
     */
    @Test
    public void single_error() {
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        e.onError(new Exception("测试异常"));
                    }
                })
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("异常通知");
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 使用map操作符，设置Scheduler
     * 当为被观察者设置Scheduler时，被观察者和观察者将以守护线程的形式运行，当主线程结束，守护线程也将关闭
     */
    @Test
    public void single_scheduler() {
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"-----发射");
                        Thread.sleep(5000);
                        e.onSuccess(0);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"处理-----"+integer);
                        return integer+1;
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println(Thread.currentThread().getName()+"接收-----"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用CountDownLatch同步主线程和守护进程
     */
    @Test
    public void single_scheduler_countdown() {
        final CountDownLatch count = new CountDownLatch(1);
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"-----发射");
                        Thread.sleep(5000);
                        e.onSuccess(0);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"处理-----"+integer);

                        return integer+1;
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println(Thread.currentThread().getName()+"接收-----"+integer);
                        count.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        count.countDown();
                        e.printStackTrace();
                    }
                });
        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
