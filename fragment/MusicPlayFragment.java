package com.ldw.music.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.activity.MainContentActivity;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicTimer;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.view.AlwaysMarqueeTextView;
import com.ldw.music.view.MyLinearlayout;
import com.ldw.music.view.VisualizerView;

import static com.ldw.music.activity.IConstants.BROADCAST_NAME;
import static com.ldw.music.activity.IConstants.MPS_INVALID;
import static com.ldw.music.activity.IConstants.MPS_NOFILE;
import static com.ldw.music.activity.IConstants.MPS_PAUSE;
import static com.ldw.music.activity.IConstants.MPS_PLAYING;
import static com.ldw.music.activity.IConstants.MPS_PREPARE;
import static com.ldw.music.activity.IConstants.PLAY_MUSIC_INDEX;
import static com.ldw.music.activity.IConstants.PLAY_STATE_NAME;

public class MusicPlayFragment extends BaseFragment implements VisualizerView.OnFftListener {

    private static final float VISUALIZER_HEIGHT_DIP = 150f;
    private Visualizer mVisualizer;
    private LinearLayout mLayout;
    VisualizerView mBaseVisualizerView;
    private MusicPlayBroadcast mPlayBroadcast;
    private ServiceManager mServiceManager;
    private AlwaysMarqueeTextView mMusicNameTv, mArtistTv, mAlbumTv;
    private SeekBar mPlaybackProgress;
    public Handler mHandler;
    private Bitmap mDefaultAlbumIcon;
    private ImageView mHeadIcon;
    private TextView mPositionTv, mDurationTv;
    private MusicTimer mMusicTimer;
    private boolean mPlayAuto = true;
    private int mProgress;
    private MyLinearlayout layput_progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceManager = MusicApp.mServiceManager;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_play, container, false);

        initView(rootView);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                refreshSeekProgress(mServiceManager.position(),
                        mServiceManager.duration());
            }
        };

        mMusicTimer = new MusicTimer(mHandler);

        return rootView;
    }

    private void initView(View view) {

        mMusicNameTv = (AlwaysMarqueeTextView) view.findViewById(R.id.musicname_tv2);
        mArtistTv = (AlwaysMarqueeTextView) view.findViewById(R.id.artist_tv2);
        mAlbumTv = (AlwaysMarqueeTextView) view.findViewById(R.id.album_tv2);

        mPositionTv = (TextView) view.findViewById(R.id.position_tv2);
        mDurationTv = (TextView) view.findViewById(R.id.duration_tv2);

        mPlaybackProgress = (SeekBar) view.findViewById(R.id.playback_seekbar);

        mDefaultAlbumIcon = BitmapFactory.decodeResource(
                mContext.getResources(), R.drawable.img_album_background);

        mHeadIcon = (ImageView) view.findViewById(R.id.headicon_iv);

        mLayout = (LinearLayout) view.findViewById(R.id.layout);
        setupVisualizerFxAndUi();

        mPlaybackProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (!mPlayAuto) {
                    mProgress = progress;
                    // mServiceManager.seekTo(progress);
                    // refreshSeekProgress(mServiceManager.position(),
                    // mServiceManager.duration());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mPlayAuto = false;
                mMusicTimer.stopTimer();
                mServiceManager.pause();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mPlayAuto = true;
                mServiceManager.seekTo(mProgress);
                refreshSeekProgress(mServiceManager.position(),
                        mServiceManager.duration());
                mServiceManager.rePlay();
                mMusicTimer.startTimer();

            }
        });

        layput_progress = (MyLinearlayout) view.findViewById(R.id.layput_progress);

        layput_progress.setMainContentActivity((MainContentActivity) getActivity());

    }


    private void setupVisualizerFxAndUi() {
        mBaseVisualizerView = new VisualizerView(getActivity());
        mBaseVisualizerView.setListener(this);
        mBaseVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)
        ));

        mLayout.addView(mBaseVisualizerView);

        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        getActivity().registerReceiver(mPlayBroadcast, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mPlayBroadcast);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicTimer.stopTimer();
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
        }
    }

    @Override
    public void onFft(String dataAscii, String dataHex) {

        MainContentActivity activity = (MainContentActivity) getActivity();
        activity.send(dataAscii, dataHex);

    }

    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {
                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }
                switch (playState) {
                    case MPS_INVALID:
                        mMusicTimer.stopTimer();
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        break;
                    case MPS_PAUSE:
                        mMusicTimer.stopTimer();
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        break;
                    case MPS_PLAYING:
                        mMusicTimer.startTimer();
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        break;
                    case MPS_PREPARE:
                        mMusicTimer.stopTimer();
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        if (mVisualizer == null) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    mVisualizer = new Visualizer(mServiceManager.getAudioSessionId());
                                    System.out.println("getAudioSessionId:" + mServiceManager.getAudioSessionId());

                                    mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

                                    mBaseVisualizerView.setVisualizer(mVisualizer);
                                    mVisualizer.setEnabled(true);
                                }
                            }, 1000);
                        }
                        break;
                }
            }
        }
    }

    public void refreshSeekProgress(int curTime, int totalTime) {

        curTime /= 1000;
        totalTime /= 1000;
        int curminute = curTime / 60;
        int cursecond = curTime % 60;

        String curTimeString = String.format("%02d:%02d", curminute, cursecond);
        mPositionTv.setText(curTimeString);

        int rate = 0;
        if (totalTime != 0) {
            rate = (int) ((float) curTime / totalTime * 100);
        }
        mPlaybackProgress.setProgress(rate);
    }

    public void refreshUI(int curTime, int totalTime, MusicInfo music) {

        int tempCurTime = curTime;
        int tempTotalTime = totalTime;

        totalTime /= 1000;
        int totalminute = totalTime / 60;
        int totalsecond = totalTime % 60;
        String totalTimeString = String.format("%02d:%02d", totalminute,
                totalsecond);

        mDurationTv.setText(totalTimeString);

        mMusicNameTv.setText(music.musicName);
        mArtistTv.setText(music.artist);
        mAlbumTv.setText(music.album);

        Bitmap bitmap = MusicUtils.getCachedArtwork(mContext, music.albumId,
                mDefaultAlbumIcon);

        mHeadIcon.setImageDrawable(new BitmapDrawable(mContext
                .getResources(), bitmap));
        refreshSeekProgress(tempCurTime, tempTotalTime);
    }
}
