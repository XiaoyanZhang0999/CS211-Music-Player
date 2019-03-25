package com.ldw.music.activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.jdy_ble.BluetoothLeService;
import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.adapter.FragmentAdapter;
import com.ldw.music.aidl.IMediaService;
import com.ldw.music.db.FavoriteInfoDao;
import com.ldw.music.db.MusicInfoDao;
import com.ldw.music.fragment.MusicListFragment;
import com.ldw.music.fragment.MusicLyricFragment;
import com.ldw.music.fragment.MusicPlayFragment;
import com.ldw.music.interfaces.IOnServiceConnectComplete;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.ToastUtils;
import com.ldw.music.view.NoScrollViewPager;
import com.ldw.music.view.XCircleIndicator;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("HandlerLeak")
public class MainContentActivity extends FragmentActivity implements IConstants, OnClickListener {

    public static final String ALARM_CLOCK_BROADCAST = "alarm_clock_broadcast";
    private List<OnBackListener> mBackListeners = new ArrayList<OnBackListener>();

    private MusicInfoDao mMusicDao;
    private int mScreenWidth;

    private final static String TAG = MainContentActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private FavoriteInfoDao mFavoriteDao;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private FragmentAdapter mAdapter;
    private XCircleIndicator indicator;
    private ImageButton mPlayBtn, mPauseBtn, mNextBtn, mPrevBtn, mModeBtn, mFavBtn;
    private ServiceManager mServiceManager;
    boolean connect_status_bit = false;
    private Bitmap defaultArtwork;
    private MusicPlayBroadcast mPlayBroadcast;
    private int mCurMode;
    //    private int modeDrawable[] = {R.drawable.icon_list_reapeat,
//            R.drawable.icon_sequence, R.drawable.icon_shuffle,
//            R.drawable.icon_single_repeat};
    private int modeDrawable[] = {R.drawable.ic_list_reapeat, R.drawable.ic_shuffle,
            R.drawable.ic_single_repeat};
    private MusicInfo mMusicInfo;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // mConnected = true;

