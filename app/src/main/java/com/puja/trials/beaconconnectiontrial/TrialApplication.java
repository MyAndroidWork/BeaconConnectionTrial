package com.puja.trials.beaconconnectiontrial;

import android.app.Application;

import com.estimote.sdk.EstimoteSDK;

/**
 * Created by puja on 28/03/17.
 */

public class TrialApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        EstimoteSDK.initialize(getApplicationContext(), "", "");
        EstimoteSDK.enableDebugLogging(true);
    }


}
