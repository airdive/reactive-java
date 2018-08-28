package com.example.reactive.RxJava2Demo;

import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;

/**
 * @description : Flowable
 * @author: liuchuang
 * @date: 2018/8/25 下午7:25
 * @modified by:
 */
@Slf4j
public class FlowableDemo {

    /**
     * BackpressureStrategy背压策略
     * 当上游发送数据的速度快于下游接收数据的速度，且运行在不同的线程中时，Flowable通过自身特有的异步缓存池，
     * 来缓存没来得及处理的数据，缓存池的容量上限为128
     * ERROR
     * 在此策略下，如果放入Flowable的异步缓存池中的数据超限了，则会抛出MissingBackpressureException异常。
     *
     * DROP
     * 在此策略下，如果Flowable的异步缓存池满了，会丢掉上游发送的数据。
     *
     * LATEST
     * 与Drop策略一样，如果缓存池满了，会丢掉将要放入缓存池中的数据，不同的是，不管缓存池的状态如何，
     * LATEST都会将最后一条数据强行放入缓存池中。
     *
     * BUFFER
     * Flowable处理背压的默认策略，此策略下，Flowable的异步缓存池同Observable的一样，
     * 没有固定大小，可以无限制向里添加数据，不会抛出MissingBackpressureException异常，但会导致OOM。
     *
     * MISSING
     * 此策略表示，通过Create方法创建的Flowable没有指定背压策略，当下游处理速度跟不上时会报MissingBackpressureException
     * 下游可以通过背压操作符（onBackpressureBuffer()/onBackpressureDrop()/onBackpressureLatest()）
     * 指定背压策略。
     *
     *
     * 缓存池中数据的清理，并不是Subscriber接收一条，便清理一条，而是每累积到95条清理一次。
     *
     */


    /**
     * Observable不支持背压
     */
    @Test
    public void observable_not_backpressure() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        int i = 0;
                        while (true) {
                            i++;
                            e.onNext(i);
                        }
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Thread.sleep(5000);
                        System.out.println(integer);
                    }
                });
    }

    /**
     * BUFFER
     * 此策略下，Flowable的异步缓存池同Observable的一样，没有固定大小，可以无限制向里添加数据，
     * 不会抛出MissingBackpressureException异常，但会导致OOM。
     */
    @Test
    public void flowable_buffer() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        System.out.println("发射----> 1");
                        e.onNext(1);
                        System.out.println("发射----> 2");
                        e.onNext(2);
                        System.out.println("发射----> 3");
                        e.onNext(3);
                        System.out.println("发射----> 完成");
                        e.onComplete();
                    }
                }, BackpressureStrategy.BUFFER) //设置背压策略
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {   //onSubscribe回调的参数不是Disposable而是Subscription
                        s.request(Long.MAX_VALUE);            //设置观察者最多能处理的消息数
                    }
                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("接收----> " + integer);
                    }
                    @Override
                    public void onError(Throwable t) {
                    }
                    @Override
                    public void onComplete() {
                        System.out.println("接收----> 完成");
                    }
                });
    }

    /**
     *
     */
    @Test
    public void flowable_error() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        for (int i = 1; i <= 200; i++) {
                            System.out.println("发射--"+i);
                            e.onNext(i);
                            try {
                                Thread.sleep(100);//每隔100毫秒发射一次数据
                            } catch (Exception ignore) {
                            }
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.ERROR)
                .subscribe(new FlowableSubscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }
                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignore) {
                            log.debug(ignore.getMessage());
                        }
                        System.out.println("接收---->"+integer);
                    }
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                    @Override
                    public void onComplete() {
                        System.out.println("接收----> 完成");
                    }
                });
    }

    /**
     * 使用DROP策略时，会丢弃来不及缓存的数据
     * 如果Flowable的异步缓存池满了，会丢掉上游发送的数据。
     */
    @Test
    public void flowable_drop() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + "开始发射数据" + System.currentTimeMillis());
                        for (int i = 1; i <= 300; i++) {
                            System.out.println(threadName + "发射---->" + i);
                            e.onNext(i);
                            try {
                                Thread.sleep(100);//每隔100毫秒发射一次数据
                            } catch (Exception ignore) {
                            }
                        }
                        System.out.println(threadName + "发射数据结束" + System.currentTimeMillis());
                        e.onComplete();
                    }
                }, BackpressureStrategy.DROP)
