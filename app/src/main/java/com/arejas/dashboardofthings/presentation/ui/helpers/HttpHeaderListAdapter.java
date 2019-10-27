package com.arejas.dashboardofthings.presentation.ui.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ItemHttpHeaderBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpHeaderListAdapter
        extends RecyclerView.Adapter<HttpHeaderListAdapter.HttpHeaderListViewHolder> {

    public Map<String, String> mData;

    private AddEditSensorActuatorPresenter elementPresenter;
    private List<String> keys;

    public HttpHeaderListAdapter(AddEditSensorActuatorPresenter elementPresenter) {
        this.elementPresenter = elementPresenter;
    }

    public Map<String, String> getData() {
        return mData;
    }

    public void setData(Map<String, String> mData) {
        this.mData = mData;
        keys = new ArrayList<>();
        keys.addAll(mData.keySet());
    }

    @Override
    public HttpHeaderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHttpHeaderBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_http_header,
                parent, false);
        return new HttpHeaderListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final HttpHeaderListViewHolder holder, int position) {
        if ((mData != null) && (keys != null) &&
                (keys.size() == mData.size()) &&
                (keys.size() > position)) {
            HttpHeader httpHeader = new HttpHeader(keys.get(position), mData.get(keys.get(position)));
            holder.setHttpHeader(httpHeader);
            holder.binding.setHttpHeader(httpHeader);
            holder.binding.setPresenter(holder);
        }
    }

    @Override
    public int getItemCount() {
        if ((mData != null) && (keys != null) &&
                (keys.size() == mData.size()))
            return mData.size();
        else
            return 0;
    }

    class HttpHeaderListViewHolder extends RecyclerView.ViewHolder implements HttpHeaderElementOptionsListener {

        private HttpHeader httpHeader;

        final ItemHttpHeaderBinding binding;

        HttpHeaderListViewHolder(ItemHttpHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HttpHeader getHttpHeader() {
            return httpHeader;
        }

        public void setHttpHeader(HttpHeader httpHeader) {
            this.httpHeader = httpHeader;
        }

        @Override
        public void removeClicked(View view) {
            elementPresenter.cancelHttpHeader(httpHeader.getName());
        }
    }

    public class HttpHeader {

        private String name;

        private String value;

        public HttpHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public interface HttpHeaderElementOptionsListener {

        public void removeClicked (View view);

    }

}

