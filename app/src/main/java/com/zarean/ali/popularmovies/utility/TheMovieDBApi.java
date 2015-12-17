package com.zarean.ali.popularmovies.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.loopj.android.http.*;
import com.zarean.ali.popularmovies.Application.Global;
import com.zarean.ali.popularmovies.data.DataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/*
Ali Zarean
11/21/2015
*/
public class TheMovieDBApi {

    private Context mContext;
    private HashMap<Integer, String> mGenreMap;

    private static final AsyncHttpClient sAsyncHttpClient = new AsyncHttpClient();
    private static final Integer TAG_MOVIE_REQUEST = 0;

    private static final String TMDB_URL = "http://api.themoviedb.org/3";
    private static final String TMDB_PATH_GENRES = "genre/movie/list";
    private static final String TMDB_PATH_MOVIE = "movie";
    private static final String TMDB_PATH_POPULAR = "popular";
    private static final String TMDB_PATH_TOP_RATED = "top_rated";
    private static final String TMDB_PATH_REVIEWS = "reviews";
    private static final String TMDB_PATH_VIDEOS = "videos";

    private static final String TMDB_PARAM_API_KEY = "api_key";
    private static final String TMDB_PARAM_APPEND = "append_to_response";

    private static final String TMDB_GENRES_KEY_GENRES = "genres";
    private static final String TMDB_GENRES_KEY_ID = "id";
    private static final String TMDB_GENRES_KEY_NAME = "name";

    private static final String TMDB_URL_IMAGE = "http://image.tmdb.org/t/p";
    private static final String TMDB_IMAGE_SIZE_POSTER = "w342";
    private static final String TMDB_IMAGE_SIZE_BACKDROP = "w780";
    //Available Sizes: w92,w154,w185,w342,w500,w780

    private static final String TMDB_KEY_PAGE = "page";
    private static final String TMDB_KEY_RESULTS = "results";
    private static final String TMDB_KEY_ADULT = "adult";
    private static final String TMDB_KEY_BACKDROP = "backdrop_path";
    private static final String TMDB_KEY_GENRE_IDS = "genre_ids";
    private static final String TMDB_KEY_GENRES = "genres";
    private static final String TMDB_KEY_ID = "id";
    private static final String TMDB_KEY_LANGUAGE = "original_language";
    private static final String TMDB_KEY_ORIGINAL_TITLE = "original_title";
    private static final String TMDB_KEY_OVERVIEW = "overview";
    private static final String TMDB_KEY_DATE = "release_date";
    private static final String TMDB_KEY_POSTER = "poster_path";
    private static final String TMDB_KEY_POPULARITY = "popularity";
    private static final String TMDB_KEY_TITLE = "title";
    private static final String TMDB_KEY_VIDEO = "video";
    private static final String TMDB_KEY_VOTE_AVERGAE = "vote_average";
    private static final String TMDB_KEY_VOTE_COUNT = "vote_count";

    private static final String TMDB_KEY_REVIEWS = "reviews";
    private static final String TMDB_KEY_VIDEOS = "videos";

    private static final String TMDB_REVIEWA_KEY_RESULTS = "results";
    private static final String TMDB_REVIEWS_KEY_AUTHOR = "author";
    private static final String TMDB_REVIEWS_KEY_CONTENT = "content";

    private static final String TMDB_VIDEOS_KEY_RESULTS = "results";
    private static final String TMDB_VIDEOS_KEY_KEY = "key";
    private static final String TMDB_VIDEOS_KEY_NAME = "name";
    public static final String TMDB_VIDEOS_URI = "https://www.youtube.com/watch";
    public static final String TMDB_VIDEOS_PARAM_V = "v";


    public TheMovieDBApi(Context context) {
        mContext = context;
        // Read Genres to HashMap
        mGenreMap = new HashMap<>();
        queryGenres();
    }

