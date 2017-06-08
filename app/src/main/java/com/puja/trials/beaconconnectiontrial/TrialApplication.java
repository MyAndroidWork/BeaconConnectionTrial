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

        EstimoteSDK.initialize(getApplicationContext(), "orchestra-technology-inc-s-4u2", "e2ca5a2c3a18f1e8343576157f9d4732");
        EstimoteSDK.enableDebugLogging(true);
    }


}