//                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }
                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(300);//每隔300毫秒接收一次数据
                        } catch (InterruptedException ignore) {
                        }
                        System.out.println(Thread.currentThread().getName() + "接收---------->" + integer);
                    }
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName() + "接收----> 完成");
                    }
                });
    }


    /**
     * LATEST策略
     * 与Drop策略一样，如果缓存池满了，会丢掉将要放入缓存池中的数据，不同的是，
     * 不管缓存池的状态如何，LATEST都会将最后一条数据强行放入缓存池中。
     */
    @Test
    public void flowable_latest() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + "开始发射数据" + System.currentTimeMillis());
                        for (int i = 1; i <= 500; i++) {
                            System.out.println(threadName + "发射---->" + i);
                            e.onNext(i);
                            try {
                                Thread.sleep(100);
                            } catch (Exception ignore) {
                            }
                        }
                        System.out.println(threadName + "发射数据结束" + System.currentTimeMillis());
                        e.onComplete();

                    }
                }, BackpressureStrategy.LATEST)
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);            //注意此处，暂时先这么设置
                    }

                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ignore) {
                        }
                        System.out.println(Thread.currentThread().getName() + "接收---------->" + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName() + "接收----> 完成");
                    }
                });
    }

    /**
     * MISSING
     * 此策略表示，通过Create方法创建的Flowable没有指定背压策略，不会对通过OnNext发射的数据做缓存
     * 或丢弃处理，需要下游通过背压操作符（onBackpressureBuffer()/onBackpressureDrop()
     * /onBackpressureLatest()）指定背压策略。
     *
     */
    @Test
    public void flowable_missing() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(threadName + "开始发射数据" + System.currentTimeMillis());
                        for (int i = 1; i <= 350; i++) {
                            System.out.println(threadName + "发射---->" + i);
                            e.onNext(i);
                            try {
                                Thread.sleep(100);
                            } catch (Exception ignore) {
                            }
                        }
                        System.out.println(threadName + "发射数据结束" + System.currentTimeMillis());
                        e.onComplete();

                    }
                }, BackpressureStrategy.MISSING)
                .onBackpressureDrop()
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);            //注意此处，暂时先这么设置
                    }

                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ignore) {
                        }
                        System.out.println(Thread.currentThread().getName() + "接收---------->" + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName() + "接收----> 完成");
                    }
                });
    }


    /**
     * onBackpressureDrop
     * 当被观察者和观察者运行在同一线程中是，背压策略失效
     */
    @Test
    public void flowable_onBackpressure_singleThread() {
        Flowable.range(0, 500)
                .onBackpressureDrop()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<Integer>() {
                               @Override
                               public void accept(@NonNull Integer integer) throws Exception {
//                                   Thread.sleep(100);
                                   log.info(Thread.currentThread().getName() + "接收-----------" + integer);

                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                               }
                           },
                            new Action() {
                            @Override
                            public void run() throws Exception {
                                System.out.println("接受完成");
                            }
                        });
    }

    /**
     * onBackpressureDrop
     * 异步缓存池在被观察者和观察者处于不同线程时才会启用
     */
    @Test
    public void flowable_onBackpressure_multiThread() {
        final CountDownLatch count = new CountDownLatch(1);
        Flowable.range(0, 300)
//                .onBackpressureBuffer()
//                .onBackpressureLatest()
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }
                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignore) {
                        }
                        System.out.println(Thread.currentThread().getName() + "接收---------->" + integer);
                    }
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        count.countDown();
                    }
                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName() + "接收----> 完成");
                        count.countDown();
                    }
                });
        try {
            count.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置request
     */
    @Test
    public void flowable_request() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        e.onNext(1);
                        e.onNext(2);
                        e.onNext(3);
                        e.onNext(4);
                        e.onNext(5);
                        e.onNext(6);
                        e.onComplete();
                    }
                }, BackpressureStrategy.BUFFER)
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(2);//设置Subscriber的消费能力为2
                        //两次设置，请求数会重新开始计数
                        s.request(3);
                    }
                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("接收----> " + integer);
                    }
                    @Override
                    public void onError(Throwable t) {
                    }
                    @Override
                    public void onComplete() {
                        System.out.println("接收----> 完成");
                    }
                });
    }

    /**
     * 上游会一直发射，不管下游请求数
     * e.requested()随着下游接收数据而递减
     */
    @Test
    public void flowable_buffer_request() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        for (int i = 1; i <= 5; i++) {
                            System.out.println("当前未完成的请求数量-->" + e.requested());
                            System.out.println(threadName + "发射---->" + i);
                            e.onNext(i);
                        }
                        System.out.println(threadName + "发射数据结束" + System.currentTimeMillis());
                        e.onComplete();
                    }
                }, BackpressureStrategy.BUFFER)//上下游运行在同一线程中
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(3);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println(Thread.currentThread().getName() + "接收---------->" + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName() + "接收----> 完成");
                    }
                });
    }

    /**
     * 设置ERROR时,下游需求跟不上缓存池存入速度，最终异步缓存池超限，会导致MissingBackpressureException异常。
     */
    @Test
    public void flowable_error_request() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        for (int i = 1; i < 130; i++) {
                            System.out.println(threadName+"发射---->" + i);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.ERROR)
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        //设置<95和>95
                        s.request(96);
                    }
                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("接收------>" + integer);
                    }
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("接收------>完成");
                    }
                });
    }


    /**
     * Observable，内存占用暴增
     */
    @Test
    public void flowable_observable_oom() {
        Observable
                .create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                        int i = 0;
                        while (true) {
                            i++;
                            System.out.println("发射---->" + i);
                            e.onNext(i);
                        }
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }
                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(50);
                            System.out.println("接收------>" + integer);
                        } catch (InterruptedException ignore) {
                        }
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onComplete() {
                        System.out.println("接收------>完成");
                    }
                });
    }

    /**
     * 自定义背压策略
     */
    @Test
    public void flowable_custom_strategy() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        int i = 0;
                        while (true) {
                            //下游需求数归零时不发送
                            if (e.requested() == 0) continue;
                            System.out.println("发射---->" + i);
                            i++;
                            e.onNext(i);
                        }
                    }
                }, BackpressureStrategy.MISSING)
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);            //设置初始请求数据量为1
                        subscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(50);
                            System.out.println("接收------>" + integer);
                            subscription.request(1);//每接收到一条数据增加一条请求量
                        } catch (InterruptedException ignore) {
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * BUFFER
     *
     */
    @Test
    public void flowable_buffer_map() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        for (int i = 1; i < 130; i++) {
                            System.out.println(threadName+"发射---->" + i);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.BUFFER) //设置背压策略
//                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        System.out.println(Thread.currentThread().getName()+"处理---"+integer);
                        return " : "+integer;
                    }
                })
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {   //onSubscribe回调的参数不是Disposable而是Subscription
                        s.request(Long.MAX_VALUE);            //设置观察者最多能处理的消息数
                    }
                    @Override
                    public void onNext(String str) {
                        System.out.println(Thread.currentThread().getName()+"接收----> " + str);
                    }
                    @Override
                    public void onError(Throwable t) {
                    }
                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName()+"接收----> 完成");
                    }
                });
    }


    /**
     *
     */
    @Test
    public void flowable_onBack() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        int i = 0;
                        while (true) {
                            Thread.sleep(100);
                            System.out.println("发射---->" + i);
                            i++;
                            e.onNext(i);
                        }
                    }
                }, BackpressureStrategy.MISSING)
                .onBackpressureDrop()
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);            //设置初始请求数据量为1
                        subscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(200);
                            System.out.println("接收----------->" + integer);
                            subscription.request(1);//每接收到一条数据增加一条请求量
                        } catch (InterruptedException ignore) {
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    @Test
    public void flowable_range_onBack() {
        Flowable
                .range(1,650)
                .onBackpressureDrop()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    private Subscription subscription;
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);            //设置初始请求数据量为1
                        subscription = s;
                    }
                    @Override
                    public void onNext(Integer integer) {
                        try {
                            Thread.sleep(100);
                            System.out.println("接收----------->" + integer);
                            subscription.request(1);//每接收到一条数据增加一条请求量
                        } catch (InterruptedException ignore) {
                            ignore.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void flowable_fromCallable() throws InterruptedException {
        Flowable.fromCallable(() -> {
            System.out.println(Thread.currentThread().getName()+"-----发射");
            Thread.sleep(1000); //  imitate expensive computation
            return "Done";
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(new FlowableSubscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(String s) {
                        System.out.println(Thread.currentThread().getName()+"处理-----"+s);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName()+"处理完成");
                    }
                });

        Thread.sleep(2000);
    }


    /**
     * 使用FlowableSubscriber作为订阅者
     */
    @Test
    public void flowable_FlowableSubscriber() {
        Flowable
                .create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                        String threadName = Thread.currentThread().getName();
                        for (int i = 1; i < 130; i++) {
                            System.out.println(threadName+"发射---->" + i);
                            e.onNext(i);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.BUFFER) //设置背压策略
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new FlowableSubscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println(Thread.currentThread().getName()+"处理-----"+integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println(Thread.currentThread().getName()+"处理完成");
                    }
                });
    }


}
