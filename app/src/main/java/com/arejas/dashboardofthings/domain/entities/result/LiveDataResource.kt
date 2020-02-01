package com.arejas.dashboardofthings.domain.entities.result

import androidx.core.util.Consumer
import androidx.core.util.Supplier
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class LiveDataResource<T> : MediatorLiveData<Resource<T>> {

    constructor(dataSupplier: () -> LiveData<T> ) {
        try {
            val data = dataSupplier()
            addSource(data) { received ->
                if (received == null) {
                    postValue(Resource.loading(null))
                } else {
                    postValue(Resource.success(received))
                }
            }
        } catch (e: Exception) {
            postValue(Resource.error(e, null))
        }

    }

    constructor(dataSupplier: Supplier<LiveData<T>>, dataModifier: Consumer<T>) {
        try {
            val data = dataSupplier.get()
            addSource(data) { received ->
                try {
                    if (received == null) {
                        postValue(Resource.loading(null))
                    } else {
                        dataModifier.accept(received)
                        postValue(Resource.success(received))
                    }
                } catch (e: Exception) {
                    postValue(Resource.error(e, null))
                }
            }
        } catch (e: Exception) {
            postValue(Resource.error(e, null))
        }

    }
}
