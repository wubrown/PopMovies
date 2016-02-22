package com.example.android.popmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A Fragment containing a GridView of movie options.
 */
public class MainActivityFragment extends Fragment {

// URL Snippets from TMDb.org Discover API Examples to be used in Project 1
    private static final String URL_PREFIX = "http://api.themoviedb.org/3";
    private static final String TMDB_API_KEY = "Insert Your API Key Here";
//    private static final String TMDB_API_KEY = "-";
    private static final String URL_MOST_POPULAR = "/discover/movie?sort_by=popularity.desc";
    private static final String URL_HIGHEST_RATED =
            "/discover/movie/?certification_country=US&certification=R&sort_by=vote_average.desc";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main_fragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            FetchMovieTask movieTask = new FetchMovieTask();
            movieTask.execute(URL_MOST_POPULAR);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(new ImageAdapter(getActivity()));
        return rootView;
    }

    /**
 * ImageAdapter from Grid View API on android.com - Used for testing
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter using Picasso
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = (ImageView) convertView;
        if (view == null) {
            // if it's not recycled, initialize some attributes
            view = new ImageView(mContext);

        }
        Object imageId = getItem(position);
        Picasso.with(mContext).load(mThumbIds[position]).into(view);
        return view;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };
}
    public class FetchMovieTask extends AsyncTask<String,Void,Void> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        @Override
        protected Void doInBackground(String... params){ //params is an Array of type String
            // Modelled after Sunshine v2 example
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null; // raw JSON response string
            String sortOrder= params[0];

            try {
                // Construct the query string for TMDb.org
                String apiKey = "&api_key=" + TMDB_API_KEY;  //need to create & ignore
                String urlStr = URL_PREFIX + sortOrder + apiKey;
                URL url = new URL(urlStr);

                // Create the request to TMDb and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read input string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) return null;

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) return null;

                moviesJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Movie JSON String: " + moviesJsonStr);
            }  catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;

        }
    }
}
