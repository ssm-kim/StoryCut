package com.stroycut.global.common.service;

public interface Subscription {
    void subscribe(Long pk);
    void unsubscribe(Long pk);
}
