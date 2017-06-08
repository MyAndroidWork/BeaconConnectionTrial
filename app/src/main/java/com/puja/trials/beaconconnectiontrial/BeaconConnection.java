package com.puja.trials.beaconconnectiontrial;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.estimote.sdk.cloud.model.DeviceFirmware;
import com.estimote.sdk.connection.DeviceConnection;
import com.estimote.sdk.connection.DeviceConnectionCallback;
import com.estimote.sdk.connection.DeviceConnectionProvider;
import com.estimote.sdk.connection.exceptions.DeviceConnectionException;
import com.estimote.sdk.connection.scanner.ConfigurableDevice;
import com.estimote.sdk.connection.scanner.ConfigurableDevicesScanner;
import com.estimote.sdk.connection.scanner.DeviceType;
import com.estimote.sdk.connection.settings.SettingCallback;
import com.estimote.sdk.connection.settings.SettingsEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by puja on 28/03/17.
 */

public class BeaconConnection {

    public Context context;
    DeviceConnectionProvider connectionProvider;
    ConfigurableDevicesScanner deviceScanner;
    List<ConfigurableDevice> deviceList = new ArrayList<ConfigurableDevice>();
    static BeaconConnection beaconConnection;
    DeviceConnection connection;
    SettingsEditor settingsEditor;

    private BeaconConnection(Context context) {
        this.context = context;
    }

    public static BeaconConnection getInstance(Context context){

        if (beaconConnection == null) {
            beaconConnection = new BeaconConnection(context);
        }

        return beaconConnection;
    }

    public List<ConfigurableDevice> scanForDevices(){

        deviceScanner = new ConfigurableDevicesScanner(context);

        // Scan for devices own by currently logged user.
      //  deviceScanner.setOwnDevicesFiltering(true);

        // Scan only for Location Beacons. You can set here different types of devices, such as Proximity Beacons or Nearables.
//        deviceScanner.setDeviceTypes(DeviceType.LOCATION_BEACON);

        // Pass callback object and start scanning. If scanner finds something, it will notify your callback.
        deviceScanner.scanForDevices(new ConfigurableDevicesScanner.ScannerCallback() {
            @Override
            public void onDevicesFound(List<ConfigurableDevicesScanner.ScanResultItem> devices) {

                for(ConfigurableDevicesScanner.ScanResultItem item : devices) {

                    Log.d("Scanned Beacon:","--" + item.device.macAddress.toString() + "--" + item.device.deviceId + "--");
                    deviceList.add(item.device);
                    setConnectionProvider();
                    // Do something with your object.
                    // ScanResultItem contains basic info about device discovery - such as RSSI, TX power, or discovery time.
                    // It also contains ConfigurableDevice object. You can easily acquire it via item.configurableDevice
                }
            }
        });




        return deviceList;
    }

    public void setConnectionProvider() {

        Log.d("BeaconConnection", "setConnectionProvider");

        connectionProvider = new DeviceConnectionProvider(context);
        connectionProvider.connectToService(new DeviceConnectionProvider.ConnectionProviderCallback() {
            @Override
            public void onConnectedToService() {
                // Handle your actions here. You are now connected to connection service.
                // For example: you can create DeviceConnection object here from connectionProvider.
                if (deviceList!= null && !deviceList.isEmpty())
                    connectToDevices(deviceList);

            }
        });
    }

    public void connectToDevices(List<ConfigurableDevice> deviceList)
    {
        Log.d("BeaconConnection", "connectToDevices");

        for (ConfigurableDevice device:deviceList)
        {
            // Pass your ConfigurableDevice to connection provider method
            connection = connectionProvider.getConnection(device);

       //     editBeaconSettings(connection);
            connection.connect(new DeviceConnectionCallback() {
                @Override
                public void onConnected() {
                    // Do something with your connection.
                    // You can for example read device settings, or make an firmware update.
                    Log.d("DeviceConnection", "onConnected");
                   editBeaconSettings(connection);
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

    //    return connection;

    }

    public void editBeaconSettings(final DeviceConnection connection)
    {
        Log.d("BeaconConnection", "editBeaconSettings");
        settingsEditor = connection.edit();
        settingsEditor.set(connection.settings.beacon.proximityUUID(), UUID.fromString(""));
        settingsEditor.set(connection.settings.beacon.major(), 54365);
        settingsEditor.set(connection.settings.beacon.minor(), 45345);

        Log.d("Updated -", connection.settings.beacon.major().toString() + "-----" + connection.settings.beacon.minor().toString());

        settingsEditor.commit(new SettingCallback() {
            @Override
            public void onSuccess(Object value) {
                // Handle success here. It will be called only when all settings have been written.
                Log.d("DeviceBulkWrite","Bulk write successful");
                updateBeaconSettings(connection);
            }

            @Override
            public void onFailure(DeviceConnectionException exception) {
                // Handle exceptions
                Log.d("DeviceBulkWrite","Bulk write failed");
            }
        });
    }

    public void updateBeaconSettings(DeviceConnection connection)
    {
        Log.d("BeaconConnection", "updateBeaconSettings");
        connection.checkForFirmwareUpdate(new DeviceConnection.CheckFirmwareCallback() {
            @Override
            public void onDeviceUpToDate(DeviceFirmware firmware) {
                // If device is up to date, handle that case here. Firmware object contains info about current version.
                Log.d("DeviceFirmwareUpdate","Device firmware is up to date.");
            }

            @Override
            public void onDeviceNeedsUpdate(DeviceFirmware firmware) {
                // Handle device update here. Firmware object contains info about latest version.
                Log.d("DeviceFirmwareUpdate","Device needs firmware update.");
            }

            @Override
            public void onError(DeviceConnectionException exception) {
                // Handle errors here
                Log.d("DeviceFirmwareUpdate","Error checking device firmware: " + exception.getMessage());
            }
        });
    }

    public void destroyConnProvider() {
        connectionProvider.destroy();
    }


}
