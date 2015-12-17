package com.zarean.ali.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
Ali Zarean
11/21/2015
*/
public class DataHelper extends SQLiteOpenHelper {

    // Database name & version
    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Build SQL commands
        final String SQL_CREATE_GENRE_TABLE = BuildSQLCreateTable(
                DataContract.GenreEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.GenreEntry._ID, "INTEGER PRIMARY KEY"},
                        {DataContract.GenreEntry.COLUMN_NAME, "TEXT NOT NULL"}
                }, null, null);
        final String SQL_CREATE_MOVIE_TABLE = BuildSQLCreateTable(
                DataContract.MovieEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.MovieEntry._ID, "INTEGER PRIMARY KEY"},
                        {DataContract.MovieEntry.COLUMN_LANGUAGE, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_TITLE, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_OVERVIEW, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_DATE, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_POSTER, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_BACKDROP, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_POPULARITY, "REAL NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_VOTE_AVERAGE, "REAL NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_VOTE_COUNT, "INTEGER NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_ADULT, "NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_GENRES, "TEXT NOT NULL"},
                        {DataContract.MovieEntry.COLUMN_FAVORITE, "NOT NULL"}
                }, null, null);
        final String SQL_CREATE_MOST_POPULAR_ENTRY = BuildSQLCreateTable(
                DataContract.MostPopularEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.MostPopularEntry._ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                        {DataContract.MostPopularEntry.COLUMN_MOVIE_ID, "INTEGER NOT NULL"},
                        {DataContract.MostPopularEntry.COLUMN_PAGE , "INTEGER NOT NULL"}
                },
                new String[][]{
                        {DataContract.MostPopularEntry.COLUMN_MOVIE_ID,
                                DataContract.MovieEntry.TABLE_NAME,
                                DataContract.MovieEntry._ID}
                },
                new String[]{
                        DataContract.MostPopularEntry.COLUMN_MOVIE_ID
                }
        );
        final String SQL_CREATE_TOP_RATED_ENTRY = BuildSQLCreateTable(
                DataContract.TopRatedEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.TopRatedEntry._ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                        {DataContract.TopRatedEntry.COLUMN_MOVIE_ID, "INTEGER NOT NULL"},
                        {DataContract.TopRatedEntry.COLUMN_PAGE , "INTEGER NOT NULL"}
                },
                new String[][]{
                        {DataContract.TopRatedEntry.COLUMN_MOVIE_ID,
                                DataContract.MovieEntry.TABLE_NAME,
                                DataContract.MovieEntry._ID}
                },
                new String[]{
                        DataContract.TopRatedEntry.COLUMN_MOVIE_ID
                }
        );
        final String SQL_CREATE_FAVORITE_ENTRY = BuildSQLCreateTable(
                DataContract.FavoritesEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.FavoritesEntry._ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                        {DataContract.FavoritesEntry.COLUMN_MOVIE_ID, "INTEGER NOT NULL"},
                },
                new String[][]{
                        {DataContract.FavoritesEntry.COLUMN_MOVIE_ID,
                                DataContract.MovieEntry.TABLE_NAME,
                                DataContract.MovieEntry._ID}
                },
                new String[]{
                        DataContract.FavoritesEntry.COLUMN_MOVIE_ID
                }
        );
        final String SQL_CREATE_REVIEWS_ENTRY = BuildSQLCreateTable(
                DataContract.ReviewsEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.ReviewsEntry._ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                        {DataContract.ReviewsEntry.COLUMN_MOVIE_ID, "INTEGER NOT NULL"},
                        {DataContract.ReviewsEntry.COLUMN_AUTHOR, "TEXT NOT NULL"},
                        {DataContract.ReviewsEntry.COLUMN_CONTENT, "TEXT NOT NULL"},
                },
                new String[][]{
                        {DataContract.ReviewsEntry.COLUMN_MOVIE_ID,
                                DataContract.MovieEntry.TABLE_NAME,
                                DataContract.MovieEntry._ID}
                },
                null
        );
        final String SQL_CREATE_VIDEOS_ENTRY = BuildSQLCreateTable(
                DataContract.VideosEntry.TABLE_NAME,
                new String[][]{
                        {DataContract.VideosEntry._ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                        {DataContract.VideosEntry.COLUMN_MOVIE_ID, "INTEGER NOT NULL"},
                        {DataContract.VideosEntry.COLUMN_KEY, "TEXT NOT NULL"},
                        {DataContract.VideosEntry.COLUMN_NAME, "TEXT NOT NULL"}
                },
                new String[][]{
                        {DataContract.VideosEntry.COLUMN_MOVIE_ID,
                                DataContract.MovieEntry.TABLE_NAME,
                                DataContract.MovieEntry._ID}
                },
                null
        );
        // Run SQL commands
        db.execSQL(SQL_CREATE_GENRE_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_MOST_POPULAR_ENTRY);
        db.execSQL(SQL_CREATE_TOP_RATED_ENTRY);
        db.execSQL(SQL_CREATE_FAVORITE_ENTRY);
        db.execSQL(SQL_CREATE_REVIEWS_ENTRY);
        db.execSQL(SQL_CREATE_VIDEOS_ENTRY);
    }

    // Helper function to build SQL "CREATE" command
    private String BuildSQLCreateTable(String name, String[][] columns, String[][] foreignKeys, String[] uniqueColumns) {
        // Create Table
        String sql = "CREATE TABLE " + name + "(";
        // Add Columns
        for (String[] column : columns) {
            sql = sql.concat(column[0]);
            sql = sql.concat(" ");
            sql = sql.concat(column[1]);
            sql = sql.concat(", ");
        }
        // Add Foreign Keys
        if (foreignKeys != null) {
            for (String[] foreign : foreignKeys) {
                sql = sql.concat(" FOREIGN KEY (");
                sql = sql.concat(foreign[0]);
                sql = sql.concat(") REFERENCES ");
                sql = sql.concat(foreign[1]);
                sql = sql.concat(" (");
                sql = sql.concat(foreign[2]);
                sql = sql.concat("), ");
            }
        }
        // Add Unique Keys
        if (uniqueColumns != null) {
            sql = sql.concat(" UNIQUE (");
            for (String unique : uniqueColumns) {
                sql = sql.concat(unique);
                sql = sql.concat(", ");
            }
            sql = sql.substring(0, sql.length() - 2);
            sql = sql.concat(") ON CONFLICT REPLACE, ");
        }
        // Finally
        sql = sql.substring(0, sql.length() - 2);
        sql = sql.concat(");");
        return sql;
    }

    // Upgrade database when version is changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
