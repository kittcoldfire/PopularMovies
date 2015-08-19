package phil.nanodegree.com.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import phil.nanodegree.com.popularmovies.adapters.GridMoviePosterAdapter;
import phil.nanodegree.com.popularmovies.models.Movie;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayList<Movie> mMovies;    // GridView items list
    private GridMoviePosterAdapter mAdapter;    // GridView adapter
    private GridView mGridView;
    private final String PREF_SORT = "pref_sort"; //0 for popular, 1 highest rated, 2 highest revenue
    private final String PREF_SEARCH = "pref_search"; //0 for popular, 1 highest rated, 2 highest revenue
    private int mPrefSort;
    private String mPrefSearchPopular = "popularity.desc";
    private String mPrefSearchHRated = "vote_average.desc";
    private String mPrefSearchHRevenue = "revenue.desc";
    public String searchParam;
    private Bundle savedState = null;
    private boolean mRestored = false;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the root view of the fragment
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        // initialize the GridView
        mGridView = (GridView) fragmentView.findViewById(R.id.gridview);

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                Movie m = (Movie) mAdapter.getItem(position);
                int movieId = m.getId();

                Bundle args = new Bundle();
                args.putInt("movieId", movieId);

                MovieDetailFragment mdf = new MovieDetailFragment();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.addToBackStack(null);

                mdf.setArguments(args);
                transaction.replace(R.id.content_frame, mdf);

                transaction.commit();
            }
        });

        setHasOptionsMenu(true);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int mLastFirstVisibleItem = 0;
            ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (view.getId() == mGridView.getId()) {
                    final int currentFirstVisibleItem = mGridView.getFirstVisiblePosition();

                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        mActionBar.hide();
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        mActionBar.show();
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }
        });

        ActionBar mActionBar;
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(false);

        if(mAdapter != null && !mAdapter.isEmpty()) {
            // Binds the Adapter to the ListView
            mGridView.setAdapter(mAdapter);
        }

        return fragmentView;
    }

    //Put in onCreate because if the device is rotated twice while this is in the BackStack onCreateView isn't called for some reason
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        mPrefSort = sharedPref.getInt(PREF_SORT, 0);
        searchParam = sharedPref.getString(PREF_SEARCH, mPrefSearchPopular);

        //Handling rotation of device, we retrieve our proper state from the savedInstanceState
        if(savedInstanceState != null && savedInstanceState.containsKey("movies")) {
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            mAdapter = new GridMoviePosterAdapter(getActivity(), mMovies);
            //savedState = null;
            mRestored = true;
        }

        //When navigating back from the back stack, we need to get the info from the Bundle we included
        if(savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle("movies");
        }
        //Only download if we don't already have the data
        if(savedState != null) {
            mMovies = savedState.getParcelableArrayList("movies");
            mAdapter = new GridMoviePosterAdapter(getActivity(), mMovies);
        } else {
            if(mRestored == false) {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
        }

        mRestored = false;
        savedState = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveState();
    }

    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Bundle state = new Bundle();
        state.putParcelableArrayList("movies", mMovies);

        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mMovies != null && !mMovies.isEmpty()) {
            outState.putBundle("movies", (savedState != null) ? savedState : saveState());
            outState.putParcelableArrayList("movies", mMovies);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if(mPrefSort == 0) {
            mActionBar.setTitle(R.string.most_popular);
        } else if (mPrefSort == 1) { //highest rated if selected
            mActionBar.setTitle(R.string.highest_rated);
        } else if(mPrefSort == 2) { //highest revenue if selected
            mActionBar.setTitle(R.string.highest_revenue);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_sort, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        mPrefSort = sharedPref.getInt(PREF_SORT, 0);

        MenuItem sort_hr = menu.findItem(R.id.action_sort_highest_rated);
        MenuItem sort_p = menu.findItem(R.id.action_sort_popular);
        MenuItem sort_rev = menu.findItem(R.id.action_sort_highest_revenue);

        //Popular by default, so only show highest rated
        if(mPrefSort == 0) {
            sort_p.setVisible(false);
            sort_rev.setVisible(true);
            sort_hr.setVisible(true);
        } else if (mPrefSort == 1) { //highest rated if selected
            sort_p.setVisible(true);
            sort_rev.setVisible(true);
            sort_hr.setVisible(false);
        } else if (mPrefSort == 2) { //highest revenue if selected
            sort_p.setVisible(true);
            sort_hr.setVisible(true);
            sort_rev.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (id == R.id.action_sort_popular) {
            editor.putInt(PREF_SORT, 0);
            editor.putString(PREF_SEARCH, mPrefSearchPopular);
            searchParam = mPrefSearchPopular;
            editor.commit();
            mPrefSort = 0;
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute();
            return true;
        } else if(id == R.id.action_sort_highest_rated) {
            editor.putInt(PREF_SORT, 1);
            editor.putString(PREF_SEARCH, mPrefSearchHRated);
            searchParam = mPrefSearchHRated;
            editor.commit();
            mPrefSort = 1;
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute();
            return true;
        } else if(id == R.id.action_sort_highest_revenue) {
            editor.putInt(PREF_SORT, 2);
            editor.putString(PREF_SEARCH, mPrefSearchHRevenue);
            searchParam = mPrefSearchHRevenue;
            editor.commit();
            mPrefSort = 2;
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public class MovieAsyncTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

        private final String API_KEY = "68c3c2327abf90fbb62940c05520d2da";
        private final String LOG_TAG = MainActivity.class.getSimpleName();
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(getActivity());
            // Set progressdialog title
            mProgressDialog.setTitle("Popular Movies");
            // Set progressdialog message
            mProgressDialog.setMessage("Populating Movie List...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<Movie> doInBackground(Void... params) {
            //Build our URL to make our request
            //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
            Uri.Builder builder = new Uri.Builder();
            if(searchParam == mPrefSearchHRated) {
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", searchParam)
                        .appendQueryParameter("api_key", API_KEY)
                        .appendQueryParameter("vote_count.gte", "80");
            } else {
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", searchParam)
                        .appendQueryParameter("api_key", API_KEY);
            }


            InputStream stream = null;
            try {
                //Get our url to make our network request
                URL url = new URL(builder.build().toString());

                Log.d(LOG_TAG, "The URL is: " + url.toString());

                // Establish a connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json");
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(LOG_TAG, "The response code is: " + responseCode + " " + conn.getResponseMessage());

                stream = conn.getInputStream();

                //Read the stream into a String of JSON
                Reader reader = null;
                reader = new InputStreamReader(stream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(reader);
                String json = bufferedReader.readLine();

                //Return our list of movies to use in our GridAdapter
                return(parseJson(json));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException ie) {
                ie.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);

            // Pass the results into ListViewAdapter.java
            mAdapter = new GridMoviePosterAdapter(getActivity(), movies);
            mMovies = movies;
            // Binds the Adapter to the ListView
            mGridView.setAdapter(mAdapter);

            ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            //Popular by default, so only show highest rated
            if(mPrefSort == 0) {
                mActionBar.setTitle(R.string.most_popular);
            } else if (mPrefSort == 1) { //highest rated if selected
                mActionBar.setTitle(R.string.highest_rated);
            } else if (mPrefSort == 2) { //highest revenue if selected
                mActionBar.setTitle(R.string.highest_revenue);
            }

            mProgressDialog.dismiss();
        }

        private ArrayList<Movie> parseJson(String stream) {

            String stringFromStream = stream;
            ArrayList<Movie> results = new ArrayList<Movie>();
            try {
                JSONObject jsonObject = new JSONObject(stringFromStream);
                JSONArray array = (JSONArray) jsonObject.get("results");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonMovieObject = array.getJSONObject(i);
                    Movie movie = new Movie(
                            Integer.parseInt(jsonMovieObject.getString("id")),              //id
                            jsonMovieObject.getString("original_title"),                    //title
                            jsonMovieObject.getString("release_date"),                      //release_date
                            jsonMovieObject.getString("overview"),                          //overview
                            jsonMovieObject.getString("poster_path"),                       //poster_path
                            Double.parseDouble(jsonMovieObject.getString("vote_average"))   //vote_average
                    );

                    JSONArray genres = (JSONArray) jsonMovieObject.get("genre_ids");
                    ArrayList<Integer> genre_ids = new ArrayList<Integer>();
                    for (int x = 0; x < genres.length(); x++) {
                        genre_ids.add(genres.getInt(x));
                    }
                    movie.setGenres(genre_ids);
                    results.add(movie);
                    Log.d(LOG_TAG, "Added movie: " + movie.getTitle());
                }
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(LOG_TAG, "Error parsing JSON. String was: " + stringFromStream);
            }
            return results;
        }
    }
}