    public void queryGenres() {
        Cursor cursor = mContext.getContentResolver().query(DataContract.GenreEntry.CONTENT_URI, null, null, null, null);
        mGenreMap.clear();
        if (cursor.moveToFirst()) {
            mGenreMap.put(cursor.getInt(0), cursor.getString(1));
            while (cursor.moveToNext())
                mGenreMap.put(cursor.getInt(0), cursor.getString(1));
        }
    }

    public void refreshDatabase() {
        sAsyncHttpClient.cancelAllRequests(true);
        refreshGenres();
        refreshMovies(TMDB_PATH_POPULAR,
                DataContract.MostPopularEntry.CONTENT_URI,
                DataContract.MostPopularEntry._ID,
                DataContract.MostPopularEntry.COLUMN_MOVIE_ID,
                DataContract.MostPopularEntry.COLUMN_PAGE);
        refreshMovies(TMDB_PATH_TOP_RATED,
                DataContract.TopRatedEntry.CONTENT_URI,
                DataContract.TopRatedEntry._ID,
                DataContract.TopRatedEntry.COLUMN_MOVIE_ID,
                DataContract.TopRatedEntry.COLUMN_PAGE);
    }

    public void refreshMovie(final Long id) {
        sAsyncHttpClient.cancelRequestsByTAG(TAG_MOVIE_REQUEST, true);
        // Build the URL
        Uri builtUri = Uri.parse(TMDB_URL).buildUpon()
                .appendEncodedPath(TMDB_PATH_MOVIE)
                .appendEncodedPath(id.toString())
                .appendQueryParameter(TMDB_PARAM_APPEND, TMDB_PATH_REVIEWS + "," + TMDB_PATH_VIDEOS)
                .appendQueryParameter(TMDB_PARAM_API_KEY, com.zarean.ali.popularmovies.BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();

        // Connect to the URL and Download the movie
        sAsyncHttpClient.get(mContext, builtUri.toString(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    // Decode Downloaded JSON
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    ContentValues contentValues = decodeMovie(jsonObject);
                    // Insert movie
                    mContext.getContentResolver().insert(DataContract.MovieEntry.CONTENT_URI, contentValues);
                    // Reviews
                    ContentValues[] contentValuesReviews = decodeMovieReviews(jsonObject);
                    mContext.getContentResolver().delete(DataContract.ReviewsEntry.CONTENT_URI.buildUpon().appendPath(id.toString()).build(),null,null);
                    mContext.getContentResolver().bulkInsert(DataContract.ReviewsEntry.CONTENT_URI, contentValuesReviews);
                    // Videos
                    ContentValues[] contentValuesVideos = decodeMovieVideos(jsonObject);
                    mContext.getContentResolver().delete(DataContract.VideosEntry.CONTENT_URI.buildUpon().appendPath(id.toString()).build(),null,null);
                    mContext.getContentResolver().bulkInsert(DataContract.VideosEntry.CONTENT_URI, contentValuesVideos);

                    Log.e(Global.LOG_TAG, "[Updated][Movie " + id + "]");
                } catch (JSONException e) {
                    Log.e(Global.LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(Global.LOG_TAG, "[Failed][Movie " + id + "]");
            }

            @Override
            public void onStart() {
                Log.e(Global.LOG_TAG, "[Start][Movie " + id + "]");
            }

            @Override
            public void onCancel() {
                Log.e(Global.LOG_TAG, "[Cancel][Movie " + id + "]");
            }
        }).setTag(TAG_MOVIE_REQUEST);
    }

    private void refreshMovies(final String path, final Uri ReferenceUri, final String Ref_ID, final String Ref_COLUMN_MOVIE_ID, final String REF_COLUMN_PAGE) {
        // Build the Popular Movies URL
        Uri builtUri = Uri.parse(TMDB_URL).buildUpon()
                .appendEncodedPath(TMDB_PATH_MOVIE)
                .appendEncodedPath(path)
                .appendQueryParameter(TMDB_PARAM_API_KEY, com.zarean.ali.popularmovies.BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();

        // Connect to the URL and Download the movies
        sAsyncHttpClient.get(mContext, builtUri.toString(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Decode Downloaded JSON
                ContentValues[][] contentValuesArray = decodeMovies(new String(responseBody), Ref_ID, Ref_COLUMN_MOVIE_ID, REF_COLUMN_PAGE);
                // Insert movies
                if (contentValuesArray.length > 0) {
                    mContext.getContentResolver().bulkInsert(DataContract.MovieEntry.CONTENT_URI, contentValuesArray[0]);
                    mContext.getContentResolver().bulkInsert(ReferenceUri, contentValuesArray[1]);
                }
                Log.e(Global.LOG_TAG, "[Updated][Movies]");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(Global.LOG_TAG, "[Failed][Movies]");
            }

            @Override
            public void onStart() {
                Log.e(Global.LOG_TAG, "[Start][Movies]");
            }

            @Override
            public void onCancel() {
                Log.e(Global.LOG_TAG, "[Cancel][Movies]");
            }
        });
    }

    private void refreshGenres() {
        // Build the Genres URL
        Uri builtUri = Uri.parse(TMDB_URL).buildUpon()
                .appendEncodedPath(TMDB_PATH_GENRES)
                .appendQueryParameter(TMDB_PARAM_API_KEY, com.zarean.ali.popularmovies.BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();

        // Connect to the URL and Download the movies
        sAsyncHttpClient.get(mContext, builtUri.toString(), new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Decode Downloaded JSON
                ContentValues[] contentValuesArray = decodeGenres(new String(responseBody));
                // Insert genres
                if (contentValuesArray.length > 0) {
                    mContext.getContentResolver().bulkInsert(DataContract.GenreEntry.CONTENT_URI, contentValuesArray);
                }
                // Load them into HashMap
                queryGenres();
                Log.e(Global.LOG_TAG, "[Updated][Genres]");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(Global.LOG_TAG, "[Failed][Genres]");
            }

            @Override
            public void onStart() {
                Log.e(Global.LOG_TAG, "[Start][Genres]");
            }

            @Override
            public void onCancel() {
                Log.e(Global.LOG_TAG, "[Cancel][Genres]");
            }
        });
    }

