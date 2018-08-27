package com.example.reactive.RxJava2Demo;

import com.example.reactive.RxJava2Demo.exception.TestException;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @description : Maybe测试
 * @author: liuchuang
 * @date: 2018/8/27 下午4:56
 * @modified by:
 */
@Slf4j
public class MaybeDemo {

    /**
     * Maybe的API除了几个有限的timeout()，都不支持为被观察者设置Scheduler
     *
     */


    /**
     * onSuccess会多次发射，只有第一次有效
     */
    @Test
    public void basic() {
        final Disposable d = Disposables.empty();

        Maybe.create(new MaybeOnSubscribe<Integer>() {
            @Override
            public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                e.setDisposable(d);

                e.onSuccess(1);
                e.onSuccess(3);
                e.onError(new TestException());
                log.info(Thread.currentThread().getName()+": "+System.currentTimeMillis());
                e.onSuccess(2);
                e.onError(new TestException());
                e.onComplete();
            }
        })
                .test()
                .assertResult(1);

        assertTrue(d.isDisposed());
    }

    @Test
    public void basicWithCancellable() {
        final Disposable d1 = Disposables.empty();
        final Disposable d2 = Disposables.empty();

        Maybe.create(new MaybeOnSubscribe<Integer>() {
            @Override
            public void subscribe(MaybeEmitter<Integer> e) throws Exception {
                e.setDisposable(d1);
                d1.dispose();
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        d2.dispose();
                    }
                });

                e.onSuccess(1);
                e.onError(new TestException());
                e.onSuccess(2);
                e.onError(new TestException());
            }
        }).test().assertResult(1);

        assertTrue(d1.isDisposed());
        assertTrue(d2.isDisposed());
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
                .subscribeOn(Schedulers.newThread())
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
        Maybe.create(new MaybeOnSubscribe<Integer>() {
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
