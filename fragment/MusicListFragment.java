package com.ldw.music.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.adapter.MyAdapter;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicUtils;

import java.util.List;

import static com.ldw.music.activity.IConstants.BROADCAST_NAME;
import static com.ldw.music.activity.IConstants.BROADCAST_QUERY_COMPLETE_NAME;
import static com.ldw.music.activity.IConstants.MPS_INVALID;
import static com.ldw.music.activity.IConstants.MPS_NOFILE;
import static com.ldw.music.activity.IConstants.PLAY_MUSIC_INDEX;
import static com.ldw.music.activity.IConstants.PLAY_STATE_NAME;
import static com.ldw.music.activity.IConstants.START_FROM_LOCAL;

public class MusicListFragment extends BaseFragment {

    private MyAdapter mAdapter;
    private ListView mListView;
    private ServiceManager mServiceManager = null;
    private MusicPlayBroadcast mPlayBroadcast;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_list, container, false);

        initView(rootView);

        initListViewStatus();

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mPlayBroadcast);
    }

    private void initView(View view) {

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mServiceManager = MusicApp.mServiceManager;
        mListView = (ListView) view.findViewById(R.id.music_listview);

        mAdapter = new MyAdapter(mContext, mServiceManager);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                mAdapter.refreshPlayingList();
                mServiceManager
                        .playById(mAdapter.getData().get(position).songId);
            }
        });

        mAdapter.setData(MusicUtils.queryMusic(mContext, START_FROM_LOCAL));

        mPlayBroadcast = new MusicPlayBroadcast();
        IntentFilter filter = new IntentFilter(BROADCAST_NAME);
        filter.addAction(BROADCAST_NAME);
        filter.addAction(BROADCAST_QUERY_COMPLETE_NAME);
        getActivity().registerReceiver(mPlayBroadcast, filter);
    }

    private void initListViewStatus() {
        try {
            int playState = mServiceManager.getPlayState();
            if (playState == MPS_NOFILE || playState == MPS_INVALID) {
                return;
            }

            List<MusicInfo> musicList = mAdapter.getData();
            int playingSongPosition = MusicUtils.seekPosInListById(musicList,
                    mServiceManager.getCurMusicId());
            mAdapter.setPlayState(playState, playingSongPosition);
            MusicInfo music = mServiceManager.getCurMusic();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MusicPlayBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_NAME)) {
                int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
                int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
                mAdapter.setPlayState(playState, curPlayIndex);
                mListView.setSelection(curPlayIndex);

            }
        }
    }
}
