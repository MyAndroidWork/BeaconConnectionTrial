package com.puja.trials.beaconconnectiontrial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.connection.DeviceConnection;
import com.estimote.sdk.connection.DeviceConnectionCallback;
import com.estimote.sdk.connection.DeviceConnectionProvider;
import com.estimote.sdk.connection.exceptions.DeviceConnectionException;
import com.estimote.sdk.connection.scanner.ConfigurableDevice;
import com.estimote.sdk.connection.scanner.ConfigurableDevicesScanner;
import com.estimote.sdk.connection.scanner.DeviceType;
import com.estimote.sdk.connection.settings.SettingCallback;
import com.estimote.sdk.connection.settings.SettingsEditor;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BeaconConnection beaconConnection;
    DeviceConnectionProvider connectionProvider;
    List<ConfigurableDevice> deviceList;
    DeviceConnection connection;
    private boolean connectedToTheConnectionProvider;
    ConfigurableDevicesScanner deviceScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectionProvider = new DeviceConnectionProvider(this);
        connectionProvider.connectToService(new DeviceConnectionProvider.ConnectionProviderCallback() {
            @Override
            public void onConnectedToService() {
                // Handle your actions here. You are now connected to connection service.
                // For example: you can create DeviceConnection object here from connectionProvider.

                connectedToTheConnectionProvider = true;


            }
        });

    //    beaconConnection = BeaconConnection.getInstance(this);

      //  beaconConnection.setConnectionProvider();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SystemRequirementsChecker.checkWithDefaultDialogs(this)) {

            deviceScanner = new ConfigurableDevicesScanner(this);
            //deviceScanner.setOwnDevicesFiltering(true);
            //deviceScanner.setDeviceTypes(DeviceType.LOCATION_BEACON);
            deviceScanner.scanForDevices(new ConfigurableDevicesScanner.ScannerCallback() {
                @Override
                public void onDevicesFound(List<ConfigurableDevicesScanner.ScanResultItem> devices) {
                    deviceScanner.stopScanning();

                    for(ConfigurableDevicesScanner.ScanResultItem item : devices) {
                        Log.d("Scanned Beacon:","--" + item.device.macAddress.toString() + "--" + item.device.deviceId + "--");
                        deviceList.add(item.device);
                    }
                }
            });

            if (connectedToTheConnectionProvider){
                connection = connectionProvider.getConnection(deviceList.get(0));
                connection.connect(new DeviceConnectionCallback() {
                    @Override
                    public void onConnected() {
                        // Do something with your connection.
                        // You can for example read device settings, or make an firmware update.
                        Log.d("DeviceConnection", "onConnected");

                        // Take your connected DeviceConnection object and get it's editor
                        SettingsEditor edit = connection.edit();
                        edit.set(connection.settings.beacon.proximityUUID(), UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"));
                        edit.set(connection.settings.beacon.major(), 54657);
                        edit.set(connection.settings.beacon.minor(), 43554);
                        edit.commit(new SettingCallback() {
                            @Override
                            public void onSuccess(Object value) {
                                // Handle success here. It will be called only when all settings have been written.
                                Log.d("DeviceBulkWrite","Bulk write successful");
                            }

                            @Override
                            public void onFailure(DeviceConnectionException exception) {
                                // Handle exceptions
                                Log.d("DeviceBulkWrite","Bulk write failed");
                            }
                        });
                    }


                    @Override
                    public void onDisconnected() {
                        // Every time your device gets disconnected, you can handle that here.
                        // For example: in this state you can try reconnecting to your device.
                        Log.d("DeviceConnection", "onDisconnected");
                    }

                    @Override
                    public void onConnectionFailed(DeviceConnectionException exception) {
                        // Handle every connection error here.
                        Log.d("DeviceConnection", "onConnectionFailed");
                    }
                });
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (deviceScanner!=null && deviceScanner.isScanning()){
            deviceScanner.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {


        if (connectionProvider!=null)
            connectionProvider.destroy();

        connection.destroy();

        super.onDestroy();

        //deviceConnection.close();
    }
}
