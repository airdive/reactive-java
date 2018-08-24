package com.example.reactive.rxjava2;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @description : TODO
 * @author: liuchuang
 * @date: 2018/8/24 下午2:45
 * @modified by:
 */
@Slf4j
public class FlowableDemo1 {

    @Test
    public void test1(){
        FlowableOnSubscribe<Integer> flowableOnSubscribe = flowableEmitter -> flowableEmitter.onNext(1);
        Flowable<Integer> integerFlowable = Flowable.create(flowableOnSubscribe, BackpressureStrategy.BUFFER);
        integerFlowable.map(new Function<Integer, Integer>(){
            @Override
            public Integer apply(Integer integer) throws Exception {
                System.out.println(Thread.currentThread().getName()+", integer="+integer);
                Integer result = integer+1;
                System.out.println(Thread.currentThread().getName()+", result="+result);
                return result;
            }
        })
                .map(new Function<Integer, Integer>(){
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        System.out.println(Thread.currentThread().getName()+", integer="+integer);
                        Integer result = integer+1;
                        System.out.println(Thread.currentThread().getName()+", result="+result);
                        return result;
                    }
                })
        .subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                System.out.println(Thread.currentThread().getName()+", integer="+integer);
                Thread.sleep(5000);
                System.out.println(integer);
            }
        });
    }


}
