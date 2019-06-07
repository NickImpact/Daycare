package com.nickimpact.daycare.api.util;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}