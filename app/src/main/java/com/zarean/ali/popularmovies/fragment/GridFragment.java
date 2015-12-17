package com.zarean.ali.popularmovies.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.zarean.ali.popularmovies.Application.Global;
import com.zarean.ali.popularmovies.adapter.GridAdapter;
import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.data.DataContract;
import com.zarean.ali.popularmovies.utility.TheMovieDBApi;

/**
 * A placeholder fragment containing a simple view.
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String[] COLUMNS = {
            DataContract.MovieEntry.TABLE_NAME + "." + DataContract.MovieEntry._ID,
            DataContract.MovieEntry.COLUMN_TITLE,
            DataContract.MovieEntry.COLUMN_POSTER,
            DataContract.MovieEntry.COLUMN_FAVORITE
    };
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_POSTER = 2;
    public static final int COLUMN_FAVORITE = 3;

    public static final String ARGS_URI_KEY = "uri";
    public static final String ARGS_SELECTED_KEY = "selected";
    public static final String ARGS_NAME_KEY = "name";
    private final int MOVIE_LOADER = 0;

    private GridAdapter mGridAdapter;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    // Update Position Once
    private int updateFlag = 1;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        public void onItemSelected(Long id);
    }

    public GridFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate Layout
        View rootView =  inflater.inflate(R.layout.fragment_grid, container, false);
        // set Adapter to gridView
        mGridView = (GridView) rootView.findViewById(R.id.main_gridView);
        mGridAdapter = new GridAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor!=null){
                    mPosition = position;
                    Long movie_id = Long.parseLong(cursor.getString(COLUMN_ID));
                    new TheMovieDBApi(getContext()).refreshMovie(id);
                    ((Callback) getActivity()).onItemSelected(id);
                }
            }
        });
        // update Position
        if(savedInstanceState!=null && savedInstanceState.containsKey(ARGS_SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(ARGS_SELECTED_KEY);
        }
        else if(getArguments()!=null && getArguments().containsKey(ARGS_SELECTED_KEY)){
            mPosition = getArguments().getInt(ARGS_SELECTED_KEY);
        }

        // Start Loader
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        // Return
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.e(Global.LOG_TAG, "[" + getArguments().getString(ARGS_NAME_KEY) + "]=[" + mPosition + "]");
        updatePosition();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateFlag=1;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARGS_SELECTED_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    public int getPosition(){
        return mPosition;
    }

    private void updatePosition(){
        if(updateFlag==1 && mGridAdapter.getCount()>mPosition){
            mGridView.setItemChecked(mPosition, true);
            mGridView.setSelection(mPosition);
            mGridView.smoothScrollToPosition(mPosition);
            mGridView.post(new Runnable() {
                @Override
                public void run() {
                    mGridView.setItemChecked(mPosition, true);
                    mGridView.setSelection(mPosition);
                    mGridView.smoothScrollToPosition(mPosition);
                }
            });
            updateFlag=0;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                (Uri) this.getArguments().get(ARGS_URI_KEY),
                COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGridAdapter.swapCursor(data);
        updatePosition();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridAdapter.swapCursor(null);
    }
}
