package com.kunfei.bookshelf.base;

import com.kunfei.bookshelf.utils.LogHelper;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * 组件之间通信 Rxbus
 */

public class RxBus {

    private final Subject<Object> mSubject = PublishSubject.create();

    public static void post(Object o) {
        HolderClass.INSTANCE.mSubject.onNext(o);
    }

    public static <T> Observable<T> register(Class<T> eventClass) {
        return HolderClass.INSTANCE.toObservable(eventClass);
    }

    private <T> Observable<T> toObservable(final Class<T> eventClass) {
        return mSubject
                .onErrorReturn(throwable -> {
                    LogHelper.e("RxBus", throwable);
                    return null;
                })
                .filter(o -> eventClass.isInstance(o)).cast(eventClass);
    }

    private static final class HolderClass {
        private static final RxBus INSTANCE = new RxBus();
    }
}
