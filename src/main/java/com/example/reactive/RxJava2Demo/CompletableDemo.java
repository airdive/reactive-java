package com.example.reactive.RxJava2Demo;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @description : Completable测试
 * @author: liuchuang
 * @date: 2018/8/27 下午6:11
 * @modified by:
 */
@Slf4j
public class CompletableDemo {

    /**
     * 除了timer()、delay()等有限的API，不支持subscribeOn()
     */

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
                        System.out.println(Thread.currentThread().getName()+"-----发射");
                        e.onComplete();
                    }
                })
//                .subscribeOn(Schedulers.newThread())
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

}
