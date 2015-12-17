package com.zarean.ali.popularmovies.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;
import com.zarean.ali.popularmovies.Application.Global;
import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.adapter.ReviewAdapter;
import com.zarean.ali.popularmovies.adapter.VideoAdapter;
import com.zarean.ali.popularmovies.data.DataContract;
import com.zarean.ali.popularmovies.utility.ExpandableHeightGridView;
import com.zarean.ali.popularmovies.utility.TheMovieDBApi;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] REVIEWS_COLUMNS = {
            DataContract.ReviewsEntry._ID,
            DataContract.ReviewsEntry.COLUMN_AUTHOR,
            DataContract.ReviewsEntry.COLUMN_CONTENT
    };
    public static final int REVIEWS_COLUMN_ID = 0;
    public static final int REVIEWS_COLUMN_AUTHOR = 1;
    public static final int REVIEWS_COLUMN_CONTENT = 2;

    private static final String[] VIDEOS_COLUMNS = {
            DataContract.VideosEntry._ID,
            DataContract.VideosEntry.COLUMN_NAME,
            DataContract.VideosEntry.COLUMN_KEY
    };
    public static final int VIDEOS_COLUMN_ID = 0;
    public static final int VIDEOS_COLUMN_NAME = 1;
    public static final int VIDEOS_COLUMN_KEY = 2;

    private static final String[] COLUMNS = {
            DataContract.MovieEntry.TABLE_NAME + "." + DataContract.MovieEntry._ID,
            DataContract.MovieEntry.COLUMN_LANGUAGE,
            DataContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            DataContract.MovieEntry.COLUMN_TITLE,
            DataContract.MovieEntry.COLUMN_OVERVIEW,
            DataContract.MovieEntry.COLUMN_DATE,
            DataContract.MovieEntry.COLUMN_POSTER,
            DataContract.MovieEntry.COLUMN_BACKDROP,
            DataContract.MovieEntry.COLUMN_POPULARITY,
            DataContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            DataContract.MovieEntry.COLUMN_VOTE_COUNT,
            DataContract.MovieEntry.COLUMN_ADULT,
            DataContract.MovieEntry.COLUMN_GENRES,
            DataContract.MovieEntry.COLUMN_FAVORITE};
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_LANGUAGE = 1;
    public static final int COLUMN_ORIGINAL_TITLE = 2;
    public static final int COLUMN_TITLE = 3;
    public static final int COLUMN_OVERVIEW = 4;
    public static final int COLUMN_DATE = 5;
    public static final int COLUMN_POSTER = 6;
    public static final int COLUMN_BACKDROP = 7;
    public static final int COLUMN_POPULARITY = 8;
    public static final int COLUMN_VOTE_AVERAGE = 9;
    public static final int COLUMN_VOTE_COUNT = 10;
    public static final int COLUMN_ADULT = 11;
    public static final int COLUMN_GENRES = 12;
    public static final int COLUMN_FAVORITE = 13;

    public static final String ARGS_ID_KEY = "id";
    private final int MOVIE_LOADER = 0;
    private final int REVIEWS_LOADER = 1;
    private final int VIDEOS_LOADER = 2;

    private ImageView mImageViewBackdrop;
    private TextView mTextViewTitle;
    private ImageView mImageViewPoster;
    private TextView mTextViewDate;
    private TextView mTextViewGenres;
    private TextView mTextViewLanguage;
    private TextView mTextViewPopularity;
    private TextView mTextViewRating;
    private TextView mTextViewVotes;
    private TextView mTextViewOverview;
    private ToggleButton mToggleButton;

    private ReviewAdapter mReviewAdapter;
    private VideoAdapter mVideoAdapter;
    private ShareActionProvider mShareActionProvider;
    private String mShareKey = null;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_detail, menu);
        MenuItem menuItemShare = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItemShare);
        super.onCreateOptionsMenu(menu, inflater);
        if (mShareKey != null) {
            mShareActionProvider.setShareIntent(createShareIntent(mShareKey));
        }
    }

    private Intent createShareIntent(String key) {
        // create Uri
        Uri uri = Uri.parse(TheMovieDBApi.TMDB_VIDEOS_URI).buildUpon()
                .appendQueryParameter(TheMovieDBApi.TMDB_VIDEOS_PARAM_V, key).build();

        // create Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        return shareIntent;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate
        final ListView listView = (ListView) inflater.inflate(R.layout.fragment_detail, container, false);
        final View headerView = inflater.inflate(R.layout.fragment_detail_header, null);
        final View footerView = inflater.inflate(R.layout.fragment_detail_footer, null);
        // add headers
        listView.addHeaderView(headerView, null, false);
        listView.addFooterView(footerView, null, false);


        mImageViewBackdrop = (ImageView) headerView.findViewById(R.id.detail_backdrop);
        mTextViewTitle = (TextView) headerView.findViewById(R.id.detail_title);
        mImageViewPoster = (ImageView) headerView.findViewById(R.id.detail_poster);
        mTextViewDate = (TextView) headerView.findViewById(R.id.detail_date);
        mTextViewGenres = (TextView) headerView.findViewById(R.id.detail_genres);
        mTextViewLanguage = (TextView) headerView.findViewById(R.id.detail_language);
        mTextViewPopularity = (TextView) headerView.findViewById(R.id.detail_popularity);
        mTextViewRating = (TextView) headerView.findViewById(R.id.detail_rating);
        mTextViewVotes = (TextView) headerView.findViewById(R.id.detail_votes);
        mTextViewOverview = (TextView) headerView.findViewById(R.id.detail_textView);
        mToggleButton = (ToggleButton) headerView.findViewById(R.id.detail_favorite);

        // set Adapters
        final ExpandableHeightGridView gridView = (ExpandableHeightGridView) footerView.findViewById(R.id.detail_grid_view);
        mReviewAdapter = new ReviewAdapter(getContext(), null, 0);
        mVideoAdapter = new VideoAdapter(getContext(), null, 0);
        gridView.setAdapter(mReviewAdapter);
        listView.setAdapter(mVideoAdapter);

        // set Video Click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    // get item
                    String key = cursor.getString(VIDEOS_COLUMN_KEY);
                    Uri uri = Uri.parse(TheMovieDBApi.TMDB_VIDEOS_URI).buildUpon()
                            .appendQueryParameter(TheMovieDBApi.TMDB_VIDEOS_PARAM_V, key).build();
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

        // set Review Click
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    // get item
                    String author = cursor.getString(REVIEWS_COLUMN_AUTHOR);
                    String content = cursor.getString(REVIEWS_COLUMN_CONTENT);

                    // create dialog
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.dialog_review);
                    dialog.setTitle(author);
                    TextView textView = (TextView) dialog.findViewById(R.id.dialog_review_text);
                    textView.setText(content);
                    dialog.show();
                }
            }
        });

        // Start Loader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
        getLoaderManager().initLoader(VIDEOS_LOADER, null, this);

        return listView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIE_LOADER: {
                Uri uri = DataContract.MovieEntry.CONTENT_URI.buildUpon().appendEncodedPath((this.getArguments().get(ARGS_ID_KEY)).toString()).build();
                return new CursorLoader(getActivity(), uri, COLUMNS, null, null, null);
            }
            case REVIEWS_LOADER: {
                Uri uri = DataContract.ReviewsEntry.CONTENT_URI.buildUpon().appendEncodedPath((this.getArguments().get(ARGS_ID_KEY)).toString()).build();
                return new CursorLoader(getActivity(), uri, REVIEWS_COLUMNS, null, null, null);
            }
            case VIDEOS_LOADER: {
                Uri uri = DataContract.VideosEntry.CONTENT_URI.buildUpon().appendEncodedPath((this.getArguments().get(ARGS_ID_KEY)).toString()).build();
                return new CursorLoader(getActivity(), uri, VIDEOS_COLUMNS, null, null, null);
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MOVIE_LOADER: {
                if (data.moveToFirst()) {
                    // Get Movie ID
                    final Long id = data.getLong(COLUMN_ID);
                    //Backdrop
                    Global.loadImage(data.getString(COLUMN_BACKDROP), mImageViewBackdrop);
                    // Title
                    mTextViewTitle.setText(data.getString(COLUMN_TITLE));
                    // Poster
                    Global.loadImage(data.getString(COLUMN_POSTER), mImageViewPoster, R.drawable.image_loading);
                    // Date
                    mTextViewDate.setText(data.getString(COLUMN_DATE).substring(0, 4));
                    // Genres
                    mTextViewGenres.setText(data.getString(COLUMN_GENRES));
                    // Language
                    mTextViewLanguage.setText(data.getString(COLUMN_LANGUAGE));
                    // Popularity
                    mTextViewPopularity.setText(String.format(getString(R.string.detail_popularity), data.getFloat(COLUMN_POPULARITY)));
                    // Rating
                    mTextViewRating.setText(String.format(getString(R.string.detail_voteAverage), data.getFloat(COLUMN_VOTE_AVERAGE)));
                    // Rating
                    mTextViewVotes.setText(String.format(getString(R.string.detail_voteCount), data.getLong(COLUMN_VOTE_COUNT)));
                    // OverView
                    mTextViewOverview.setText(data.getString(COLUMN_OVERVIEW));
                    // Favorite
                    mToggleButton.setChecked(data.getInt(COLUMN_FAVORITE) == 1 ? true : false);
                    mToggleButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri uri = DataContract.FavoritesEntry.CONTENT_URI.buildUpon().appendPath(id.toString()).build();
                            if (mToggleButton.isChecked()) {
                                getContext().getContentResolver().insert(uri, null);
                            } else {
                                getContext().getContentResolver().delete(uri, null, null);
                            }
                        }
                    });
                }
                break;
            }
            case REVIEWS_LOADER: {
                mReviewAdapter.swapCursor(data);
                break;
            }
            case VIDEOS_LOADER: {
                if (data.moveToFirst()) {
                    mShareKey = data.getString(VIDEOS_COLUMN_KEY);
                    if (mShareActionProvider != null)
                        mShareActionProvider.setShareIntent(createShareIntent(mShareKey));
                }
                mVideoAdapter.swapCursor(data);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
