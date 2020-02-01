package com.arejas.dashboardofthings.presentation.ui.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ItemHttpHeaderBinding

import java.util.ArrayList

class HttpHeaderListAdapter(private val elementPresenter: AddEditSensorActuatorPresenter) :
    RecyclerView.Adapter<HttpHeaderListAdapter.HttpHeaderListViewHolder>() {

    var mData: Map<String, String>? = null
    private var keys: MutableList<String>? = null

    var data: Map<String, String>?
        get() = mData
        set(mData) {
            this.mData = mData
            keys = ArrayList()
            keys!!.addAll(mData.keys)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HttpHeaderListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemHttpHeaderBinding>(
            inflater, R.layout.item_http_header,
            parent, false
        )
        return HttpHeaderListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HttpHeaderListViewHolder, position: Int) {
        if (mData != null && keys != null &&
            keys!!.size == mData!!.size &&
            keys!!.size > position
        ) {
            val httpHeader = HttpHeader(keys!![position], mData!![keys!![position]])
            holder.httpHeader = httpHeader
            holder.binding.httpHeader = httpHeader
            holder.binding.presenter = holder
        }
    }

    override fun getItemCount(): Int {
        return if (mData != null && keys != null &&
            keys!!.size == mData!!.size
        )
            mData!!.size
        else
            0
    }

    internal inner class HttpHeaderListViewHolder(val binding: ItemHttpHeaderBinding) :
        RecyclerView.ViewHolder(binding.root), HttpHeaderElementOptionsListener {

        var httpHeader: HttpHeader? = null

        override fun removeClicked(view: View) {
            elementPresenter.cancelHttpHeader(httpHeader!!.name)
        }
    }

    inner class HttpHeader(var name: String?, var value: String?)

    interface HttpHeaderElementOptionsListener {

        fun removeClicked(view: View)

    }

}