                connect_status_bit = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                connect_status_bit = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE1.equals(action))
            {
                displayData1(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA1));
            }
        }
    };
    private NoScrollViewPager viewpager;

    private void initView() {

        defaultArtwork = BitmapFactory.decodeResource(getResources(),
                R.drawable.img_album_background);

        mPlayBtn = (ImageButton) findViewById(R.id.btn_play2);
        mPauseBtn = (ImageButton) findViewById(R.id.btn_pause2);
        mNextBtn = (ImageButton) findViewById(R.id.btn_playNext2);
        mPrevBtn = (ImageButton) findViewById(R.id.btn_playPre);
        mModeBtn = (ImageButton) findViewById(R.id.btn_mode);
        mFavBtn = (ImageButton) findViewById(R.id.btn_favorite);

        mPlayBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPrevBtn.setOnClickListener(this);
        mModeBtn.setOnClickListener(this);
        mFavBtn.setOnClickListener(this);

        viewpager = (NoScrollViewPager) findViewById(R.id.viewpager);
        indicator = (XCircleIndicator) findViewById(R.id.xCircleIndicator);
        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

                indicator.setCurrentPage(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        registerReceiver(mPlayBroadcast, filter);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_play2:
                mServiceManager.rePlay();
                break;
            case R.id.btn_pause2:
                mServiceManager.pause();
                break;
            case R.id.btn_playNext2:
                mServiceManager.next();
                break;
            case R.id.btn_playPre:
                mServiceManager.prev();
                break;
            case R.id.btn_mode:
                changeMode();
                break;
            case R.id.btn_favorite:

                if (mMusicInfo != null) {
                    if (mFavoriteDao.isFavMusicInfo(mMusicInfo.songId)) {
                        mFavoriteDao.deleteBySongId(mMusicInfo.songId);
                        mFavBtn.setImageResource(R.drawable.icon_favourite_normal);
                    } else {
                        mFavoriteDao.saveMusicInfoBySongId(mMusicInfo);
                        mFavBtn.setImageResource(R.drawable.icon_favourite_checked);
                    }
                }


                break;
        }

    }

    public interface OnBackListener {
        public abstract void onBack();

    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;

        initSDCard();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ALARM_CLOCK_BROADCAST);
        registerReceiver(mAlarmReceiver, filter);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.frame_main);
        initView();


        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {

            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mFragments.add(new MusicListFragment());
        mFragments.add(new MusicPlayFragment());
        mFragments.add(new MusicLyricFragment());
        mAdapter = new FragmentAdapter(getSupportFragmentManager(), mFragments);
        viewpager.setAdapter(mAdapter);
        viewpager.setOffscreenPageLimit(3);

        indicator.initData(mFragments.size(), 0);

        indicator.setCurrentPage(0);

        mMusicDao = new MusicInfoDao(this);

        mFavoriteDao = new FavoriteInfoDao(this);

        getData();

        MusicApp.mServiceManager.connectService();
        MusicApp.mServiceManager.setOnServiceConnectComplete(new IOnServiceConnectComplete() {
            @Override
            public void onServiceConnectComplete(IMediaService service) {

            }
        });

        mServiceManager = MusicApp.mServiceManager;
        mCurMode = mServiceManager.getPlayMode();
    }

    private void initSDCard() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);

        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        // intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        // intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(sdCardReceiver, intentFilter);
    }

    private void getData() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mMusicDao.hasData()) {

                } else {
                    MusicUtils.queryMusic(MainContentActivity.this,
                            START_FROM_LOCAL);
                    MusicUtils.queryAlbums(MainContentActivity.this);
                    MusicUtils.queryArtist(MainContentActivity.this);
                    MusicUtils.queryFolder(MainContentActivity.this);
                }
            }
        }).start();
    }

    public void registerBackListener(OnBackListener listener) {
        if (!mBackListeners.contains(listener)) {
            mBackListeners.add(listener);
        }
    }

    public void unRegisterBackListener(OnBackListener listener) {
        mBackListeners.remove(listener);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("Please Select your action")
                    .setPositiveButton("Back Stage", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            moveTaskToBack(true);
                        }
                    })
                    .setNegativeButton("Exist", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private final BroadcastReceiver sdCardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.intent.action.MEDIA_REMOVED")
                    || action.equals("android.intent.action.MEDIA_UNMOUNTED")
                    || action.equals("android.intent.action.MEDIA_BAD_REMOVAL")
                    || action.equals("android.intent.action.MEDIA_SHARED")) {
                finish();
                Toast.makeText(MainContentActivity.this, "Can not restore!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void showSleepDialog() {

        if (MusicApp.mIsSleepClockSetting) {
            cancleSleepClock();
            Toast.makeText(getApplicationContext(), "Cancel sleeping mode！",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        View view = View.inflate(this, R.layout.sleep_time, null);
        final Dialog dialog = new Dialog(this, R.style.lrc_dialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        // lp.x = 100;
        // lp.y = 100;
        lp.width = (int) (mScreenWidth * 0.7);
        // lp.height = 400;


        // dialog.onWindowAttributesChanged(lp);
        dialogWindow.setAttributes(lp);

        dialog.show();

        final Button cancleBtn = (Button) view.findViewById(R.id.cancle_btn);
        final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        final EditText timeEt = (EditText) view.findViewById(R.id.time_et);
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == cancleBtn) {
                    dialog.dismiss();
                } else if (v == okBtn) {
                    String timeS = timeEt.getText().toString();
                    if (TextUtils.isEmpty(timeS)
                            || Integer.parseInt(timeS) == 0) {
                        Toast.makeText(getApplicationContext(), "Invalid Input！",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    setSleepClock(timeS);
                    dialog.dismiss();
                }
            }
        };

        cancleBtn.setOnClickListener(listener);
        okBtn.setOnClickListener(listener);
    }

    /**
     * Sleeping alarm
     *
     * @param timeS
     */
    private void setSleepClock(String timeS) {
        Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainContentActivity.this, 0, intent, 0);

        int time = Integer.parseInt(timeS);
        long longTime = time * 60 * 1000L;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + longTime,
                pendingIntent);
        MusicApp.mIsSleepClockSetting = true;
        Toast.makeText(getApplicationContext(), "will exist after " + timeS + " mins", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * cancel sleeping alarm
     */
    private void cancleSleepClock() {
        Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                MainContentActivity.this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        MusicApp.mIsSleepClockSetting = false;
    }

    private BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            finish();
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPlayBroadcast);
        unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService.close();
        }
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        unregisterReceiver(sdCardReceiver);
        unregisterReceiver(mAlarmReceiver);
        MusicApp.mServiceManager.exit();
        MusicApp.mServiceManager = null;
        MusicUtils.clearCache();
        cancleSleepClock();
        System.exit(0);
    }

    private void displayData(byte[] data1)
    {


    }

    private void displayData1(byte[] data1)
    {


    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null)
            return;

        if (gattServices.size() > 0 && mBluetoothLeService.get_connected_status(gattServices) == 2)
        {
            if (connect_status_bit) {
                mConnected = true;
                invalidateOptionsMenu();
                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(0);
                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(1);
                mBluetoothLeService.Delay_ms(100);

                byte[] WriteBytes = new byte[2];
                WriteBytes[0] = (byte) 0xE7;
                WriteBytes[1] = (byte) 0xf6;
                mBluetoothLeService.function_data(WriteBytes);

                ToastUtils.showToast(this, getString(R.string.connected));

            } else {

                ToastUtils.showToast(this, "Not connected！");
            }
        } else if (gattServices.size() > 0 && mBluetoothLeService.get_connected_status(gattServices) == 1)
        {
            if (connect_status_bit) {
                mConnected = true;
                invalidateOptionsMenu();

                mBluetoothLeService.Delay_ms(100);
                mBluetoothLeService.enable_JDY_ble(0);

            } else {
                ToastUtils.showToast(this, "Not connected！");
            }
        } else {
            ToastUtils.showToast(this, "Notice！The device is not JDY BLE part");
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE1);
        return intentFilter;
    }


    public void send(String dataAscii, String dataHex) {

        if (connect_status_bit) {
            if (mConnected) {
                mBluetoothLeService.txxx(dataHex, false);
//                mBluetoothLeService.txxx(dataAscii, true);
//                System.out.println("send:" + dataHex);
            }
        }
    }


    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {

//                mCurMode = mServiceManager.getPlayMode();

                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }

                mMusicInfo = music;

                switch (playState) {
                    case MPS_INVALID:
                        mServiceManager.next();
                        showPlay(true);
                        break;
                    case MPS_PAUSE:
                        mServiceManager.cancelNotification();
                        showPlay(true);
                        break;
                    case MPS_PLAYING:
                        Bitmap bitmap = MusicUtils.getCachedArtwork(MainContentActivity.this,
                                music.albumId, defaultArtwork);
                        // Bitmap bitmap = MusicUtils.getArtwork(getActivity(),
                        // music._id, music.albumId);

                        mServiceManager.updateNotification(bitmap, music.musicName,
                                music.artist);
                        showPlay(false);

                        break;
                    case MPS_PREPARE:
                        showPlay(true);
                        break;
                }
            }
        }
    }

    public void showPlay(boolean flag) {
        if (flag) {
            mPlayBtn.setVisibility(View.VISIBLE);
            mPauseBtn.setVisibility(View.GONE);
        } else {
            mPlayBtn.setVisibility(View.GONE);
            mPauseBtn.setVisibility(View.VISIBLE);
        }

        if (mMusicInfo != null) {
            if (mFavoriteDao.isFavMusicInfo(mMusicInfo.songId)) {
                mFavBtn.setImageResource(R.drawable.icon_favourite_checked);
            } else {
                mFavBtn.setImageResource(R.drawable.icon_favourite_normal);
            }
        }
    }


    private void changeMode() {
        mCurMode++;
        if (mCurMode > MPM_SINGLE_LOOP_PLAY) {
            mCurMode = MPM_LIST_LOOP_PLAY;
        }
        mServiceManager.setPlayMode(mCurMode);
        initPlayMode();
    }

    private void initPlayMode() {
        mModeBtn.setImageResource(modeDrawable[mCurMode]);
    }

    public void setScrool(boolean isScroll) {
        viewpager.setScroll(isScroll);
    }
}
