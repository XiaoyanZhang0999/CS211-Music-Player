package com.ldw.music.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


public class AlbumInfo implements Parcelable {
	
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_ALBUM_ID = "album_id";
	public static final String KEY_NUMBER_OF_SONGS = "number_of_songs";
	public static final String KEY_ALBUM_ART = "album_art";
	

	public String album_name;

	public int album_id = -1;

	public int number_of_songs = 0;

	public String album_art;

	@Override
	public int describeContents() {
		return 0;
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putString(KEY_ALBUM_NAME, album_name);
		bundle.putString(KEY_ALBUM_ART, album_art);
		bundle.putInt(KEY_NUMBER_OF_SONGS, number_of_songs);
		bundle.putInt(KEY_ALBUM_ID, album_id);
		dest.writeBundle(bundle);
	}
	
	public static final Parcelable.Creator<AlbumInfo> CREATOR = new Parcelable.Creator<AlbumInfo>() {


		@Override
		public AlbumInfo createFromParcel(Parcel source) {
			AlbumInfo info = new AlbumInfo();
			Bundle bundle = source.readBundle();
			info.album_name = bundle.getString(KEY_ALBUM_NAME);
			info.album_art = bundle.getString(KEY_ALBUM_ART);
			info.number_of_songs = bundle.getInt(KEY_NUMBER_OF_SONGS);
			info.album_id = bundle.getInt(KEY_ALBUM_ID);
			return info;
		}

		@Override
		public AlbumInfo[] newArray(int size) {
			return new AlbumInfo[size];
		}
	};

}
