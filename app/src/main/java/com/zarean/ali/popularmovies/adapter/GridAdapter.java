package com.zarean.ali.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;
import com.zarean.ali.popularmovies.Application.Global;
import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.data.DataContract;
import com.zarean.ali.popularmovies.fragment.GridFragment;

/**
 * Created by Ali Zarean on 12/6/2015.
 */
public class GridAdapter extends CursorAdapter {

    public GridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private static class ViewHolder{
        public final ImageView imageView;
        public final TextView textView;
        public final ToggleButton toggleButton;

        public ViewHolder(View root){
            imageView = (ImageView) root.findViewById(R.id.grid_item_image);
            textView = (TextView) root.findViewById(R.id.grid_item_text);
            toggleButton = (ToggleButton) root.findViewById(R.id.grid_item_favorite);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate Grid Item
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        // Attach ViewHolder as Tag
        view.setTag(new ViewHolder(view));
        // Return
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Get the viewHolder
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Get Movie ID
        final Long id = cursor.getLong(GridFragment.COLUMN_ID);
        // Load Poster
        Global.loadImage(cursor.getString(GridFragment.COLUMN_POSTER), viewHolder.imageView, R.drawable.image_loading);
        // Load Title
        viewHolder.textView.setText(cursor.getString(GridFragment.COLUMN_TITLE));
        // Favorite
        viewHolder.toggleButton.setChecked(cursor.getInt(GridFragment.COLUMN_FAVORITE)==1?true:false);
        viewHolder.toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = DataContract.FavoritesEntry.CONTENT_URI.buildUpon().appendPath(id.toString()).build();
                if (viewHolder.toggleButton.isChecked()) {
                    context.getContentResolver().insert(uri, null);
                } else {
                    context.getContentResolver().delete(uri, null, null);
                }
            }
        });

    }
}
