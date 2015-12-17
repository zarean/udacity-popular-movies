package com.zarean.ali.popularmovies.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.zarean.ali.popularmovies.Application.Global;
import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.fragment.DetailFragment;
import com.zarean.ali.popularmovies.fragment.GridFragment;

public class MainActivity extends AppCompatActivity implements GridFragment.Callback {

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Global.logMemory();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.detail_container) == null) {
            mTwoPane = false;
        } else {
            mTwoPane = true;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onItemSelected(Long id) {
        if (mTwoPane) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle args = new Bundle();
            args.putLong(DetailFragment.ARGS_ID_KEY, id);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.ARGS_ID_KEY, id);
            startActivity(intent);
        }
    }
}
