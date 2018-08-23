package com.example.reactive.rxjava2;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @description : TODO
 * @author: liuchuang
 * @date: 2018/8/23 下午5:19
 * @modified by:
 */
@Slf4j
public class RxJavaDemo1 {
    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
    }

    //订阅发布
    static void test1(){
        //创建一个上游 Observable：
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });
        //创建一个下游 Observer
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                log.info("subscribe");
            }

            @Override
            public void onNext(Integer value) {
                log.info( "" + value);
            }

            @Override
            public void onError(Throwable e) {
                log.info( "error");
            }

            @Override
            public void onComplete() {
                log.info( "complete");
            }
        };
        //建立连接
        observable.subscribe(observer);

    }

    //链式
    static void test2(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                log.info( "subscribe");
            }

            @Override
            public void onNext(Integer value) {
                log.info( "" + value);
            }

            @Override
            public void onError(Throwable e) {
                log.info( "error");
            }

            @Override
            public void onComplete() {
                log.info( "complete");
            }
        });
    }

    //中断订阅
    static void test3(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                log.info( "emit 1");
                emitter.onNext(1);
                log.info( "emit 2");
                emitter.onNext(2);
                log.info( "emit 3");
                emitter.onNext(3);
                log.info( "emit complete");
                emitter.onComplete();
                log.info( "emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Observer<Integer>() {
            private Disposable mDisposable;
            private int i;

            @Override
            public void onSubscribe(Disposable d) {
                log.info( "subscribe");
                mDisposable = d;
            }

            @Override
            public void onNext(Integer value) {
                log.info( "onNext: " + value);
                i++;
                if (i == 2) {
                    log.info( "dispose");
                    mDisposable.dispose();
                    log.info( "isDisposed : " + mDisposable.isDisposed());
                }
            }

            @Override
            public void onError(Throwable e) {
                log.info( "error");
            }

            @Override
            public void onComplete() {
                log.info( "complete");
            }
        });
    }

    //只订阅onNext事件
    static void test4() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                log.info( "emit 1");
                emitter.onNext(1);
                log.info( "emit 2");
                emitter.onNext(2);
                log.info( "emit 3");
                emitter.onNext(3);
                log.info( "emit complete");
                emitter.onComplete();
                log.info( "emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                log.info( "onNext: " + integer);
            }
        });
    }
}
