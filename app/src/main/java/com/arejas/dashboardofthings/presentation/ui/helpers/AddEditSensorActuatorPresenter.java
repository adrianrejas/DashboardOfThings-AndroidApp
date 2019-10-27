package com.arejas.dashboardofthings.presentation.ui.helpers;

public interface AddEditSensorActuatorPresenter {

    public void pickImage();

    public void cancelImagePicked();

    public void pickLocation();

    public void cancelLocationPicked();

    public void addHttpHeader();

    public void cancelHttpHeader(String headerName);

    public void networkSelected(int ordinal);

    public void messageTypeSelected(int ordinal);

    public void dataTypeSelected(int ordinal);

}
