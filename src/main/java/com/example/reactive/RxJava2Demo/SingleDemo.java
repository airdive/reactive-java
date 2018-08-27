package com.example.reactive.RxJava2Demo;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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
     * 发射单条数据
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
                        System.out.println(Thread.currentThread().getName()+"接收-----"+integer);
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
     */
    @Test
    public void single_scheduler() {
        Single
                .create(new SingleOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(SingleEmitter<Integer> e) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"-----发射");
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
    }

}
