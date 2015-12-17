package com.zarean.ali.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/*
Ali Zarean
11/21/2015
*/
public class DataProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DataHelper mDataHelper;

    // UriMatcher Codes
    private static final int URI_GENRE = 100;
    private static final int URI_MOVIE = 200;
    private static final int URI_MOVIE_ID = 201;
    private static final int URI_MOVIE_MOST_POPULAR = 210;
    private static final int URI_MOVIE_TOP_RATED = 220;
    private static final int URI_MOVIE_FAVORITES = 230;
    private static final int URI_MOST_POPULAR = 300;
    private static final int URI_TOP_RATED = 400;
    private static final int URI_FAVORITE = 500;
    private static final int URI_FAVORITE_ID = 501;
    private static final int URI_REVIEWS = 600;
    private static final int URI_REVIEWS_MOVIE_ID = 611;
    private static final int URI_VIDEOS = 700;
    private static final int URI_VIDEOS_MOVIE_ID = 711;


    private static UriMatcher buildUriMatcher() {
        // Create UriMatcher
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;

        // Add Uris
        uriMatcher.addURI(authority, DataContract.GenreEntry.TABLE_NAME, URI_GENRE);
        uriMatcher.addURI(authority, DataContract.MovieEntry.TABLE_NAME, URI_MOVIE);
        uriMatcher.addURI(authority, DataContract.MovieEntry.TABLE_NAME + "/#", URI_MOVIE_ID);
        uriMatcher.addURI(authority, DataContract.MovieEntry.TABLE_NAME + "/" + DataContract.MostPopularEntry.TABLE_NAME, URI_MOVIE_MOST_POPULAR);
        uriMatcher.addURI(authority, DataContract.MovieEntry.TABLE_NAME + "/" + DataContract.TopRatedEntry.TABLE_NAME, URI_MOVIE_TOP_RATED);
        uriMatcher.addURI(authority, DataContract.MovieEntry.TABLE_NAME + "/" + DataContract.FavoritesEntry.TABLE_NAME, URI_MOVIE_FAVORITES);
        uriMatcher.addURI(authority, DataContract.MostPopularEntry.TABLE_NAME, URI_MOST_POPULAR);
        uriMatcher.addURI(authority, DataContract.TopRatedEntry.TABLE_NAME, URI_TOP_RATED);
        uriMatcher.addURI(authority, DataContract.FavoritesEntry.TABLE_NAME, URI_FAVORITE);
        uriMatcher.addURI(authority, DataContract.FavoritesEntry.TABLE_NAME + "/#", URI_FAVORITE_ID);
        uriMatcher.addURI(authority, DataContract.ReviewsEntry.TABLE_NAME, URI_REVIEWS);
        uriMatcher.addURI(authority, DataContract.ReviewsEntry.TABLE_NAME + "/#", URI_REVIEWS_MOVIE_ID);
        uriMatcher.addURI(authority, DataContract.VideosEntry.TABLE_NAME, URI_VIDEOS);
        uriMatcher.addURI(authority, DataContract.VideosEntry.TABLE_NAME + "/#", URI_VIDEOS_MOVIE_ID);


        // Return
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDataHelper = new DataHelper(getContext());
        return true;
    }

    private void updateFavorites() {
        final String sql1 = "UPDATE " +
                DataContract.MovieEntry.TABLE_NAME +
                " SET " +
                DataContract.MovieEntry.COLUMN_FAVORITE +
                " = 0";
        final String sql2 = "UPDATE " +
                DataContract.MovieEntry.TABLE_NAME +
                " SET " +
                DataContract.MovieEntry.COLUMN_FAVORITE +
                " = 1 WHERE " +
                DataContract.MovieEntry._ID +
                " IN (SELECT " +
                DataContract.FavoritesEntry.COLUMN_MOVIE_ID +
                " FROM " +
                DataContract.FavoritesEntry.TABLE_NAME +
                ")";

        SQLiteDatabase db = mDataHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(sql1);
            db.execSQL(sql2);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private String createProjectionString(String[] columns){
        StringBuilder sb = new StringBuilder();
        for (String n : columns) {
            if (sb.length() > 0) sb.append(',');
            sb.append(n);
        }
        return sb.toString();
    }

    private String createQueryMoviesString(String[] projection, String table, String column_id, String column_movie_id){
        return "SELECT " + createProjectionString(projection) + " FROM " +
                DataContract.MovieEntry.TABLE_NAME +
                " INNER JOIN " +
                table +
                " b ON " +
                DataContract.MovieEntry.TABLE_NAME +
                "." +
                DataContract.MovieEntry._ID +
                "=b." +
                column_movie_id +
                " ORDER BY b." +
                column_id +
                " ASC";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case URI_GENRE: {
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.GenreEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_MOVIE: {
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_MOVIE_ID: {
                updateFavorites();
                selection = DataContract.MovieEntry._ID + "=" + uri.getLastPathSegment();
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_MOVIE_MOST_POPULAR: {
                updateFavorites();
                final String query = createQueryMoviesString(projection,
                        DataContract.MostPopularEntry.TABLE_NAME,
                        DataContract.MostPopularEntry._ID,
                        DataContract.MostPopularEntry.COLUMN_MOVIE_ID);
                retCursor = mDataHelper.getReadableDatabase().rawQuery(query, null);
                break;
            }
            case URI_MOVIE_TOP_RATED: {
                updateFavorites();
                final String query = createQueryMoviesString(projection,
                        DataContract.TopRatedEntry.TABLE_NAME,
                        DataContract.TopRatedEntry._ID,
                        DataContract.TopRatedEntry.COLUMN_MOVIE_ID);
                retCursor = mDataHelper.getReadableDatabase().rawQuery(query, null);
                break;
            }
            case URI_MOVIE_FAVORITES: {
                updateFavorites();
                final String query = createQueryMoviesString(projection,
                        DataContract.FavoritesEntry.TABLE_NAME,
                        DataContract.FavoritesEntry._ID,
                        DataContract.FavoritesEntry.COLUMN_MOVIE_ID);
                retCursor = mDataHelper.getReadableDatabase().rawQuery(query, null);
                break;
            }
            case URI_MOST_POPULAR: {
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.MostPopularEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_TOP_RATED: {
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.TopRatedEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_FAVORITE: {
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_REVIEWS_MOVIE_ID: {
                selection = DataContract.ReviewsEntry.COLUMN_MOVIE_ID + "=" + uri.getLastPathSegment();
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.ReviewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case URI_VIDEOS_MOVIE_ID: {
                selection = DataContract.VideosEntry.COLUMN_MOVIE_ID + "=" + uri.getLastPathSegment();
                retCursor = mDataHelper.getReadableDatabase().query(
                        DataContract.VideosEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case URI_GENRE: {
                return DataContract.GenreEntry.CONTENT_DIR_TYPE;
            }
            case URI_MOVIE: {
                return DataContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case URI_MOVIE_ID: {
                return DataContract.MovieEntry.CONTENT_ITEM_TYPE;
            }
            case URI_MOVIE_MOST_POPULAR: {
                return DataContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case URI_MOVIE_TOP_RATED: {
                return DataContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case URI_MOVIE_FAVORITES: {
                return DataContract.MovieEntry.CONTENT_DIR_TYPE;
            }
            case URI_MOST_POPULAR: {
                return DataContract.MostPopularEntry.CONTENT_DIR_TYPE;
            }
            case URI_TOP_RATED: {
                return DataContract.TopRatedEntry.CONTENT_DIR_TYPE;
            }
            case URI_FAVORITE: {
                return DataContract.FavoritesEntry.CONTENT_DIR_TYPE;
            }
            case URI_FAVORITE_ID: {
                return DataContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            }
            case URI_REVIEWS: {
                return DataContract.ReviewsEntry.CONTENT_DIR_TYPE;
            }
            case URI_REVIEWS_MOVIE_ID: {
                return DataContract.ReviewsEntry.CONTENT_DIR_TYPE;
            }
            case URI_VIDEOS: {
                return DataContract.VideosEntry.CONTENT_DIR_TYPE;
            }
            case URI_VIDEOS_MOVIE_ID: {
                return DataContract.VideosEntry.CONTENT_DIR_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDataHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case URI_GENRE: {
                long id = db.replace(DataContract.GenreEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DataContract.GenreEntry.buildUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case URI_MOVIE: {
                long id = db.replace(DataContract.MovieEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DataContract.MovieEntry.buildUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case URI_MOST_POPULAR: {
                long id = db.replace(DataContract.MostPopularEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DataContract.MostPopularEntry.buildUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case URI_TOP_RATED: {
                long id = db.replace(DataContract.TopRatedEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DataContract.TopRatedEntry.buildUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case URI_FAVORITE: {
                long id = db.replace(DataContract.FavoritesEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = DataContract.FavoritesEntry.buildUri(id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case URI_FAVORITE_ID: {
                values = new ContentValues();
                values.put(DataContract.FavoritesEntry.COLUMN_MOVIE_ID, Long.parseLong(uri.getLastPathSegment()));
                long id = db.replace(DataContract.FavoritesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = DataContract.FavoritesEntry.buildUri(id);
                    // Notify Movies
                    getContext().getContentResolver().notifyChange(DataContract.MovieEntry.CONTENT_URI, null);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDataHelper.getReadableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri)) {
            case URI_GENRE: {
                rowsDeleted = db.delete(DataContract.GenreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_MOVIE: {
                rowsDeleted = db.delete(DataContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_MOST_POPULAR: {
                rowsDeleted = db.delete(DataContract.MostPopularEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_TOP_RATED: {
                rowsDeleted = db.delete(DataContract.TopRatedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_FAVORITE: {
                rowsDeleted = db.delete(DataContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_FAVORITE_ID: {
                selection = DataContract.FavoritesEntry.COLUMN_MOVIE_ID + " = " + uri.getLastPathSegment();
                selectionArgs = null;
                rowsDeleted = db.delete(DataContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                // Notify Movies
                getContext().getContentResolver().notifyChange(DataContract.MovieEntry.CONTENT_URI, null);
                break;
            }
            case URI_REVIEWS_MOVIE_ID: {
                selection = DataContract.ReviewsEntry.COLUMN_MOVIE_ID + " = " + uri.getLastPathSegment();
                selectionArgs = null;
                rowsDeleted = db.delete(DataContract.ReviewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_VIDEOS_MOVIE_ID: {
                selection = DataContract.VideosEntry.COLUMN_MOVIE_ID + " = " + uri.getLastPathSegment();
                selectionArgs = null;
                rowsDeleted = db.delete(DataContract.VideosEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDataHelper.getReadableDatabase();
        int rowsUpdated;
        switch (sUriMatcher.match(uri)) {
            case URI_GENRE: {
                rowsUpdated = db.update(DataContract.GenreEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case URI_MOVIE: {
                rowsUpdated = db.update(DataContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case URI_MOST_POPULAR: {
                rowsUpdated = db.update(DataContract.MostPopularEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case URI_TOP_RATED: {
                rowsUpdated = db.update(DataContract.TopRatedEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case URI_FAVORITE: {
                rowsUpdated = db.update(DataContract.FavoritesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Nullable
    @Override
    public int bulkInsert(@NonNull Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDataHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case URI_GENRE: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(DataContract.GenreEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case URI_MOVIE: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(DataContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case URI_MOST_POPULAR: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(DataContract.MostPopularEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case URI_TOP_RATED: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(DataContract.TopRatedEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case URI_REVIEWS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(DataContract.ReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case URI_VIDEOS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.replace(DataContract.VideosEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default: {
                return super.bulkInsert(uri, values);
            }
        }
    }
}