    private ContentValues[][] decodeMovies(String jsonString, final String Ref_ID, final String Ref_COLUMN_MOVIE_ID, final String REF_COLUMN_PAGE) {
        // Create an ArrayList of Movies
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<>();
        ArrayList<ContentValues> contentValuesArrayListRef = new ArrayList<>();

        // Decode JSON and add Movies to the ArrayList
        try {
            // Extract the JSONArray of movies JSONObjects
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TMDB_KEY_RESULTS);

            // get page
            int page = jsonObject.getInt(TMDB_KEY_PAGE);

            // Iterate on movies JSONObjects
            for (int i = 0; i < jsonArray.length(); i++) {
                // Get the movie JSONObject
                JSONObject jsonObjectMovie = jsonArray.getJSONObject(i);

                // Decode Movie
                ContentValues contentValues = decodeMovie(jsonObjectMovie);

                // set ref values
                ContentValues contentValuesRef = new ContentValues();
                contentValuesRef.put(Ref_ID, (page - 1) * 20 + i);
                contentValuesRef.put(REF_COLUMN_PAGE, page);
                contentValuesRef.put(Ref_COLUMN_MOVIE_ID, contentValues.getAsLong(DataContract.MovieEntry._ID));

                // Add the movie to movies
                contentValuesArrayList.add(contentValues);
                contentValuesArrayListRef.add(contentValuesRef);
            }
        } catch (JSONException e) {
            // Error
        }

        // Convert ArrayList to Array
        ContentValues[][] contentValuesArray = new ContentValues[2][contentValuesArrayList.size()];
        contentValuesArrayList.toArray(contentValuesArray[0]);
        contentValuesArrayListRef.toArray(contentValuesArray[1]);

