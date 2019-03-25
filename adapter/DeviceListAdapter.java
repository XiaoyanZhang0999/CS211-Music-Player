package com.ldw.music.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.model.JDY_type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceListAdapter extends Activity {
    public JDY_type DEV_TYPE;
    BluetoothAdapter apter;
    Context context;
    byte dev_VID = (byte) -120;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1 && DeviceListAdapter.this.stop_timer) {
                DeviceListAdapter.this.loop_list();
            }
            super.handleMessage(msg);
        }
    };
    public String ibeacon_MAJOR = "";
    public String ibeacon_MINOR = "";
    public String ibeacon_UUID = "";
    int ip = 0;
    private DeviceListAdapter1 list_cell_0;
    int list_select_index = 0;
    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            DeviceListAdapter deviceListAdapter = DeviceListAdapter.this;
            deviceListAdapter.scan_int++;
            if (DeviceListAdapter.this.scan_int > 1) {
                DeviceListAdapter.this.scan_int = 0;
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    JDY_type m_tyep = DeviceListAdapter.this.dv_type(scanRecord);
                    if (m_tyep != JDY_type.UNKW && m_tyep != null) {
                        DeviceListAdapter.this.list_cell_0.addDevice(device, scanRecord, Integer.valueOf(rssi), m_tyep);
                        DeviceListAdapter.this.list_cell_0.notifyDataSetChanged();
                        return;
                    }
                    return;
                }
                DeviceListAdapter.this.runOnUiThread(new Runnable() {
                    public void run() {
                        JDY_type m_tyep = DeviceListAdapter.this.dv_type(scanRecord);
                        if (m_tyep != JDY_type.UNKW && m_tyep != null) {
                            DeviceListAdapter.this.list_cell_0.addDevice(device, scanRecord, Integer.valueOf(rssi), m_tyep);
                            DeviceListAdapter.this.list_cell_0.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    };
    int scan_int = 0;
    public byte[] sensor_VID;
    public byte sensor_batt;
    public byte sensor_humid;
    public byte sensor_temp;
    boolean stop_timer = true;
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            message.what = 1;
            DeviceListAdapter.this.handler.sendMessage(message);
        }
    };
    Timer timer = new Timer();

    class DeviceListAdapter1 extends BaseAdapter {
        int count = 0;
        private List<BluetoothDevice> dev_ble = new ArrayList();
        private List<Integer> dev_rssi = new ArrayList();
        private List<byte[]> dev_scan_data = new ArrayList();
        private List<JDY_type> dev_type = new ArrayList();
        int ip = 0;
        private List<Integer> remove = new ArrayList();
        private ViewHolder viewHolder;

        public void loop() {
            if (this.remove != null && this.remove.size() > 0 && this.ip == 0) {
                if (this.count >= this.remove.size()) {
                    this.count = 0;
                }
                Integer it = (Integer) this.remove.get(this.count);
                if (it.intValue() >= 3) {
                    this.dev_ble.remove(this.count);
                    this.dev_scan_data.remove(this.count);
                    this.dev_rssi.remove(this.count);
                    this.dev_type.remove(this.count);
                    this.remove.remove(this.count);
                    notifyDataSetChanged();
                } else {
                    this.remove.add(this.count + 1, Integer.valueOf(it.intValue() + 1));
                    this.remove.remove(this.count);
                }
                this.count++;
            }
        }

        public void addDevice(BluetoothDevice device, byte[] scanRecord, Integer RSSI, JDY_type type) {
            this.ip = 1;
            if (this.dev_ble.contains(device)) {
                for (int i = 0; i < this.dev_ble.size(); i++) {
                    if (((BluetoothDevice) this.dev_ble.get(i)).getAddress().equals(device.getAddress())) {
                        this.dev_ble.add(i + 1, device);
                        this.dev_ble.remove(i);
                        this.dev_scan_data.add(i + 1, scanRecord);
                        this.dev_scan_data.remove(i);
                        this.dev_rssi.add(i + 1, RSSI);
                        this.dev_rssi.remove(i);
                        this.dev_type.add(i + 1, type);
                        this.dev_type.remove(i);
                        this.remove.add(i + 1, Integer.valueOf(0));
                        this.remove.remove(i);
                    }
                }
            } else {
                this.dev_ble.add(device);
                this.dev_scan_data.add(scanRecord);
                this.dev_rssi.add(RSSI);
                this.dev_type.add(type);
                this.remove.add(Integer.valueOf(0));
            }
            notifyDataSetChanged();
            this.ip = 0;
        }

        public void clear() {
            this.dev_ble.clear();
            this.dev_scan_data.clear();
            this.dev_rssi.clear();
            this.dev_type.clear();
            this.remove.clear();
        }

        public int getCount() {
            return this.dev_ble.size();
        }

        public BluetoothDevice getItem(int position) {
            return (BluetoothDevice) this.dev_ble.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position > this.dev_ble.size()) {
                return null;
            }
            JDY_type type_0 = (JDY_type) this.dev_type.get(position);
            BluetoothDevice device;
            String devName;
            String tp;
            if (type_0 == JDY_type.JDY) {
                convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_device, null);
                this.viewHolder = new ViewHolder();
                this.viewHolder.tv_devName = (TextView) convertView.findViewById(R.id.device_name);
                this.viewHolder.tv_devAddress = (TextView) convertView.findViewById(R.id.device_address);
                this.viewHolder.device_rssi = (TextView) convertView.findViewById(R.id.device_rssi);
                this.viewHolder.scan_data = (TextView) convertView.findViewById(R.id.scan_data);
                this.viewHolder.type0 = (TextView) convertView.findViewById(R.id.type0);
                convertView.setTag(this.viewHolder);
                DeviceListAdapter.this.list_select_index = 1;
                device = (BluetoothDevice) this.dev_ble.get(position);
                devName = device.getName();
                if (this.viewHolder.tv_devName != null) {
                    this.viewHolder.tv_devName.setText(devName);
                }
                String mac = device.getAddress();
                if (this.viewHolder.tv_devAddress != null) {
                    this.viewHolder.tv_devAddress.setText(mac);
                }
                String rssi_00 = "RSSI:-" + (this.dev_rssi.get(position));
                if (this.viewHolder.device_rssi != null) {
                    this.viewHolder.device_rssi.setText(rssi_00);
                }
                tp = "Type:Stander mode";
                if (this.viewHolder.type0 != null) {
                    this.viewHolder.type0.setText(tp);
                }
                if (this.viewHolder.scan_data != null) {
                    this.viewHolder.scan_data.setText("scanRecord:" + bytesToHexString1((byte[]) this.dev_scan_data.get(position)));
                }
            } else if (type_0 == JDY_type.JDY_iBeacon) {
                convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_ibeacon, null);
                this.viewHolder = new ViewHolder();
                this.viewHolder.ibeacon_name = (TextView) convertView.findViewById(R.id.ibeacon_Name);
                this.viewHolder.ibeacon_mac = (TextView) convertView.findViewById(R.id.iBeacon_mac);
                this.viewHolder.ibeacon_uuid = (TextView) convertView.findViewById(R.id.ibeacon_uuid);
                this.viewHolder.ibeacon_major = (TextView) convertView.findViewById(R.id.ibeacon_major);
                this.viewHolder.ibeacon_minor = (TextView) convertView.findViewById(R.id.ibeacon_minor);
                this.viewHolder.ibeacon_rssi = (TextView) convertView.findViewById(R.id.ibeacon_rssi);
                this.viewHolder.type0 = (TextView) convertView.findViewById(R.id.type0);
                convertView.setTag(this.viewHolder);
                this.viewHolder.type0.setText("Type:iBeacon");
                device = (BluetoothDevice) this.dev_ble.get(position);
                if (this.viewHolder.ibeacon_name != null) {
                    this.viewHolder.ibeacon_name.setText(device.getName());
                }
                if (this.viewHolder.ibeacon_mac != null) {
                    this.viewHolder.ibeacon_mac.setText(device.getAddress());
                }
                if (this.viewHolder.ibeacon_uuid != null) {
                    this.viewHolder.ibeacon_uuid.setText(get_ibeacon_uuid(position));
                }
                if (this.viewHolder.ibeacon_major != null) {
                    this.viewHolder.ibeacon_major.setText(get_ibeacon_major(position));
                }
                if (this.viewHolder.ibeacon_minor != null) {
                    this.viewHolder.ibeacon_minor.setText(get_ibeacon_minor(position));
                }
                if (this.viewHolder.ibeacon_rssi != null) {
                    this.viewHolder.ibeacon_rssi.setText("-" + this.dev_rssi.get(position));
                }
                DeviceListAdapter.this.list_select_index = 2;
            } else if (type_0 == JDY_type.sensor_temp) {
                convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_sensor_temp, null);
                this.viewHolder = new ViewHolder();
                this.viewHolder.sensor_name = (TextView) convertView.findViewById(R.id.sensor_name);
                this.viewHolder.sensor_mac = (TextView) convertView.findViewById(R.id.sensor_mac);
                this.viewHolder.sensor_rssi = (TextView) convertView.findViewById(R.id.sensor_rssi);
                this.viewHolder.sensor_type0 = (TextView) convertView.findViewById(R.id.sensor_type0);
                this.viewHolder.sensor_temp = (TextView) convertView.findViewById(R.id.sensor_thermo_c);
                this.viewHolder.sensor_humid = (TextView) convertView.findViewById(R.id.sensor_thermo_f);
                this.viewHolder.sensor_batt = (TextView) convertView.findViewById(R.id.sensor_batt);
                convertView.setTag(this.viewHolder);
                DeviceListAdapter.this.list_select_index = 2;
                device = (BluetoothDevice) this.dev_ble.get(position);
                devName = device.getName();
                if (this.viewHolder.sensor_name != null) {
                    this.viewHolder.sensor_name.setText("Name:" + devName);
                }
                if (this.viewHolder.sensor_mac != null) {
                    this.viewHolder.sensor_mac.setText("MAC:" + device.getAddress());
                }
                if (this.viewHolder.sensor_rssi != null) {
                    this.viewHolder.sensor_rssi.setText("RSSI:-" + (this.dev_rssi.get(position)));
                }
                tp = "Type:sensor";
                if (this.viewHolder.sensor_type0 != null) {
                    this.viewHolder.sensor_type0.setText(tp);
                }
                if (this.viewHolder.sensor_temp != null) {
                    this.viewHolder.sensor_temp.setText(get_sensor_temp(position));
                }
                if (this.viewHolder.sensor_humid != null) {
                    this.viewHolder.sensor_humid.setText(get_sensor_humid(position) + "%");
                }
                if (this.viewHolder.sensor_batt != null) {
                    this.viewHolder.sensor_batt.setText(get_sensor_batt(position));
                }
            } else if (type_0 == JDY_type.JDY_LED1) {
                convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_led, null);
                this.viewHolder = new ViewHolder();
                this.viewHolder.led_name = (TextView) convertView.findViewById(R.id.led_name);
                this.viewHolder.led_mac = (TextView) convertView.findViewById(R.id.led_mac);
                this.viewHolder.led_rssi = (TextView) convertView.findViewById(R.id.led_rssi);
                this.viewHolder.led_type113 = (TextView) convertView.findViewById(R.id.led_type113);
                convertView.setTag(this.viewHolder);
                device = (BluetoothDevice) this.dev_ble.get(position);
                if (this.viewHolder.led_name != null) {
                    this.viewHolder.led_name.setText("Name:" + device.getName());
                }
                if (this.viewHolder.led_mac != null) {
                    this.viewHolder.led_mac.setText("MAC:" + device.getAddress());
                }
                if (this.viewHolder.led_rssi != null) {
                    this.viewHolder.led_rssi.setText("RSSI:-" + (this.dev_rssi.get(position)));
                }
                if (this.viewHolder.led_type113 != null) {
                    tp = "";
                    this.viewHolder.led_type113.setText("Type:LED灯带");
                }
            } else if (type_0 != JDY_type.JDY_LED2) {
                if (type_0 == JDY_type.JDY_AMQ) {
                    convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_massager, null);
                    this.viewHolder = new ViewHolder();
                    this.viewHolder.massager_name = (TextView) convertView.findViewById(R.id.massager_name);
                    this.viewHolder.massager_mac = (TextView) convertView.findViewById(R.id.massager_mac);
                    this.viewHolder.massager_rssi = (TextView) convertView.findViewById(R.id.massager_rssi);
                    this.viewHolder.massager_type113 = (TextView) convertView.findViewById(R.id.massager_type113);
                    convertView.setTag(this.viewHolder);
                    device = (BluetoothDevice) this.dev_ble.get(position);
                    if (this.viewHolder.massager_name != null) {
                        this.viewHolder.massager_name.setText("Name:" + device.getName());
                    }
                    if (this.viewHolder.massager_mac != null) {
                        this.viewHolder.massager_mac.setText("MAC:" + device.getAddress());
                    }
                    if (this.viewHolder.massager_rssi != null) {
                        this.viewHolder.massager_rssi.setText("RSSI:-" + (this.dev_rssi.get(position)));
                    }
                    if (this.viewHolder.massager_type113 != null) {
                        tp = "";
                        this.viewHolder.massager_type113.setText("Type:AV棒");
                    }
                } else if (type_0 == JDY_type.JDY_KG) {
                    convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_switch, null);
                    this.viewHolder = new ViewHolder();
                    this.viewHolder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
                    this.viewHolder.switch_mac = (TextView) convertView.findViewById(R.id.switch_mac);
                    this.viewHolder.switch_rssi = (TextView) convertView.findViewById(R.id.switch_rssi);
                    this.viewHolder.switch_type113 = (TextView) convertView.findViewById(R.id.switch_type113);
                    convertView.setTag(this.viewHolder);
                    device = (BluetoothDevice) this.dev_ble.get(position);
                    if (this.viewHolder.switch_name != null) {
                        this.viewHolder.switch_name.setText(device.getName());
                    }
                    if (this.viewHolder.switch_mac != null) {
                        this.viewHolder.switch_mac.setText(device.getAddress());
                    }
                    if (this.viewHolder.switch_rssi != null) {
                        this.viewHolder.switch_rssi.setText("RSSI:-" + (this.dev_rssi.get(position)));
                    }
                    if (this.viewHolder.switch_type113 != null) {
                        tp = "";
                        this.viewHolder.switch_type113.setText("Type:remote");
                    }
                } else if (type_0 == JDY_type.JDY_WMQ) {
                    convertView = LayoutInflater.from(DeviceListAdapter.this.context).inflate(R.layout.listitem_switch, null);
                    this.viewHolder = new ViewHolder();
                    this.viewHolder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
                    this.viewHolder.switch_mac = (TextView) convertView.findViewById(R.id.switch_mac);
                    this.viewHolder.switch_rssi = (TextView) convertView.findViewById(R.id.switch_rssi);
                    this.viewHolder.switch_type113 = (TextView) convertView.findViewById(R.id.switch_type113);
                    this.viewHolder.type_imageView2 = (ImageView) convertView.findViewById(R.id.type_imageView2);
                    convertView.setTag(this.viewHolder);
                    device = (BluetoothDevice) this.dev_ble.get(position);
                    if (this.viewHolder.switch_name != null) {
                        this.viewHolder.switch_name.setText(device.getName());
                    }
                    if (this.viewHolder.switch_mac != null) {
                        this.viewHolder.switch_mac.setText(device.getAddress());
                    }
                    if (this.viewHolder.switch_rssi != null) {
                        this.viewHolder.switch_rssi.setText("RSSI:-" + (this.dev_rssi.get(position)));
                    }
                    if (this.viewHolder.switch_type113 != null) {
                        tp = "";
                        this.viewHolder.switch_type113.setText("Type:remote");
                    }
                    if (this.viewHolder.type_imageView2 != null) {
                        this.viewHolder.type_imageView2.setImageDrawable(DeviceListAdapter.this.getResources().getDrawable(R.drawable.massager_img));
                    }
                } else {
                    JDY_type jDY_type = JDY_type.JDY_LOCK;
                }
            }
            return convertView;
        }

        public String get_ibeacon_uuid(int pos) {
            HashMap<String, String> map = new HashMap();
            byte[] byte1000 = (byte[]) this.dev_scan_data.get(pos);
            if (byte1000.length < 32) {
                return null;
            }
            byte[] proximityUuidBytes = new byte[16];
            System.arraycopy(byte1000, 9, proximityUuidBytes, 0, 16);
            String Beacon_UUID = bytesToHexString(proximityUuidBytes);
            String uuid_8 = Beacon_UUID.substring(0, 8);
            String uuid_4 = Beacon_UUID.substring(8, 12);
            String uuid_44 = Beacon_UUID.substring(12, 16);
            String uuid_444 = Beacon_UUID.substring(16, 20);
            return new StringBuilder(String.valueOf(uuid_8)).append("-").append(uuid_4).append("-").append(uuid_44).append("-").append(uuid_444).append("-").append(Beacon_UUID.substring(20, 32)).toString();
        }

        public String get_ibeacon_major(int pos) {
            if (((byte[]) this.dev_scan_data.get(pos)).length < 60) {
                return null;
            }
            return String.valueOf(byteArrayToInt1(new byte[]{(byte) 0, (byte) 0, ((byte[]) this.dev_scan_data.get(pos))[25], ((byte[]) this.dev_scan_data.get(pos))[26]}));
        }

        public String get_ibeacon_minor(int pos) {
            if (((byte[]) this.dev_scan_data.get(pos)).length < 60) {
                return null;
            }
            return String.valueOf(byteArrayToInt1(new byte[]{(byte) 0, (byte) 0, ((byte[]) this.dev_scan_data.get(pos))[27], ((byte[]) this.dev_scan_data.get(pos))[28]}));
        }

        public String get_sensor_temp(int pos) {
            return bytesToHexString(new byte[]{((byte[]) this.dev_scan_data.get(pos))[58]});
        }

        public String get_sensor_humid(int pos) {
            return bytesToHexString(new byte[]{((byte[]) this.dev_scan_data.get(pos))[59]});
        }

        public String get_sensor_batt(int pos) {
            return bytesToHexString(new byte[]{((byte[]) this.dev_scan_data.get(pos))[60]});
        }

        public int get_vid(int pos) {
            byte[] byte1000 = (byte[]) this.dev_scan_data.get(pos);
            byte[] result = new byte[4];
            result[0] = (byte) 0;
            result[1] = (byte) 0;
            result[2] = (byte) 0;
            JDY_type tp = (JDY_type) this.dev_type.get(pos);
            if (tp == JDY_type.JDY || tp == JDY_type.JDY_LED1 || tp == JDY_type.JDY_LED2 || tp == JDY_type.JDY_AMQ || tp == JDY_type.JDY_KG || tp == JDY_type.JDY_WMQ || tp == JDY_type.JDY_LOCK) {
                result[3] = byte1000[13];
            } else {
                result[3] = byte1000[56];
            }
            return byteArrayToInt1(result);
        }

        public int byteArrayToInt1(byte[] bytes) {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                value += (bytes[i] & MotionEventCompat.ACTION_MASK) << ((3 - i) * 8);
            }
            return value;
        }

        private String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder(src.length);
            int length = src.length;
            for (int i = 0; i < length; i++) {
                stringBuilder.append(String.format("%02X", new Object[]{Byte.valueOf(src[i])}));
            }
            return stringBuilder.toString();
        }

        private String bytesToHexString1(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder(src.length);
            int length = src.length;
            for (int i = 0; i < length; i++) {
                stringBuilder.append(String.format(" %02X", new Object[]{Byte.valueOf(src[i])}));
            }
            return stringBuilder.toString();
        }
    }

    class ViewHolder {
        TextView device_rssi;
        TextView ibeacon_mac;
        TextView ibeacon_major;
        TextView ibeacon_minor;
        TextView ibeacon_name;
        TextView ibeacon_rssi;
        TextView ibeacon_uuid;
        TextView led_mac;
        TextView led_name;
        TextView led_rssi;
        TextView led_type113;
        TextView massager_mac;
        TextView massager_name;
        TextView massager_rssi;
        TextView massager_type113;
        TextView scan_data;
        TextView sensor_batt;
        TextView sensor_humid;
        TextView sensor_mac;
        TextView sensor_name;
        TextView sensor_rssi;
        TextView sensor_temp;
        TextView sensor_type0;
        TextView switch_mac;
        TextView switch_name;
        TextView switch_rssi;
        TextView switch_type113;
        TextView tv_devAddress;
        TextView tv_devName;
        TextView type0;
        ImageView type_imageView2;

        ViewHolder() {
        }
    }

    public JDY_type dv_type(byte[] p) {

        if (p.length != 62) {
            return null;
        }

        byte m1 = (byte) (p[20] + 1 ^ 0x11);
        byte m2 = (byte) (p[19] + 1 ^ 0x22);

        int ib1_major = 0;
        int ib1_minor = 0;
        if (p[52] == (byte) -1 && p[53] == (byte) -1) {
            ib1_major = 1;
        }
        if (p[54] == (byte) -1 && p[55] == (byte) -1) {
            ib1_minor = 1;
        }
        if (p[5] == (byte) -32 && p[6] == (byte) -1 && p[11] == m1 && p[12] == m2 && this.dev_VID == p[13]) {
            byte[] WriteBytes = new byte[4];
            WriteBytes[0] = p[13];
            WriteBytes[1] = p[14];
            Log.d("out_1", "TC" + this.list_cell_0.bytesToHexString1(WriteBytes));
            if (p[14] == (byte) -96) {
                return JDY_type.JDY;
            }
            if (p[14] == (byte) -91) {
                return JDY_type.JDY_AMQ;
            }
            if (p[14] == (byte) -79) {
                return JDY_type.JDY_LED1;
            }
            if (p[14] == (byte) -78) {
                return JDY_type.JDY_LED2;
            }
            if (p[14] == (byte) -60) {
                return JDY_type.JDY_KG;
            }
            return JDY_type.JDY;
        } else if (p[44] == (byte) 16 && p[45] == (byte) 22 && (ib1_major == 1 || ib1_minor == 1)) {
            return JDY_type.sensor_temp;
        } else {
            if (p[44] != (byte) 16 || p[45] != (byte) 22) {
                return JDY_type.UNKW;
            }
            if (p[57] == (byte) -32) {
                return JDY_type.JDY_iBeacon;
            }
            if (p[57] == (byte) -31) {
                return JDY_type.sensor_temp;
            }
            if (p[57] == (byte) -30) {
                return JDY_type.sensor_humid;
            }
            if (p[57] == (byte) -29) {
                return JDY_type.sensor_temp_humid;
            }
            if (p[57] == (byte) -28) {
                return JDY_type.sensor_fanxiangji;
            }
            if (p[57] == (byte) -27) {
                return JDY_type.sensor_zhilanshuibiao;
            }
            if (p[57] == (byte) -26) {
                return JDY_type.sensor_dianyabiao;
            }
            if (p[57] == (byte) -25) {
                return JDY_type.sensor_dianliu;
            }
            if (p[57] == (byte) -24) {
                return JDY_type.sensor_zhonglian;
            }
            if (p[57] == (byte) -23) {
                return JDY_type.sensor_pm2_5;
            }
            return JDY_type.JDY_iBeacon;
        }
    }

    public DeviceListAdapter(BluetoothAdapter adapter, Context context1) {
        this.apter = adapter;
        this.context = context1;
        this.list_cell_0 = new DeviceListAdapter1();
        this.timer.schedule(this.task, 1000, 1000);
    }

    public DeviceListAdapter1 init_adapter() {
        return this.list_cell_0;
    }

    public BluetoothDevice get_item_dev(int pos) {
        return (BluetoothDevice) this.list_cell_0.dev_ble.get(pos);
    }

    public JDY_type get_item_type(int pos) {
        return (JDY_type) this.list_cell_0.dev_type.get(pos);
    }

    public int get_count() {
        return this.list_cell_0.getCount();
    }

    public String get_iBeacon_uuid(int pos) {
        return this.list_cell_0.get_ibeacon_uuid(pos);
    }

    public String get_ibeacon_major(int pos) {
        return this.list_cell_0.get_ibeacon_major(pos);
    }

    public String get_ibeacon_minor(int pos) {
        return this.list_cell_0.get_ibeacon_minor(pos);
    }

    public String get_sensor_temp(int pos) {
        return this.list_cell_0.get_sensor_temp(pos);
    }

    public String get_sensor_humid(int pos) {
        return this.list_cell_0.get_sensor_humid(pos);
    }

    public String get_sensor_batt(int pos) {
        return this.list_cell_0.get_sensor_batt(pos);
    }

    public byte get_vid(int pos) {
        return (byte) this.list_cell_0.get_vid(pos);
    }

    public void set_vid(byte vid) {
        this.dev_VID = vid;
    }

    public void loop_list() {
        this.list_cell_0.loop();
    }

    public void stop_flash() {
        this.stop_timer = false;
    }

    public void start_flash() {
        this.stop_timer = true;
    }

    public void clear() {
        this.list_cell_0.clear();
    }

    public void scan_jdy_ble(Boolean p) {
        if (p.booleanValue()) {
            this.list_cell_0.notifyDataSetChanged();
            this.apter.startLeScan(this.mLeScanCallback);
            start_flash();
            return;
        }
        this.apter.stopLeScan(this.mLeScanCallback);
        stop_flash();
    }
}