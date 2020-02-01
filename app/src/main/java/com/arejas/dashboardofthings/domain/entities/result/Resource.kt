package com.arejas.dashboardofthings.domain.entities.result

import java.util.Objects

/*
 * Class used for representing a request to database or network and its result.
 */
class Resource<T> private constructor(
    var status: Status, var data: T?,
    var error: Throwable?
) {

    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(error: Throwable, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, error)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}
