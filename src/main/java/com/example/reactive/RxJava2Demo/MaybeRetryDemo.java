package com.example.reactive.RxJava2Demo;

import io.reactivex.Maybe;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @description : TODO
 * @author: liuchuang
 * @date: 2018/8/27 下午4:34
 * @modified by:
 */
@Slf4j
public class MaybeRetryDemo {
    @Test
    public void retryTimesPredicateWithMatchingPredicate() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);

        Maybe.fromCallable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                int count = numberOfSubscribeCalls.incrementAndGet();
                log.info("call调用次数"+count);
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }

                throw new IllegalArgumentException();
            }
        })
                //Integer.MAX_VALUE为重试的最大次数
                .retry(Integer.MAX_VALUE, new Predicate<Throwable>() {
                    @Override public boolean test(final Throwable throwable) throws Exception {
                        //非IllegalArgumentException异常，即重试
                        return !(throwable instanceof IllegalArgumentException);
                    }
                })
                .test()
                .assertFailure(IllegalArgumentException.class);
        log.info("atomicInteger="+atomicInteger.get()+", numberOfSubscribeCalls="+numberOfSubscribeCalls.get());
        assertEquals(3, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithMatchingRetryAmount() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);

        Maybe.fromCallable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                int count = numberOfSubscribeCalls.incrementAndGet();
                log.info("call调用次数"+count);
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }

                return true;
            }
        })
                .retry(2, Functions.alwaysTrue())
                .test()
                .assertResult(true);
        log.info("atomicInteger="+atomicInteger.get()+", numberOfSubscribeCalls="+numberOfSubscribeCalls.get());
        assertEquals(3, numberOfSubscribeCalls.get());
    }


    @Test
    public void retryTimesPredicateWithNotMatchingRetryAmount() {
        final AtomicInteger atomicInteger = new AtomicInteger(3);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);

        Maybe.fromCallable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                int count = numberOfSubscribeCalls.incrementAndGet();
                log.info("call调用次数"+count);
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }

                return true;
            }
        })
                .retry(1, Functions.alwaysTrue())
                .test()
                .assertFailure(RuntimeException.class);
        log.info("atomicInteger="+atomicInteger.get()+", numberOfSubscribeCalls="+numberOfSubscribeCalls.get());
        assertEquals(2, numberOfSubscribeCalls.get());
    }

    @Test
    public void retryTimesPredicateWithZeroRetries() {
        final AtomicInteger atomicInteger = new AtomicInteger(2);
        final AtomicInteger numberOfSubscribeCalls = new AtomicInteger(0);

        Maybe.fromCallable(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                int count = numberOfSubscribeCalls.incrementAndGet();
                log.info("call调用次数"+count);
                if (atomicInteger.decrementAndGet() != 0) {
                    throw new RuntimeException();
                }

                return true;
            }
        })
                .retry(0, Functions.alwaysTrue())
                .test()
                .assertFailure(RuntimeException.class);
        log.info("atomicInteger="+atomicInteger.get()+", numberOfSubscribeCalls="+numberOfSubscribeCalls.get());
        assertEquals(1, numberOfSubscribeCalls.get());
    }

}
