package com.arejas.dashboardofthings.utils.functional;

@FunctionalInterface
public interface Consumer<T> {

    void accept(T t);

}
