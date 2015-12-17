package com.zarean.ali.popularmovies.Application;

/**
 * Created by Ali Zarean on 12/10/2015.
 */
import android.app.Application;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.zarean.ali.popularmovies.utility.TheMovieDBApi;

public class Global extends Application {

    private static Picasso picasso;
    public static final String LOG_TAG = "zarean";

    @Override
    public void onCreate() {
        super.onCreate();

        // Load Movies
        TheMovieDBApi theMovieDBApi = new TheMovieDBApi(getApplicationContext());
        theMovieDBApi.refreshDatabase();

        // Fix Picasso Caching
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        //built.setIndicatorsEnabled(true);
        Picasso.setSingletonInstance(built);
        picasso = Picasso.with(this);
    }

    public static void loadImage(String uri, ImageView imageView){
        loadImage(uri, imageView, null);
    }

    public static void loadImage(String uri, ImageView imageView, Integer placeHolder){
        if (placeHolder==null){
            picasso.load(uri).into(imageView);
        }
        else{
            picasso.load(uri).placeholder(placeHolder).into(imageView);
        }

    }

    public static void logMemory(){
        long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024*1024);
        long maxMemory = (Runtime.getRuntime().maxMemory())/(1024*1024);
        Log.e(LOG_TAG, "[" + usedMemory + "/" + maxMemory + "]");
    }
}
