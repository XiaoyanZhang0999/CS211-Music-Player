package com.ldw.music.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.activity.MainContentActivity;
import com.ldw.music.adapter.LyricAdapter;
import com.ldw.music.lrc.LyricDownloadManager;
import com.ldw.music.lrc.LyricLoadHelper;
import com.ldw.music.model.LyricSentence;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicTimer;
import com.ldw.music.view.AlwaysMarqueeTextView;
import com.ldw.music.view.MyLinearlayout;

import java.io.File;
import java.util.List;

import static com.ldw.music.activity.IConstants.BROADCAST_NAME;
import static com.ldw.music.activity.IConstants.MPS_INVALID;
import static com.ldw.music.activity.IConstants.MPS_NOFILE;
import static com.ldw.music.activity.IConstants.MPS_PAUSE;
import static com.ldw.music.activity.IConstants.MPS_PLAYING;
import static com.ldw.music.activity.IConstants.MPS_PREPARE;
import static com.ldw.music.activity.IConstants.PLAY_STATE_NAME;

public class MusicLyricFragment extends BaseFragment implements View.OnClickListener {

    private ListView mLrcListView;
    private LyricDownloadManager mLyricDownloadManager;
    private LyricLoadHelper mLyricLoadHelper;
    private LyricAdapter mLyricAdapter;
    private TextView mLrcEmptyView;

