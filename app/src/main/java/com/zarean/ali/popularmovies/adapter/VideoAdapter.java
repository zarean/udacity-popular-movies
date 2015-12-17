package com.zarean.ali.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.fragment.DetailFragment;
import com.zarean.ali.popularmovies.utility.TheMovieDBApi;

import cz.msebera.android.httpclient.client.utils.URIBuilder;

/**
 * Created by Ali Zarean on 12/6/2015.
 */
public class VideoAdapter extends CursorAdapter {

    public VideoAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private static class ViewHolder{
        public final TextView textViewName;

        public ViewHolder(View root){
            textViewName = (TextView) root.findViewById(R.id.video_item_text);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate Grid Item
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, parent, false);
        // Attach ViewHolder as Tag
        view.setTag(new ViewHolder(view));
        // Return
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Get the viewHolder
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Load Name
        viewHolder.textViewName.setText(cursor.getString(DetailFragment.VIDEOS_COLUMN_NAME));

        // Uri
        Uri uri = Uri.parse(TheMovieDBApi.TMDB_VIDEOS_URI).buildUpon()
                .appendQueryParameter(TheMovieDBApi.TMDB_VIDEOS_PARAM_V, cursor.getString(DetailFragment.VIDEOS_COLUMN_KEY))
                .build();
    }
}
