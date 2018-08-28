package com.example.reactive.RxJava2Demo;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @description : Observable测试
 * @author: liuchuang
 * @date: 2018/8/24 下午3:26
 * @modified by:
 */
@Slf4j
public class ObservableDemo {

    /**
     * Observable basic
     */
    @Test
    public void demo1() {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("Hello RxJava");
                emitter.onComplete();
            }
        });

        Observer<String> observer = new Observer<String>() {
            private Disposable disposable;
            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }
            @Override
            public void onNext(String s) {
                System.out.println(s);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("接受完成");
            }
        };

        observable.subscribe(observer);
    }

    /**
     * chain
     */
    @Test
    public void demo2() {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        emitter.onNext("Hello World");
                        emitter.onComplete();
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("接受完成");
                    }
                });
    }

    /**
     * 通过just方法创建Observable，依次发射接收的参数，最后发射onComplete
     * Consumer是对Observer的简化，只接收可观察对象发射的数据，不接收异常信息或完成信息。
     */
    @Test
    public void demo3() {
        Observable.just("Hello World")
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

    /**
     * 指定onNext、onError、onComplete事件的消费者
     */
    @Test
    public void demo4() {
        Observable.just("Hello World")
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                }, (throwable) -> {
                        throwable.printStackTrace();
                }, ()-> {
                        System.out.println("接受完成");
                });
    }

    /**
     * subscribe中遍历List
     */
    @Test
    public void demo5() {
        final List<String> list = Arrays.asList("A","B","C");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                try {
                    for (String str : list) {
                        log.info("subscribe: "+Thread.currentThread().getName());
                        emitter.onNext(str);
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
                emitter.onComplete();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(String s) {
                log.info("onNext: "+Thread.currentThread().getName());
                System.out.println(s);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("接受完成");
            }
        });
    }

    /**
     * 将可迭代的集合转换成Observable，集合中的元素依次发射
     */
    @Test
    public void demo6() {
        final List<String> list = Arrays.asList("A","B","C");
        Observable
                .fromIterable(list)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

    /**
     * Observer订阅时会产生一个Disposable对象
     */
    @Test
    public void demo7() {
        final List<String> list = Arrays.asList("A","B","C");
        Disposable disposable1 = Observable.just("Hello World")
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                        Thread.sleep(2000);
                    }
                });
        System.out.println("disposable1: "+disposable1.isDisposed());
        System.out.println("-------------------------");
        Disposable disposable2 = Observable
                .fromIterable(list)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

    /**
     * Observer主动中断订阅
     */
    @Test
    public void demo8() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; i < 10; i++) {
                    System.out.println("发送" + i);
                    emitter.onNext(i);
                }
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("接收" + integer);
                if (integer > 4) {
                    log.info("----------------");
                    disposable.dispose();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("数据接受完成");
            }
        });
    }
}