    private boolean mIsLyricDownloading;
    private int mScreenWidth;
    private MusicPlayBroadcast mPlayBroadcast;
    private MusicInfo mCurrentMusicInfo;
    private AlwaysMarqueeTextView mMusicNameTv, mArtistTv;
    private SeekBar mPlaybackProgress;
    public Handler mHandler;
    private TextView mPositionTv, mDurationTv;
    private MusicTimer mMusicTimer;
    private boolean mPlayAuto = true;
    private int mProgress;
    private MyLinearlayout layput_progress;
    private ServiceManager mServiceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceManager = MusicApp.mServiceManager;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_music_lyric, container, false);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mPlayBroadcast);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicTimer.stopTimer();

    }

    private void initView(View view) {

        mLyricDownloadManager = new LyricDownloadManager(mContext);
        mLyricLoadHelper = new LyricLoadHelper();
        mLyricLoadHelper.setLyricListener(mLyricListener);

        DisplayMetrics metric = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
        mScreenWidth = metric.widthPixels;


        mLyricAdapter = new LyricAdapter(mContext);

        // mLrcUtil = new LrcUtil();

        mLrcListView = (ListView) view.findViewById(R.id.lyricshow);
        mLrcEmptyView = (TextView) view.findViewById(R.id.lyric_empty);

        mLrcListView.setAdapter(mLyricAdapter);
        mLrcListView.setEmptyView(mLrcEmptyView);
        mLrcListView.startAnimation(AnimationUtils.loadAnimation(mContext,
                android.R.anim.fade_in));

        mLrcEmptyView.setOnClickListener(this);

        mMusicNameTv = (AlwaysMarqueeTextView) view.findViewById(R.id.musicname_tv2);
        mArtistTv = (AlwaysMarqueeTextView) view.findViewById(R.id.artist_tv2);

        mPositionTv = (TextView) view.findViewById(R.id.position_tv2);
        mDurationTv = (TextView) view.findViewById(R.id.duration_tv2);

        mPlaybackProgress = (SeekBar) view.findViewById(R.id.playback_seekbar);


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

        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        getActivity().registerReceiver(mPlayBroadcast, filter);


    }

    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            // Log.i(TAG, "onLyricLoaded");
            if (lyricSentences != null) {
                // Log.i(TAG, "onLyricLoaded--->total sentences =" + lyricSentences.size()
                // + ",current sentence =" + index);
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();

                // mHandler.sendMessageDelayed(
                // Message.obtain(null, MSG_SET_LYRIC_INDEX, index, 0),
                // 100);
            }
        }

        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            // Log.i(TAG, "onLyricSentenceChanged--->current sentence=" +
            // indexOfCurSentence);
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            mLrcListView.smoothScrollToPositionFromTop(indexOfCurSentence,
                    mLrcListView.getHeight() / 2, 500);
        }
    };

    @Override
    public void onClick(View v) {

        if (mCurrentMusicInfo == null) {
            return;
        }
//        showLrcDialog();

    }

    private void showLrcDialog() {
        View view = View.inflate(mContext, R.layout.lrc_dialog, null);
        view.setMinimumWidth(mScreenWidth - 40);
        final Dialog dialog = new Dialog(mContext, R.style.lrc_dialog);

        final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
        final Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
        final EditText artistEt = (EditText) view.findViewById(R.id.artist_tv);
        final EditText musicEt = (EditText) view.findViewById(R.id.music_tv);

        artistEt.setText(mCurrentMusicInfo.artist);
        musicEt.setText(mCurrentMusicInfo.musicName);
        View.OnClickListener btnListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v == okBtn) {
                    String artist = artistEt.getText().toString().trim();
                    String music = musicEt.getText().toString().trim();
                    if (TextUtils.isEmpty(artist) || TextUtils.isEmpty(music)) {
                        Toast.makeText(mContext, "artist and name can not be empty",
                                Toast.LENGTH_SHORT).show();
                    } else {

//						loadLyric(music, artist);
                        loadLyricByHand(music, artist);
                        dialog.dismiss();
                    }
                } else if (v == cancleBtn) {
                    dialog.dismiss();
                }
            }
        };
        okBtn.setOnClickListener(btnListener);
        cancleBtn.setOnClickListener(btnListener);
        dialog.setContentView(view);
        dialog.show();
    }


    public void loadLyric(MusicInfo playingSong) {
        if (playingSong == null) {
            return;
        }

        String lyricFilePath = MusicApp.lrcPath + "/" + playingSong.musicName
                + ".lrc";
        File lyricfile = new File(lyricFilePath);

        System.out.println(lyricfile.getPath());

        if (lyricfile.exists()) {

            // Log.i(TAG, "loadLyric()--->local lyric access");
            mLyricLoadHelper.loadLyric(lyricFilePath);
        } else {
            if (true) {
                mIsLyricDownloading = true;

                // Log.i(TAG, "loadLyric()---lyric from internet");
                new LyricDownloadAsyncTask().execute(playingSong.musicName,
                        playingSong.artist);
            } else {

                mLyricLoadHelper.loadLyric(null);
            }
        }
    }

    private void loadLyricByHand(String musicName, String artist) {

        String lyricFilePath = MusicApp.lrcPath + "/" + musicName + ".lrc";
        File lyricfile = new File(lyricFilePath);

        if (lyricfile.exists()) {

            // Log.i(TAG, "loadLyric()--->lyric access from local file");
            mLyricLoadHelper.loadLyric(lyricFilePath);
        } else {
            mIsLyricDownloading = true;

            // Log.i(TAG, "loadLyric()--->no local lyric file, access from internet");
            new LyricDownloadAsyncTask().execute(musicName, artist);

        }
    }

    class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(
                    params[0], params[1], mCurrentMusicInfo.musicName);

            mIsLyricDownloading = false;
            return lyricFilePath;
        }

        @Override
        protected void onPostExecute(String result) {

            mLyricLoadHelper.loadLyric(result);
        }

        ;
    }

    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {
                MusicInfo music = new MusicInfo();
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
                if (bundle != null) {
                    music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
                }
                switch (playState) {
                    case MPS_INVALID:
                        mMusicTimer.stopTimer();
                        mCurrentMusicInfo = music;
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        break;
                    case MPS_PAUSE:
                        mMusicTimer.stopTimer();
                        mCurrentMusicInfo = music;
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        break;
                    case MPS_PLAYING:
                        mMusicTimer.startTimer();
                        mCurrentMusicInfo = music;
                        refreshUI(mServiceManager.position(), music.duration,
                                music);
                        break;
                    case MPS_PREPARE:
                        mMusicTimer.stopTimer();
                        mCurrentMusicInfo = music;
                        refreshUI(mServiceManager.position(), music.duration,
                                music);

                        loadLyric(music);

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

        refreshSeekProgress(tempCurTime, tempTotalTime);
    }
}
