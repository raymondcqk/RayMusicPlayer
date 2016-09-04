package com.raymondqk.raymusicplayer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 陈其康 raymondchan on 2016/9/3 0003.
 */
public class MusicMode implements Parcelable {
    private String title;
    private String artist;
    private String duration;
    private Bitmap avatar;
    private Uri mUri;
    private boolean isFavor;

    @Override
    public String toString() {
        return "MusicMode{" +
                "title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration='" + duration + '\'' +
                ", avatar=" + avatar +
                ", mUri=" + mUri.toString() +
                ", isFavor=" + isFavor +
                '}';
    }

    public MusicMode() {

    }

    public MusicMode(Parcel in) {
        title = in.readString();
        artist = in.readString();
        duration = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        mUri = in.readParcelable(Uri.class.getClassLoader());
        isFavor = in.readByte() != 0;
    }

    public static final Creator<MusicMode> CREATOR = new Creator<MusicMode>() {
        @Override
        public MusicMode createFromParcel(Parcel in) {
            return new MusicMode(in);
        }

        @Override
        public MusicMode[] newArray(int size) {
            return new MusicMode[size];
        }
    };

    public boolean isFavor() {
        return isFavor;
    }

    public void setFavor(boolean favor) {
        isFavor = favor;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(duration);
        dest.writeParcelable(avatar, flags);
        dest.writeParcelable(mUri, flags);
        dest.writeByte((byte) (isFavor ? 1 : 0));
    }
}
