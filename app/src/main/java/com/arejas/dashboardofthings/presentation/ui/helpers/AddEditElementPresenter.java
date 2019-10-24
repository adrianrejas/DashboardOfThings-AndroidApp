package com.arejas.dashboardofthings.presentation.ui.helpers;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public interface AddEditElementPresenter {

    public void pickImage();

    public void cancelImagePicked();

    public void pickHttpCert();

    public void cancelHttpCert();

    public void pickMqttCert();

    public void cancelMqttCert();

    public void networkTypeSelected(int ordinal);

    public void httpAuthTypeSelected(int ordinal);

    public void httpUseSslChanged(boolean checked);

    public void mqttUseSslChanged(boolean checked);

}
