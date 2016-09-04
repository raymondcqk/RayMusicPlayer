package com.raymondqk.raymusicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.raymondqk.raymusicplayer.MusicMode;
import com.raymondqk.raymusicplayer.R;
import com.raymondqk.raymusicplayer.customview.AvatarCircle;

import java.util.List;

/**
 * Created by 陈其康 raymondchan on 2016/8/4 0004.
 */
public class MusicListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<MusicMode> mMusicModes;


    public MusicListAdapter(Context context, List<MusicMode> musicModes) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMusicModes = musicModes;
    }

    @Override
    public int getCount() {
        return mMusicModes.size();
    }

    @Override
    public Object getItem(int position) {
        return mMusicModes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.avatarCircle = (AvatarCircle) convertView.findViewById(R.id.avatar_list_item);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.tv_artist_list_item);
            viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mMusicModes.get(position).getAvatar() != null) {
            viewHolder.avatarCircle.setImageBitmap(mMusicModes.get(position).getAvatar());
        }else {
            viewHolder.avatarCircle.setImageResource(R.mipmap.blueball_72px);
        }

        viewHolder.title.setText(mMusicModes.get(position).getTitle());
        viewHolder.artist.setText(mMusicModes.get(position).getArtist());
        return convertView;
    }

    class ViewHolder {
        AvatarCircle avatarCircle;
        TextView title;
        TextView artist;
    }
}
