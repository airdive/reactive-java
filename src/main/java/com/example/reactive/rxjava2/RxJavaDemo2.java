package com.example.reactive.rxjava2;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

/**
 * @description : TODO
 * @author: liuchuang
 * @date: 2018/8/23 下午5:44
 * @modified by:
 */
@Slf4j
public class RxJavaDemo2 {

    public static void main(String[] args) {
        test1();
        System.out.println();
        test2();
        System.out.println();
        test3();

    }

    //线程
    static void test1(){
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                log.info( "Observable thread is : " + Thread.currentThread().getName());
                log.info( "emit 1");
                emitter.onNext(1);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                log.info( "Observer thread is :" + Thread.currentThread().getName());
                log.info( "onNext: " + integer);
            }
        };
        observable.subscribe(consumer);
    }

    static void test2(){
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                log.info( "Observable thread is : " + Thread.currentThread().getName());
                log.info( "emit 1");
                emitter.onNext(1);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                log.info( "Observer thread is :" + Thread.currentThread().getName());
                log.info( "onNext: " + integer);
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(consumer);

    }

    static void test3(){

    }

}
