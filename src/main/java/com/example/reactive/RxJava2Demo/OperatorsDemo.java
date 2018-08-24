package com.example.reactive.RxJava2Demo;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @description : Operators测试
 * @author: liuchuang
 * @date: 2018/8/24 下午4:40
 * @modified by:
 */
@Slf4j
public class OperatorsDemo {

    /**
     * range方法返回一个发射指定范围整数的Observable对象
     */
    @Test
    public void demo1_range() {
        Observable
                .range(0, 5)
                .subscribe((integer) -> {
                        System.out.println(integer);
                });
    }

    /**
     * filter方法用于过滤Observable发射的数据
     */
    @Test
    public void demo2_filter() {
        Observable
                .range(0, 10)
                .filter((integer) -> {
                        return integer % 3 == 0;
                })
                .subscribe((integer) -> {
                        System.out.println(integer);
                });
    }

    /**
     * distinct方法会对发射的元素去重
     */
    @Test
    public void demo3_distinct() {
        Observable.just(1, 1, 2, 3, 1, 2, 2, 4, 5)
                .distinct()
                .subscribe((integer) -> {
                        System.out.println(integer);
                });
    }


    /**
     *
     */
    @Test
    public void demo4_distinct_filter() {
        Observable.just(1, 1, 2, 3, 1, 2, 2, 4, 5)
                .distinct()
                .filter((integer) -> {
                        return integer % 2 == 0;
                })
                .subscribe((integer) -> {
                        System.out.println(integer);
                });
    }

    /**
     * map方法会对发射的元素进行映射处理
     */
    @Test
    public void demo5_map() {
        Observable.range(0, 5)
                .map((integer) -> {
                        return integer + "^2 = " + integer * integer;
                })
                .subscribe((str) -> {
                    System.out.println(str);
                });
    }

    /**
     *
     */
    @Test
    public void demo6_flatMap() {
        Integer nums1[] = new Integer[]{1, 2, 3, 4};
        Integer nums2[] = new Integer[]{5, 6};
        Integer nums3[] = new Integer[]{7, 8, 9};
        Observable.just(nums1, nums2, nums3)
                .flatMap(new Function<Integer[], Observable<Integer>>() {
                    @Override
                    public Observable<Integer> apply(@NonNull Integer[] integers) throws Exception {
                        return Observable.fromArray(integers);
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });
    }


    /**
     * mergeWith合并两个Observable发射的元素，不保证顺序
     */
    @Test
    public void demo7_mergeWith() {
        Integer nums1[] = new Integer[]{5, 6, 7, 8, 9};
        Observable.just(1, 2, 3, 4, 5)
                .mergeWith(Observable.fromArray(nums1))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });
    }

    /**
     * concatWith会让当前Observable先发射元素，然后再由连接Observable发射
     */
    @Test
    public void demo8_concatWith() {
        Integer nums1[] = new Integer[]{5, 6, 7, 8, 9};
        Observable.just(1, 2, 3, 4, 5)
                .concatWith(Observable.fromArray(nums1))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        System.out.println(integer);
                    }
                });
    }

    /**
     * zipWith会聚合两个Observable，两个Observable每次发射的元素会组合起来一起发射，最终发射的元素数量由少的那个决定
     */
    @Test
    public void demo9_zipWith() {
        String names[] = new String[]{"红娃", "橙娃", "黄娃", "绿娃", "青娃", "蓝娃", "紫娃"};
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8)
                .zipWith(Observable.fromArray(names), new BiFunction<Integer, String, String>() {
                    @Override
                    public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                        return integer + s;
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }

    @Test
    public void demo10_link_all_operators() {
        Integer nums1[] = new Integer[]{5, 6, 7, 8, 9};
        Integer nums2[] = new Integer[]{3, 4, 5, 6};
        String names[] = new String[]{"红娃", "橙娃", "黄娃", "绿娃", "青娃", "蓝娃", "紫娃"};
        Observable.just(nums1)
                .flatMap(new Function<Integer[], Observable<Integer>>() {
                    @Override
                    public Observable<Integer> apply(@NonNull Integer[] integers) throws Exception {
                        return Observable.fromArray(integers);
                    }
                })
                .mergeWith(Observable.fromArray(nums2))
                .concatWith(Observable.just(1, 2))
                .distinct()
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer < 5;
                    }
                })
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        return integer + ":";
                    }
                })
                .zipWith(Observable.fromArray(names), new BiFunction<String, String, String>() {
                    @Override
                    public String apply(@NonNull String s, @NonNull String s2) throws Exception {
                        return s + s2;
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                });
    }



}
