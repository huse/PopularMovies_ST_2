package com.hpr.hus.popularmovies;

/**
 * Created by hk640d on 8/1/2017.
 */

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.hpr.hus.popularmovies.MovieAdapter.MovieAdapterOnClickHandler;
import com.hpr.hus.popularmovies.settings_general.SettingsActivity;


public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private RecyclerView rvList;
    MovieAdapter movieAdapter;

    MovieAdapterOnClickHandler clickHandler;


    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("hhhh", "MainActivity_onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reserveInitialingViews(savedInstanceState);
        rvList = (RecyclerView) findViewById(R.id.recyclerview_movies_list);


        MovieSelected[] movies = new MovieSelected[0];
        clickHandler = this;
        rvList.setAdapter(new MovieAdapter(movies,getApplicationContext(),this));
    }

    private void reserveInitialingViews(@Nullable Bundle savedInstanceState){
        rvList = (RecyclerView) findViewById(R.id.recyclerview_movies_list);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvList.setLayoutManager(layoutManager);


        rvList.setVisibility(View.VISIBLE);


        if (savedInstanceState == null) {
            Log.v("hhhh", "MainActivity_onCreate_savedInstanceState == null");

            getMoviesFromTMDb(getSortMethod());
        } else {

            Parcelable[] parcelable = savedInstanceState.
                    getParcelableArray(getString(R.string.parcel_movie));
            Log.v("hhhh", "MainActivity_onCreate_parcelable");
            if (parcelable != null) {
                int numMovieObjects = parcelable.length;
                MovieSelected[] movies = new MovieSelected[numMovieObjects];
                for (int i = 0; i < numMovieObjects; i++) {
                    movies[i] = (MovieSelected) parcelable[i];
                }
                Log.v("hhhh", "MainActivity_onCreate_rvList.setAdapter");

            }
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);


        mMenu = menu;


        mMenu.add(Menu.NONE,
                R.id.sort_popularity,
                Menu.NONE,
                null)
                .setVisible(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mMenu.add(Menu.NONE, R.id.sort_top_rate, Menu.NONE, null)
                .setVisible(false)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        updateMenu();

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sort_popularity:
                updateSharedPrefs(getString(R.string.tmdb_sort_pop_desc));

                getMoviesFromTMDb(getSortMethod());
                Log.v("kkkkkk","pref_sort_pop_desc_key");
                mMenu.findItem(R.id.sort_popularity).setVisible(false);
                mMenu.findItem(R.id.sort_top_rate).setVisible(true);
                updateMenu();
                return true;
            case R.id.sort_top_rate:
                updateSharedPrefs(getString(R.string.tmdb_sort_vote_avg_desc));

                getMoviesFromTMDb(getSortMethod());
                mMenu.findItem(R.id.sort_popularity).setVisible(true);
                mMenu.findItem(R.id.sort_top_rate).setVisible(false);
                Log.v("kkkkkk","pref_sort_vote_avg_desc_key");
                updateMenu();
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
        }

        return super.onOptionsItemSelected(item);
    }


    private final GridView.OnItemClickListener moviePosterClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MovieSelected movie = (MovieSelected) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
            intent.putExtra(getResources().getString(R.string.parcel_movie), movie);

            startActivity(intent);
        }
    };



    private void getMoviesFromTMDb(String sortMethod) {
        if (isNetworkAvailable()) {
            Log.v("hhhh2","NetworkAvailable");
            String apiKey = getString(R.string.key_themoviedb);

            rvList.setLayoutManager(new GridLayoutManager(this, numberOfColumns()));

            rvList.setHasFixedSize(true);
            rvList.setVisibility(View.VISIBLE);

            TaskInterfaceCompleted taskCompleted = new TaskInterfaceCompleted() {
                @Override
                public void onFetchMoviesTaskCompleted(MovieSelected[] movies) {
                    String movieName="";
                    if (movies!=null && movies.length!=0) {
                        for (MovieSelected ms : movies) {
                            movieName = movieName + " _ " + ms.getOriginalTitle();
                        }
                        Log.v("hhhh2_this", this.getClass().toString());
                        Log.v("hhhh2_getApplic", getApplicationContext().getClass().toString());

                        movieAdapter = new MovieAdapter(movies, getApplicationContext(), clickHandler);

                        rvList.setAdapter(movieAdapter);
                        Log.v("hhhh2", movieName);
                    }
                }
            };


            AsyncTaskFetchPopularMovies movieTask = new AsyncTaskFetchPopularMovies(taskCompleted, apiKey);
            movieTask.execute(sortMethod);
        } else {
            Log.v("gggg","NOT-----------NetworkAvailable");
            Toast.makeText(this, getString(R.string.error_need_internet), Toast.LENGTH_LONG).show();
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void updateMenu() {
        String sortMethod = getSortMethod();

        if (sortMethod.equals(getString(R.string.tmdb_sort_pop_desc))) {
            Log.v("hhhh2","tmdb_sort_pop_desc");

            mMenu.findItem(R.id.sort_popularity).setVisible(false);
            mMenu.findItem(R.id.sort_top_rate).setVisible(true);

        } else {
            Log.v("hhhh2","pref_sort_vote_avg_desc_key");

            mMenu.findItem(R.id.sort_popularity).setVisible(true);
            mMenu.findItem(R.id.sort_top_rate).setVisible(false);
        }
    }


    private String getSortMethod() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String result = prefs.getString(getString(R.string.pref_sort_method_key),
                getString(R.string.tmdb_sort_pop_desc));


        Log.v("getSortMethod", result);
        return result;
    }


    private void updateSharedPrefs(String sortMethod) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_sort_method_key), sortMethod);
        editor.apply();
    }
    @Override
    public void onClick(MovieSelected movie) {

        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
        intent.putExtra(getResources().getString(R.string.parcel_movie), movie);

        startActivity(intent);

    }
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }
}
