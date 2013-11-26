package com.verlanos.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.verlanos.sensors.datastructure.Item;
import com.verlanos.sensors.datastructure.SensorMapping;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a {@link SensorListActivity}
 * in two-pane mode (on tablets) or a {@link SensorDetailActivity}
 * on handsets.
 */
public class SensorDetailFragment extends Fragment implements SensorEventListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private ArrayAdapter adapter;
    private ArrayList<Item> listItems = null;
    private String currentSensor = null;
    private Sensor lastSensor = null;
    private SensorManager sensorManager = null;
    private BroadcastReceiver myRSSIChanged = null;
    private PhoneStateListener phoneStateListener = null;
    /**
     * The dummy content this fragment is presenting.
     */
    private SensorMapping.Mapping mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = SensorMapping.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sensor_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            //((TextView) rootView.findViewById(R.id.sensor_detail)).setText(mItem.content);
            return createSensorView(rootView,mItem.content,inflater);
        }

        return rootView;
    }

    private View createSensorView(View rootView,String SENSOR,LayoutInflater inflater)
    {
        getActivity().getActionBar().setTitle(SENSOR);

        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        listItems = new ArrayList<Item>();
        currentSensor = SENSOR;
        ListView lview = (ListView)rootView.findViewById(R.id.sensor_detail);
        String[] fromColumns = {};
        adapter = new ArrayAdapter<Item>(getActivity(),android.R.layout.simple_list_item_1,listItems);
        lview.setAdapter(adapter);

        if (SENSOR == "LIGHT")
        {
            addLightData();
        }
        else if(SENSOR == "TEMPERATURE")
        {
            addTemperatureData();
        }
        else if(SENSOR == "PROXIMITY")
        {
            addProximityData();
        }
        else if(SENSOR == "PRESSURE")
        {
            addPressureData();
        }
        else if(SENSOR == "RELATIVE HUMIDITY")
        {
            addRelativeHumidityData();
        }
        else if(SENSOR == "WIFI")
        {
            /*Sensor.TYPE_ACCELEROMETER
            Sensor.TYPE_GAME_ROTATION_VECTOR
            Sensor.TYPE_GRAVITY
            Sensor.TYPE_GYROSCOPE
            Sensor.TYPE_LINEAR_ACCELERATION
            Sensor.TYPE_MAGNETIC_FIELD
            Sensor.TYPE_ROTATION_VECTOR
            Sensor.TYPE_SIGNIFICANT_MOTION*/
            addWiFiData();
        }
        else if(SENSOR == "CELL")
        {
            addCellData();
        }

        return lview;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (sensorManager != null && lastSensor != null)
        {
            sensorManager.unregisterListener(this,lastSensor);
        }
        else if(myRSSIChanged != null)
        {
            getActivity().unregisterReceiver(myRSSIChanged);
        }
        else if(phoneStateListener != null)
        {
            TelephonyManager tm = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(phoneStateListener,PhoneStateListener.LISTEN_NONE);
        }

        Item.resetKey();
    }

    private void addLightData()
    {
        addSensorData(Sensor.TYPE_LIGHT,"Lux",SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void addTemperatureData()
    {
        addSensorData(Sensor.TYPE_AMBIENT_TEMPERATURE,"Celsius",SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void addProximityData()
    {
        addSensorData(Sensor.TYPE_PROXIMITY,"Centimetres",SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void addPressureData()
    {
        addSensorData(Sensor.TYPE_PRESSURE,"hPa(millibar)",SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void addRelativeHumidityData()
    {
        addSensorData(Sensor.TYPE_RELATIVE_HUMIDITY,"Air humidity in %",SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void addCellData()
    {
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        CellInfo defaultCellInfo = cellInfos.get(0);

        CellInfoGsm gsmCell = (CellInfoGsm) defaultCellInfo;
        gsmCell.getCellSignalStrength();
        CellIdentityGsm gsmIdentity = gsmCell.getCellIdentity();


        //Item cell = new Item("",defaultCellInfo.);
        int call_state = telephonyManager.getCallState();
        int network_type = telephonyManager.getNetworkType();
        int data_state = telephonyManager.getDataState();
        int data_activity = telephonyManager.getDataActivity();
        CellLocation location = telephonyManager.getCellLocation();
        String device_id = telephonyManager.getDeviceId();
        String device_software_version = telephonyManager.getDeviceSoftwareVersion();
        String line_number = telephonyManager.getLine1Number();
        String network_country_iso = telephonyManager.getNetworkCountryIso();
        String network_operator = telephonyManager.getNetworkOperator();
        String network_operator_name = telephonyManager.getNetworkOperatorName();
        int phoneType = telephonyManager.getPhoneType();
        String groupId = telephonyManager.getGroupIdLevel1();
        String sim_country_iso = telephonyManager.getSimCountryIso();
        String sim_operator = telephonyManager.getSimOperator();
        String sim_operator_name = telephonyManager.getSimOperatorName();
        String sim_serial = telephonyManager.getSimSerialNumber();
        int sim_state = telephonyManager.getSimState();
        String subscriberId = telephonyManager.getSubscriberId();
        String voicemail_number = telephonyManager.getVoiceMailNumber();
        String voicemail_tag = telephonyManager.getVoiceMailAlphaTag();
        boolean hasIccCard = telephonyManager.hasIccCard();
        boolean isNetworkRoaming = telephonyManager.isNetworkRoaming();

        listItems.add(new Item("Line ID",line_number));
        listItems.add(new Item("Group ID",groupId));
        listItems.add(new Item("Subscriber ID",subscriberId));
        listItems.add(new Item("Voicemail number",voicemail_number));
        listItems.add(new Item("Voicemail Alpha tag",voicemail_tag));
        listItems.add(new Item("has ICC Card?",hasIccCard));
        listItems.add(new Item("Roaming?",isNetworkRoaming));

        String real_phone_type = "";

        switch(phoneType)
        {
            case TelephonyManager.PHONE_TYPE_GSM:
                real_phone_type = "GSM";
                break;
            case TelephonyManager.PHONE_TYPE_CDMA:
                real_phone_type = "CDMA";
                break;
            case TelephonyManager.PHONE_TYPE_NONE:
                real_phone_type = "NONE";
                break;
            case TelephonyManager.PHONE_TYPE_SIP:
                real_phone_type = "SIP";
                break;
        }

        listItems.add(new Item("Phone type",real_phone_type)); // ---- Needs defining
        listItems.add(new Item("Device ID",device_id));
        listItems.add(new Item("Device SW version",device_software_version));

        String real_network_type = "";

        switch(network_type)
        {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                real_network_type = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                real_network_type = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                real_network_type = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                real_network_type = "EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                real_network_type = "EVDO 0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                real_network_type = "EVDO A";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                real_network_type = "EVDO B";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                real_network_type = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                real_network_type = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                real_network_type = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                real_network_type = "HSPAP";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                real_network_type = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                real_network_type = "IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                real_network_type = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                real_network_type = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                real_network_type = "UNKNOWN";
                break;
        }

        listItems.add(new Item("Network type",real_network_type));
        listItems.add(new Item("Network operator",network_operator));
        listItems.add(new Item("Network operator name",network_operator_name));
        listItems.add(new Item("Network Country ISO",network_country_iso));
        listItems.add(new Item("SIM operator",sim_operator));
        listItems.add(new Item("SIM operator name",sim_operator_name));
        listItems.add(new Item("SIM country ISO",sim_country_iso));
        listItems.add(new Item("SIM Serial Number",sim_serial));

        String real_sim_state = "N/A";

        switch (sim_state)
        {
            case TelephonyManager.SIM_STATE_ABSENT:
                real_sim_state = "ABSENT";
            break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                real_sim_state = "NETWORK LOCKED";
            break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                real_sim_state = "PIN REQUIRED";
            break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                real_sim_state = "PUK REQUIRED";
            break;
            case TelephonyManager.SIM_STATE_READY:
                real_sim_state = "READY";
            break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                real_sim_state = "UNKNOWN";
                break;
        }

        listItems.add(new Item("SIM STATE",real_sim_state));

        String real_call_state = "";

        switch(call_state)
        {
            case TelephonyManager.CALL_STATE_IDLE:
                real_call_state = "IDLE";
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                real_call_state = "OFFHOOK";
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                real_call_state = "RINGING";
        }

        listItems.add(new Item("CALL STATE",real_call_state));

        String real_data_state = "";

        switch(data_state)
        {
            case TelephonyManager.DATA_CONNECTED:
                real_data_state = "CONNECTED";
                break;
            case TelephonyManager.DATA_CONNECTING:
                real_data_state = "CONNECTING";
                break;
            case TelephonyManager.DATA_DISCONNECTED:
                real_data_state = "DISCONNECTED";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                real_data_state = "SUSPENDED";
        }

        listItems.add(new Item("DATA STATE",real_data_state));

        String real_data_activity = "";

        switch(data_activity)
        {
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                real_data_activity = "DORMANT";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                real_data_activity = "DATA IN";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                real_data_activity = "DATA IN/OUT";
                break;
            case TelephonyManager.DATA_ACTIVITY_NONE:
                real_data_activity = "NONE";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                real_data_activity = "DATA OUT";
        }

        listItems.add(new Item("DATA_ACTIVITY",real_data_activity));

        String curSignal = "";
        String curLocation = "";

        if (location instanceof GsmCellLocation)
        {
            GsmCellLocation g_cell = (GsmCellLocation) location;
            CellInfoGsm gsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
            //CellIdentityGsm gsm_identity = gsm.getCellIdentity();
            int cid = g_cell.getCid();
            int lac = g_cell.getLac();
            int psc = g_cell.getPsc();
            //int mnc = g_cell.getMnc();
            //int mcc = gsm_identity.getMcc();

            CellSignalStrengthGsm cellSignalStrengthGsm = gsm.getCellSignalStrength();
            int asu_level = cellSignalStrengthGsm.getAsuLevel();
            int level = cellSignalStrengthGsm.getLevel();
            int dbm = cellSignalStrengthGsm.getDbm();

            curSignal = "\n      ASU level : "+asu_level+"\n      DBM : "+dbm+"\n      Level : "+level;
            curLocation = "\n      CID : "+cid+"\n      LAC : "+lac+"\n      PSC : "+psc;
        }
        else if(location instanceof CdmaCellLocation)
        {
            CdmaCellLocation c_cell = (CdmaCellLocation) location;

            CellInfoCdma cdma = (CellInfoCdma) telephonyManager.getAllCellInfo().get(0);
            //CellIdentityCdma cdma_identity = cdma.getCellIdentity();
            int baseStationId = c_cell.getBaseStationId();
            int baseStationLat = c_cell.getBaseStationLatitude();
            int baseStationLong = c_cell.getBaseStationLongitude();
            int systemId = c_cell.getSystemId();
            int networkId = c_cell.getNetworkId();

            CellSignalStrengthCdma cellSignalStrengthCdma = cdma.getCellSignalStrength();
            int asu_level = cellSignalStrengthCdma.getAsuLevel();
            int level = cellSignalStrengthCdma.getLevel();
            int dbm = cellSignalStrengthCdma.getDbm();

            curSignal = "\n      ASU level : "+asu_level+"\n      DBM : "+dbm+"\n      Level : "+level;
            curLocation = "\n      Base station ID : "+baseStationId+"\n      Base station LAT : "+baseStationLat+"\n      Base station LONG : "+baseStationLong+"\n      System ID : "+systemId+"\n      Network ID : "+networkId;
        }

        listItems.add(new Item("\nSIGNAL","\n\n"+curSignal+"\n\n"));
        listItems.add(new Item("\nLOCATION","\n\n"+curLocation+"\n\n"));

        adapter.notifyDataSetChanged();

        phoneStateListener = new PhoneStateListener(){

            @Override
            public void onCellLocationChanged(CellLocation newLocation) {
                super.onCellLocationChanged(newLocation);

                TelephonyManager tmanager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                Item lastItem = listItems.get(listItems.size()-1);

                String curLocation = "";

                if (newLocation instanceof GsmCellLocation)
                {

                    GsmCellLocation g_cell = (GsmCellLocation) newLocation;
                    CellInfoGsm gsm = (CellInfoGsm) tmanager.getAllCellInfo().get(0);
                    CellIdentityGsm gsm_identity = gsm.getCellIdentity();
                    int cid = g_cell.getCid();
                    int lac = g_cell.getLac();
                    int psc = g_cell.getPsc();


                    curLocation = "\n      CID : "+cid+"\n      LAC : "+lac+"\n      PSC : "+psc;
                }
                else if(newLocation instanceof CdmaCellLocation)
                {
                    CdmaCellLocation c_cell = (CdmaCellLocation) newLocation;

                    //CellInfoCdma cdma = (CellInfoCdma) tmanager.getAllCellInfo().get(0);
                   // CellIdentityCdma cdma_identity = cdma.getCellIdentity();
                    int baseStationId = c_cell.getBaseStationId();
                    int baseStationLat = c_cell.getBaseStationLatitude();
                    int baseStationLong = c_cell.getBaseStationLongitude();
                    int systemId = c_cell.getSystemId();
                    int networkId = c_cell.getNetworkId();

                    curLocation = "\n      Base station ID : "+baseStationId+"\n      Base station LAT : "+baseStationLat+"\n      Base station LONG : "+baseStationLong+"\n      System ID : "+systemId+"\n      Network ID : "+networkId;
                }
                lastItem.setValue("\n\n"+curLocation+"\n\n");

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                TelephonyManager tmanager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                Item strength_item = listItems.get(listItems.size()-2);
                String curSignal = "";

                if(signalStrength.isGsm())
                {
                    CellInfoGsm gsm = (CellInfoGsm) tmanager.getAllCellInfo().get(0);

                    CellSignalStrengthGsm cellSignalStrengthGsm = gsm.getCellSignalStrength();
                    int bit_error_rate = signalStrength.getGsmBitErrorRate();
                    int level = cellSignalStrengthGsm.getLevel();
                    int dbm = signalStrength.getGsmSignalStrength();
                    int asu_level = cellSignalStrengthGsm.getAsuLevel();

                    curSignal = "\n      ASU level : "+(asu_level >= Integer.MAX_VALUE ? "N/A":asu_level) +"\n      DBM : "+(dbm >= Integer.MAX_VALUE ? "N/A":dbm)+"\n      Level : "+(level >= Integer.MAX_VALUE ? "N/A":level)+"\n      Bit Error Rate : "+(bit_error_rate >= Integer.MAX_VALUE ? "N/A":bit_error_rate);
                }
                else
                {
                    CellInfoCdma cdma = (CellInfoCdma) tmanager.getAllCellInfo().get(0);

                    CellSignalStrengthCdma cellSignalStrengthCdma = cdma.getCellSignalStrength();
                    int asu_level = cellSignalStrengthCdma.getAsuLevel();
                    int level = cellSignalStrengthCdma.getLevel();
                    int dbm = cellSignalStrengthCdma.getDbm();

                    curSignal = "\n      ASU level : "+(asu_level >= Integer.MAX_VALUE ? "N/A":asu_level) +"\n      DBM : "+(dbm >= Integer.MAX_VALUE ? "N/A":dbm)+"\n      Level : "+(level >= Integer.MAX_VALUE ? "N/A":level);
                }

                strength_item.setValue("\n\n"+curSignal+"\n\n");
                adapter.notifyDataSetChanged();
            }
        };

        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CELL_LOCATION);
        telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    private void addWiFiData()
    {
        WifiManager manager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);

        if(manager.isWifiEnabled())
        {
            WifiInfo info = manager.getConnectionInfo();
            // Sensor data
            Item bssid = new Item("Basic Service Set Identifier",info.getBSSID());
            Item hidden = new Item("is Hidden?",info.getHiddenSSID());
            Item ip = new Item("IP address", formatIPAddress(info.getIpAddress()));
            Item mac = new Item("MAC address",info.getMacAddress());
            Item net_id = new Item("Network ID",info.getNetworkId());
            Item ssid = new Item("SSID",info.getSSID());

            Item state = new Item("STATE",info.getSupplicantState());
            Item link_speed = new Item("Link speed",info.getLinkSpeed());
            Item rss = new Item("Received Signal Strength",info.getRssi());


            listItems.add(ssid);
            listItems.add(bssid);
            listItems.add(net_id);
            listItems.add(hidden);
            listItems.add(mac);
            listItems.add(ip);

            listItems.add(state);
            listItems.add(link_speed);
            listItems.add(rss);

            adapter.notifyDataSetChanged();
            myRSSIChanged = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    WifiManager wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
                    int newRSSI = wifiManager.getConnectionInfo().getRssi();
                    int newSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
                    String newState = wifiManager.getConnectionInfo().getSupplicantState().toString();

                    Item rssi = listItems.get(8);
                    Item speed = listItems.get(7);
                    Item state = listItems.get(6);

                    rssi.setValue(newRSSI);
                    speed.setValue(newSpeed);
                    state.setValue(newState);

                    adapter.notifyDataSetChanged();
                    wifiManager.startScan();
                }
            };

            this.getActivity().registerReceiver(myRSSIChanged,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            manager.startScan();
        }
        else
        {
            Item na = new Item("WIFI is turned OFF","N/A");
            listItems.add(na);
            adapter.notifyDataSetChanged();
        }
    }

    protected String formatIPAddress(int ipAddress){

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = null;
        }

        return ipAddressString;
    }

    private void addSensorData(int sensorType,String readingLabel,int delay)
    {
        Sensor mSensor = sensorManager.getDefaultSensor(sensorType);
        acquireCurrentSensorDescription(mSensor);
        Item reading = new Item(readingLabel+" ","N/A");
        listItems.add(reading);
        sensorManager.registerListener(this, mSensor,delay);
        lastSensor = mSensor;
    }

    private void acquireCurrentSensorDescription(Sensor sensor)
    {
        if(sensor != null)
        {
            // Sensor data
            Item name = new Item("Name",sensor.getName());
            Item vendor = new Item("Vendor",sensor.getVendor());
            Item power = new Item("Power",sensor.getPower());
            Item version = new Item("Version",sensor.getVersion());
            Item resolution = new Item("Resolution",sensor.getResolution());
            Item max_range = new Item("Maximum range",sensor.getMaximumRange());
            Item type = new Item("Type",sensor.getType());
            Item delay = new Item("Min poll delay",sensor.getMinDelay());


            listItems.add(name);
            listItems.add(version);
            listItems.add(vendor);
            listItems.add(type);
            listItems.add(max_range);
            listItems.add(delay);
            listItems.add(resolution);
            listItems.add(power);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        Object value = null;

        if( currentSensor == "LIGHT" |
            currentSensor == "TEMPERATURE" |
            currentSensor == "PROXIMITY" |
            currentSensor == "RELATIVE HUMIDITY" |
            currentSensor == "PRESSURE" )
        {
            value = sensorEvent.values[0];
        }
        else
        {

        }

        Item lastItem = listItems.get(listItems.size()-1);
        lastItem.setValue(value);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
