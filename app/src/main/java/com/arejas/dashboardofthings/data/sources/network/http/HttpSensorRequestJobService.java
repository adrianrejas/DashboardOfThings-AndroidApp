package com.arejas.dashboardofthings.data.sources.network.http;


import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class HttpSensorRequestJobService extends JobService {

    public static final String NETWORK_OBJECT = "NETWORK_OBJECT";
    public static final String SENSOR_OBJECT = "SENSOR_OBJECT";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        try {
            Network network = jobParameters.getExtras().getParcelable(NETWORK_OBJECT);
            Sensor sensor = jobParameters.getExtras().getParcelable(SENSOR_OBJECT);
            HttpRequestIntentService.startActionSensorRequest(getApplicationContext(),
                    network, sensor);
            return false;
        } catch (Exception e) {
            RxHelper.publishLog(0, Enumerators.ElementType.NETWORK,
                    Enumerators.LogLevel.CRITICAL,
                    getString(R.string.log_critical_sensor_scheduling));
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