        // Return
        return contentValuesArray;
    }

    private ContentValues[] decodeMovieReviews(JSONObject jsonObjectMovie) throws JSONException {
        // Create a new ContentValues
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<>();

        // Extract ID
        long id = jsonObjectMovie.getLong(TMDB_KEY_ID);
        // Extract Reviews
        JSONArray jsonArrayReviews = jsonObjectMovie.getJSONObject(TMDB_KEY_REVIEWS).getJSONArray(TMDB_REVIEWA_KEY_RESULTS);
        for (int j = 0; j < jsonArrayReviews.length(); j++) {
            // Create ContentValues
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataContract.ReviewsEntry.COLUMN_MOVIE_ID, id);

            // decode
            JSONObject jsonObjectReview = jsonArrayReviews.getJSONObject(j);
            contentValues.put(DataContract.ReviewsEntry.COLUMN_AUTHOR, jsonObjectReview.getString(TMDB_REVIEWS_KEY_AUTHOR));
            contentValues.put(DataContract.ReviewsEntry.COLUMN_CONTENT, jsonObjectReview.getString(TMDB_REVIEWS_KEY_CONTENT));

            // Add to arraylist
            contentValuesArrayList.add(contentValues);
        }

        ContentValues[] contentValuesArray = new ContentValues[contentValuesArrayList.size()];
        contentValuesArrayList.toArray(contentValuesArray);
        return contentValuesArray;
    }
    private ContentValues[] decodeMovieVideos(JSONObject jsonObjectMovie) throws JSONException {
        // Create a new ContentValues
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<>();

        // Extract ID
        long id = jsonObjectMovie.getLong(TMDB_KEY_ID);
        // Extract Videos
        JSONArray jsonArrayVideos = jsonObjectMovie.getJSONObject(TMDB_KEY_VIDEOS).getJSONArray(TMDB_VIDEOS_KEY_RESULTS);
        for (int j = 0; j < jsonArrayVideos.length(); j++) {
            // Create ContentValues
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataContract.VideosEntry.COLUMN_MOVIE_ID, id);

            // decode
            JSONObject jsonObjectVideo = jsonArrayVideos.getJSONObject(j);
            contentValues.put(DataContract.VideosEntry.COLUMN_KEY, jsonObjectVideo.getString(TMDB_VIDEOS_KEY_KEY));
            contentValues.put(DataContract.VideosEntry.COLUMN_NAME, jsonObjectVideo.getString(TMDB_VIDEOS_KEY_NAME));

            // Add to arraylist
            contentValuesArrayList.add(contentValues);
        }

        ContentValues[] contentValuesArray = new ContentValues[contentValuesArrayList.size()];
        contentValuesArrayList.toArray(contentValuesArray);
        return contentValuesArray;
    }

    private ContentValues decodeMovie(JSONObject jsonObjectMovie) throws JSONException {
        // Create a new Movie
        ContentValues contentValues = new ContentValues();

        // Extract ID
        long id = jsonObjectMovie.getLong(TMDB_KEY_ID);
        contentValues.put(DataContract.MovieEntry._ID, id);
        // Extract language
        contentValues.put(DataContract.MovieEntry.COLUMN_LANGUAGE, jsonObjectMovie.getString(TMDB_KEY_LANGUAGE));
        // Extract originalTitle
        contentValues.put(DataContract.MovieEntry.COLUMN_ORIGINAL_TITLE, jsonObjectMovie.getString(TMDB_KEY_ORIGINAL_TITLE));
        // Extract title
        contentValues.put(DataContract.MovieEntry.COLUMN_TITLE, jsonObjectMovie.getString(TMDB_KEY_TITLE));
        // Extract overview
        contentValues.put(DataContract.MovieEntry.COLUMN_OVERVIEW, jsonObjectMovie.getString(TMDB_KEY_OVERVIEW));
        // Extract date
        contentValues.put(DataContract.MovieEntry.COLUMN_DATE, jsonObjectMovie.getString(TMDB_KEY_DATE));
        // Extract poster
        Uri posterUri = Uri.parse(TMDB_URL_IMAGE).buildUpon()
                .appendEncodedPath(TMDB_IMAGE_SIZE_POSTER)
                .appendEncodedPath(jsonObjectMovie.getString(TMDB_KEY_POSTER).substring(1))
                .appendQueryParameter(TMDB_PARAM_API_KEY, com.zarean.ali.popularmovies.BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        contentValues.put(DataContract.MovieEntry.COLUMN_POSTER, posterUri.toString());
        // Extract backdrop
        Uri backdropUri = Uri.parse(TMDB_URL_IMAGE).buildUpon()
                .appendEncodedPath(TMDB_IMAGE_SIZE_BACKDROP)
                .appendEncodedPath(jsonObjectMovie.getString(TMDB_KEY_BACKDROP).substring(1))
                .appendQueryParameter(TMDB_PARAM_API_KEY, com.zarean.ali.popularmovies.BuildConfig.THE_MOVIE_DB_API_KEY)
                .build();
        contentValues.put(DataContract.MovieEntry.COLUMN_BACKDROP, backdropUri.toString());
        // Extract popularity
        contentValues.put(DataContract.MovieEntry.COLUMN_POPULARITY, jsonObjectMovie.getString(TMDB_KEY_POPULARITY));
        // Extract voteAverage
        contentValues.put(DataContract.MovieEntry.COLUMN_VOTE_AVERAGE, jsonObjectMovie.getString(TMDB_KEY_VOTE_AVERGAE));
        // Extract voteCount
        contentValues.put(DataContract.MovieEntry.COLUMN_VOTE_COUNT, jsonObjectMovie.getString(TMDB_KEY_VOTE_COUNT));
        // Extract adult
        contentValues.put(DataContract.MovieEntry.COLUMN_ADULT, jsonObjectMovie.getBoolean(TMDB_KEY_ADULT));
        // Extract genre Ids
        JSONArray jsonArrayGenres = jsonObjectMovie.optJSONArray(TMDB_KEY_GENRE_IDS);
        StringBuilder buffer = new StringBuilder();
        if (jsonArrayGenres == null) {
            jsonArrayGenres = jsonObjectMovie.getJSONArray(TMDB_KEY_GENRES);
            for (int j = 0; j < jsonArrayGenres.length(); j++) {
                buffer.append(mGenreMap.get(jsonArrayGenres.getJSONObject(j).getInt(TMDB_GENRES_KEY_ID)));
                buffer.append(", ");
            }
        } else {
            for (int j = 0; j < jsonArrayGenres.length(); j++) {
                buffer.append(mGenreMap.get(jsonArrayGenres.getInt(j)));
                buffer.append(", ");
            }
        }
        contentValues.put(DataContract.MovieEntry.COLUMN_GENRES, buffer.substring(0, buffer.length() - 2));
        // Put Favorite
        contentValues.put(DataContract.MovieEntry.COLUMN_FAVORITE, 0);
        return contentValues;
    }


    private ContentValues[] decodeGenres(String jsonString) {
        // Create Empty ContentValues ArrayList
        ArrayList<ContentValues> contentValuesArrayList = new ArrayList<>();

        // Decode JSON
        try {
            // Extract the JSONArray of genres JSONObjects
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TMDB_GENRES_KEY_GENRES);

            // Iterate on genres JSONObjects
            for (int i = 0; i < jsonArray.length(); i++) {
                // Get the genre JSONObject
                JSONObject jsonObjectGenre = jsonArray.getJSONObject(i);
                // read key value
                Integer id = jsonObjectGenre.getInt(TMDB_GENRES_KEY_ID);
                String name = jsonObjectGenre.getString(TMDB_GENRES_KEY_NAME);
                // insert in contentValuesVector
                ContentValues contentValues = new ContentValues();
                contentValues.put(DataContract.GenreEntry._ID, id);
                contentValues.put(DataContract.GenreEntry.COLUMN_NAME, name);
                contentValuesArrayList.add(contentValues);
            }
        } catch (JSONException e) {
            // Error
        }

        // Convert ArrayList to Array
        ContentValues[] contentValuesArray = new ContentValues[contentValuesArrayList.size()];
        contentValuesArrayList.toArray(contentValuesArray);

        // Return
        return contentValuesArray;
    }
}
