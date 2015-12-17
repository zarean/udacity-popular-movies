package com.zarean.ali.popularmovies.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.zarean.ali.popularmovies.R;
import com.zarean.ali.popularmovies.adapter.ViewPagerAdapter;
import com.zarean.ali.popularmovies.data.DataContract;

/**
 * Created by Ali Zarean on 12/9/2015.
 */
public class TabFragment extends Fragment{

    private final static String SELECTED_KEY_MOST_POPULAR = "sk1";
    private final static String SELECTED_KEY_TOP_RATED = "sk2";
    private final static String SELECTED_KEY_FAVORITE = "sk3";

    private GridFragment mFragmentMostPopular;
    private GridFragment mFragmentTopRated;
    private GridFragment mFragmentFavorite;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);

        // Find ViewPager and TabLayout
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        // Only keep active fragment alive
        // Doesn't work for 0 :(
        // viewPager.setOffscreenPageLimit(0);

        // create Adapter
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getFragmentManager());
        // add Fragments
        int positionMostPopular = GridView.INVALID_POSITION;
        int positionTopRated = GridView.INVALID_POSITION;
        int positionFavorite = GridView.INVALID_POSITION;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELECTED_KEY_MOST_POPULAR))
                positionMostPopular = savedInstanceState.getInt(SELECTED_KEY_MOST_POPULAR);
            if (savedInstanceState.containsKey(SELECTED_KEY_TOP_RATED))
                positionMostPopular = savedInstanceState.getInt(SELECTED_KEY_TOP_RATED);
            if (savedInstanceState.containsKey(SELECTED_KEY_FAVORITE))
                positionMostPopular = savedInstanceState.getInt(SELECTED_KEY_FAVORITE);
        }
        mFragmentMostPopular = (GridFragment) createGridFragment(DataContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(DataContract.MostPopularEntry.TABLE_NAME).build(),
                positionMostPopular,
                getString(R.string.tab_popular));
        viewPagerAdapter.addFragment(mFragmentMostPopular, getString(R.string.tab_popular));
        mFragmentTopRated = (GridFragment) createGridFragment(DataContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(DataContract.TopRatedEntry.TABLE_NAME).build(),
                positionTopRated,
                getString(R.string.tab_toprated));
        viewPagerAdapter.addFragment(mFragmentTopRated, getString(R.string.tab_toprated));
        mFragmentFavorite = (GridFragment) createGridFragment(DataContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(DataContract.FavoritesEntry.TABLE_NAME).build(),
                positionFavorite,
                getString(R.string.tab_favorite));
        viewPagerAdapter.addFragment(mFragmentFavorite, getString(R.string.tab_favorite));
        // set Adapter
        viewPager.setAdapter(viewPagerAdapter);
        // setup TabLayout
        tabLayout.setupWithViewPager(viewPager);
        // set icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_star);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_like);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_heart);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_KEY_MOST_POPULAR, mFragmentMostPopular.getPosition());
        outState.putInt(SELECTED_KEY_TOP_RATED, mFragmentTopRated.getPosition());
        outState.putInt(SELECTED_KEY_FAVORITE, mFragmentFavorite.getPosition());
        super.onSaveInstanceState(outState);
    }

    private Fragment createGridFragment(Uri uri, int position, String name) {
        Fragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putParcelable(GridFragment.ARGS_URI_KEY, uri);
        args.putInt(GridFragment.ARGS_SELECTED_KEY, position);
        args.putString(GridFragment.ARGS_NAME_KEY, name);
        fragment.setArguments(args);
        return fragment;
    }
}
