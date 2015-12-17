package com.zarean.ali.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.fragment.DetailFragment;

/**
 * Created by Ali Zarean on 12/6/2015.
 */
public class ReviewAdapter extends CursorAdapter {

    public ReviewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    private static class ViewHolder{
        public final TextView textViewAuthor;
        public final TextView textViewContent;

        public ViewHolder(View root){
            textViewAuthor = (TextView) root.findViewById(R.id.review_item_author);
            textViewContent = (TextView) root.findViewById(R.id.review_item_content);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate Grid Item
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        // Attach ViewHolder as Tag
        view.setTag(new ViewHolder(view));
        // Return
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Get the viewHolder
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Load Author
        viewHolder.textViewAuthor.setText(cursor.getString(DetailFragment.REVIEWS_COLUMN_AUTHOR));
        // Load Content
        viewHolder.textViewContent.setText(cursor.getString(DetailFragment.REVIEWS_COLUMN_CONTENT));
    }
}
