package com.example.android.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Fragment containing a GridView of movie options.
 */
public class MainActivityFragment extends Fragment {

    private ImageAdapter mMovieAdapter;
    private String[] thumbs = {
            "http://image.tmdb.org/t/p/w185/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg",
            "http://image.tmdb.org/t/p/w185/inVq3FRqcYIRl2la8iZikYYxFNR.jpg"
    };
    private ArrayList<String> mThumbs = new ArrayList<String>(Arrays.asList(thumbs));
    private Movies mMovies;
// URL Snippets from TMDb.org Discover API Examples to be used in Project 1
    private static final String URL_PREFIX = "http://api.themoviedb.org/3";
//    private static final String TMDB_API_KEY = "Insert Your API Key Here";
    private static final String TMDB_API_KEY = "-";
    private static final String URL_MOST_POPULAR = "/discover/movie?sort_by=popularity.desc";
    private static final String URL_HIGHEST_RATED =
            "/discover/movie/?certification_country=US&certification=R&sort_by=vote_average.desc";
    private static final String IMAGE_URL_PREFIX = "http://image.tmdb.org/t/p/";
    private static final String THUMBNAIL_SIZE = "w185";

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new ImageAdapter(getActivity());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movie);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
              //  String movie = mMovieAdapter.getItem(position);
                Movie movie = mMovies.getMovie(position);
                Intent intent = new Intent(getActivity(),DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,movie.toString());
                intent.putExtra(Intent.EXTRA_STREAM,movie.getPosterUri());
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMovies(){
        FetchMovieTask movieTask = new FetchMovieTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order =prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));
        if(order.equals("popular")){
            movieTask.execute(URL_MOST_POPULAR);
        }
        else {
            movieTask.execute(URL_HIGHEST_RATED);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }
    /**
 * ImageAdapter from Grid View API on android.com - Used for testing
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private final String LOG_TAG = ImageAdapter.class.getSimpleName();
    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbs.size();
    }

    public String getItem(int position) {
        return mThumbs.get(position);
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
        String imageId = getItem(position);
        Picasso.with(mContext).load(imageId).into(view);
        return view;
    }
}
/* Container class for individual movie objects with fields having been
 * extracted from JSON objects retrieved from THDb
 */

    public class Movie {
        public String title;        // original_title in JSON
        public String thumbnail;    // poster_path
        public String overview;     // overview
        public double rating;          // vote_average
        public String release;      // release_date

        public Movie (String title, String thumbnail,String overview,double rating,String release){
            this.title = title;
            this.thumbnail = thumbnail;
            this.overview = overview;
            this.rating = rating;
            this.release = release;
        }
        // Build the EXTRA.TEXT to be sent to detail activity
        @Override public String toString(){
            StringBuilder str = new StringBuilder();
            String NEW_LINE = System.getProperty("line.separator");
            str.append("Title: " + title + NEW_LINE);
            str.append("Plot: " + overview + NEW_LINE);
            str.append("User Rating: " + rating + NEW_LINE);
            str.append("Release Date: " + release + NEW_LINE);
            return str.toString();
        }
        public String getPosterUri(){
            return IMAGE_URL_PREFIX + THUMBNAIL_SIZE + thumbnail;
        }
    }
/* Collection of Movie objects selected for user review.
 * Methods added to facilitate specific activity needs
 */
    public class Movies {
        private List<Movie> mMovies;
// Constructor takes string list of movie data in JSON format, extracts individual movie data
// and desired fields, creates Movie objects and adds them to the Movies List object.
        public Movies (String movieBatchJson) throws JSONException{
            mMovies = new ArrayList<Movie>();
            final String TMDB_RESULTS = "results";
            final String TMDB_THUMBNAIL = "poster_path";
            final String TMDB_TITLE = "original_title";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_RATING = "vote_average";
            final String TMDB_RELEASE = "release_date";

            JSONObject moviesJson = new JSONObject(movieBatchJson);
            JSONArray movieArray = moviesJson.getJSONArray(TMDB_RESULTS);
            for(int i = 0; i < movieArray.length(); i++){
                String title;
                String thumbnail;
                String overview;
                Double rating;
                String release;
                Movie movie;

                JSONObject movieJson = movieArray.getJSONObject(i);
                title = movieJson.getString(TMDB_TITLE);
                thumbnail = movieJson.getString(TMDB_THUMBNAIL);
                overview = movieJson.getString(TMDB_OVERVIEW);
                rating = movieJson.getDouble(TMDB_RATING);
                release = movieJson.getString(TMDB_RELEASE);
                movie = new Movie(title,thumbnail,overview,rating,release);
                mMovies.add(movie);
            }
        }
        public String[] getAllThumbnails(){
            String [] thumbs = new String[mMovies.size()];
            for (int i = 0; i < mMovies.size(); i++) {
                thumbs[i] = IMAGE_URL_PREFIX + THUMBNAIL_SIZE + mMovies.get(i).thumbnail;
                Log.v("getAllThumbnails","Thumbnails Array: " + thumbs[i]);
            }
            return thumbs;
        }
        public Movie getMovie(int index){
            return mMovies.get(index);
        }

    }
    public class FetchMovieTask extends AsyncTask<String,Void,Movies> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        @Override
        protected Movies doInBackground(String... params) { //params is an Array of type String
            // Modelled after Sunshine v2 example
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJsonStr = null; // raw JSON response string
            String sortOrder= params[0];
            Movies movies;

            try {
                // Construct the query string for TMDb.org
                // Choose not to use URI builder at this point
                String apiKey = "&api_key=" + TMDB_API_KEY;  //need to create & ignore
                String urlStr = URL_PREFIX + sortOrder + apiKey;
                URL url = new URL(urlStr);
//                Log.v(LOG_TAG,"URI: " + urlStr);

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

            try {
                movies = new Movies(moviesJsonStr);
                return movies;
            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        return null;
        }

        @Override
        protected void onPostExecute(Movies result) throws NullPointerException {
            if (result != null) {
                mThumbs.clear();
                mMovieAdapter.notifyDataSetChanged();
                for (String movieUrl : result.getAllThumbnails()) {
                    mThumbs.add(movieUrl);
                }
                mMovies = result;
            }
        }
    }
}
