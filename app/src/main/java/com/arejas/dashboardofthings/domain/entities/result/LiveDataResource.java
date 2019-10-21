package com.arejas.dashboardofthings.domain.entities.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.arejas.dashboardofthings.utils.functional.Consumer;
import com.arejas.dashboardofthings.utils.functional.Supplier;

public class LiveDataResource<T> extends MediatorLiveData<Resource<T>> {

    public LiveDataResource(Supplier<LiveData<T>> dataSupplier) {
        try {
            LiveData<T> data = dataSupplier.get();
            addSource(data, received -> {
                if (received == null) {
                    postValue(Resource.loading(null));
                } else {
                    postValue(Resource.success(received));
                }
            });
        } catch (Exception e) {
            postValue(Resource.error(e, null));
        }
    }

    public LiveDataResource(Supplier<LiveData<T>> dataSupplier, Consumer<T> dataModifier) {
        try {
            LiveData<T> data = dataSupplier.get();
            addSource(data, received -> {
                try {
                    if (received == null) {
                        postValue(Resource.loading(null));
                    } else {
                        dataModifier.accept(received);
                        postValue(Resource.success(received));
                    }
                } catch (Exception e) {
                    postValue(Resource.error(e, null));
                }
            });
        } catch (Exception e) {
            postValue(Resource.error(e, null));
        }
    }
}
