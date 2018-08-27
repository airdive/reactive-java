package com.example.reactive.RxJava2Demo;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * @description : TODO
 * @author: liuchuang
 * @date: 2018/8/26 下午11:34
 * @modified by:
 */
@Slf4j
public class SingleCompletableMaybeDemo {

    /**
     * Single
     * 只发射一条单一的数据，或者一条异常通知，不能发射完成通知，其中数据与通知只能发射一个。
     *
     * Completable
     * 只发射一条完成通知，或者一条异常通知，不能发射数据，其中完成通知与异常通知只能发射一个
     *
     * Maybe
     * 可发射一条单一的数据，以及发射一条完成通知，或者一条异常通知，其中完成通知和异常通知只
     * 能发射一个，发射数据只能在发射完成通知或者异常通知之前，否则发射数据无效。
     *
     */

    /**
     *
     */
    @Test
    public void single_success() {
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        e.onSuccess(0);
                    }
                })
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println("接收-----"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }


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

    @Test
    public void single_scheduler() {
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        e.onSuccess(0);
                    }
                })
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
    }

    /**
     * 发射完成消息
     */
    @Test
    public void completable_complete() {
        Completable
                .create(new CompletableOnSubscribe() {
                    @Override
                    public void subscribe(CompletableEmitter e) throws Exception {
                        e.onComplete();
                    }
                })
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }


    /**
     * 发射异常通知
     */
    @Test
    public void completable_error() {
        Completable
                .create(new CompletableOnSubscribe() {
                    @Override
                    public void subscribe(CompletableEmitter e) throws Exception {
                        e.onError(new Exception("测试异常"));
                    }
                })
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("异常通知");
                        e.printStackTrace();
                    }
                });
    }


    /**
     *
     */
    @Test
    public void completable_scheduler() {
        Completable
                .create(new CompletableOnSubscribe() {
                    @Override
                    public void subscribe(CompletableEmitter e) throws Exception {
                        e.onComplete();
                    }
                })
                .observeOn(Schedulers.computation())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName()+"执行完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 单条数据和完成通知不能同时发射，以先发射的为准
     */
    @Test
    public void maybe_success_complete() {
        Maybe
                .create(new MaybeOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(MaybeEmitter<Integer> e) throws Exception {
//                        e.onSuccess(1);
                        e.onComplete();
                        e.onSuccess(1);
                    }
                })
                .subscribe(new MaybeObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println("处理--"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }
                });
    }


    /**
     * 发射单条数据和异常通知
     */
    @Test
    public void maybe_success_error() {
        Maybe
                .create(new MaybeOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                        e.onSuccess(1);
                        e.onError(new Exception("测试异常"));
                    }
                })
                .subscribe(new MaybeObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println("处理---"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }
                });
    }

    /**
     * 发射完成通知和异常通知
     */
    @Test
    public void maybe_complete_error() {
        Maybe
                .create(new MaybeOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                        e.onComplete();
                        e.onError(new Exception("测试异常"));
                    }
                })
                .subscribe(new MaybeObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println("处理---"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }
                });
    }


    /**
     * 发射单条数据，使用map，Scheduler
     */
    @Test
    public void maybe_success_scheduler() {
        Maybe
                .create(new MaybeOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"发射-->"+1);
                        e.onSuccess(1);
                    }
                })
                .observeOn(Schedulers.computation())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"处理---->"+integer);
                        return integer+1;
                    }
                })
                .observeOn(Schedulers.single())
                .subscribe(new MaybeObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println(Thread.currentThread().getName()+"接收-->"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }
                });
    }


    /**
     * 发射单条数据，使用map中抛出异常
     */
    @Test
    public void maybe_success_throw() {
        Maybe
                .create(new MaybeOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"发射-->"+1);
                        e.onSuccess(1);
                    }
                })
                .observeOn(Schedulers.computation())
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"处理---->"+integer);
                        if (integer==1){
                            throw new Exception(Thread.currentThread().getName()+"--map处理异常");
                        }
                        return integer+1;
                    }
                })
                .observeOn(Schedulers.single())
                .subscribe(new MaybeObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        System.out.println(Thread.currentThread().getName()+"接收-->"+integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完成");
                    }
                });
    }
}
