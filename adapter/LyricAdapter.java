package com.ldw.music.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.model.LyricSentence;

public class LyricAdapter extends BaseAdapter {
	private static final String TAG = LyricAdapter.class.getSimpleName();


	List<LyricSentence> mLyricSentences = null;

	Context mContext = null;


	int mIndexOfCurrentSentence = 0;

	float mCurrentSize = 20;
	float mNotCurrentSize = 17;

	public LyricAdapter(Context context) {
		mContext = context;
		mLyricSentences = new ArrayList<LyricSentence>();
		mIndexOfCurrentSentence = 0;
	}


	public void setLyric(List<LyricSentence> lyric) {
		mLyricSentences.clear();
		if (lyric != null) {
			mLyricSentences.addAll(lyric);
			Log.i(TAG, "total sentences =" + mLyricSentences.size());
		}
		mIndexOfCurrentSentence = 0;
	}

	@Override
	public boolean isEmpty() {

		if (mLyricSentences == null) {
			Log.i(TAG, "isEmpty:null");
			return true;
		} else if (mLyricSentences.size() == 0) {
			Log.i(TAG, "isEmpty:size=0");
			return true;
		} else {
			Log.i(TAG, "isEmpty:not empty");
			return false;
		}
	}

	@Override
	public boolean isEnabled(int position) {

		return false;
	}

	@Override
	public int getCount() {
		return mLyricSentences.size();
	}

	@Override
	public Object getItem(int position) {
		return mLyricSentences.get(position).getContentText();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.lyric_line, null);
			holder.lyric_line = (TextView) convertView
					.findViewById(R.id.lyric_line_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position >= 0 && position < mLyricSentences.size()) {
			holder.lyric_line.setText(mLyricSentences.get(position)
					.getContentText());
		}
		if (mIndexOfCurrentSentence == position) {

			holder.lyric_line.setTextColor(Color.WHITE);
			holder.lyric_line.setTextSize(mCurrentSize);
		} else {

			holder.lyric_line.setTextColor(mContext.getResources().getColor(
					R.color.trans_white));
			holder.lyric_line.setTextSize(mNotCurrentSize);
		}
		return convertView;
	}

	public void setCurrentSentenceIndex(int index) {
		mIndexOfCurrentSentence = index;
	}

	static class ViewHolder {
		TextView lyric_line;
	}
}
