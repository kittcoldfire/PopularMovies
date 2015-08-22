package phil.nanodegree.com.popularmovies;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
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
import android.widget.Toast;

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
import phil.nanodegree.com.popularmovies.adapters.GridMoviePosterCursorAdapter;
import phil.nanodegree.com.popularmovies.data.MovieContract;
import phil.nanodegree.com.popularmovies.models.Movie;
import phil.nanodegree.com.popularmovies.utilities.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<Movie> mMovies;    // GridView items list
    private GridMoviePosterAdapter mAdapter;    // GridView adapter
    private GridMoviePosterCursorAdapter mCursorAdapter;
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
    private boolean isConnected;

    private static final int MOVIES_LOADER = 0;

    public Fragment fragment = this;

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_GENRES,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_SORT,
            MovieContract.MovieEntry.COLUMN_DATE_ADDED
    };

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the root view of the fragment
        View fragmentView = inflater.inflate(R.layout.fragment_main, container, false);

        // initialize the GridView
        mGridView = (GridView) fragmentView.findViewById(R.id.gridview);

        mCursorAdapter = new GridMoviePosterCursorAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mCursorAdapter);

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                //Movie m = (Movie) mCursorAdapter.getItem(position);
                Cursor mCursor = mCursorAdapter.getCursor();
                if(mCursor.moveToPosition(position)) {
                    int movieId = mCursor.getInt(mCursor.getColumnIndex(MovieContract.MovieEntry._ID));
                    if(!isConnected) {
                        String backdrop = mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH));
                        if(backdrop == null || backdrop.equals("") || backdrop.equals("null")) {
                            Toast.makeText(getActivity(), "Content not downloaded for this title, please reconnect internet connection to view details!", Toast.LENGTH_LONG).show();
                        } else {
                            Bundle args = new Bundle();
                            args.putInt("movieId", movieId);

                            MovieDetailFragment mdf = new MovieDetailFragment();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.addToBackStack(null);

                            mdf.setArguments(args);
                            transaction.replace(R.id.content_frame, mdf);

                            transaction.commit();
                        }
                    } else {
                        Bundle args = new Bundle();
                        args.putInt("movieId", movieId);

                        MovieDetailFragment mdf = new MovieDetailFragment();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.addToBackStack(null);

                        mdf.setArguments(args);
                        transaction.replace(R.id.content_frame, mdf);

                        transaction.commit();
                    }
                }
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

//        if(mAdapter != null && !mAdapter.isEmpty()) {
//            // Binds the Adapter to the ListView
//            mGridView.setAdapter(mAdapter);
//        }

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected) {
            Toast.makeText(getActivity(), "No internet connection available, running in offline mode!", Toast.LENGTH_LONG).show();
        }
        mCursorAdapter.setOfflineMode(isConnected);

        return fragmentView;
    }

    //Put in onCreate because if the device is rotated twice while this is in the BackStack onCreateView isn't called for some reason
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefSort = Utils.getSortSection(getActivity());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        searchParam = sharedPref.getString(PREF_SEARCH, mPrefSearchPopular);

        //Handling rotation of device, we retrieve our proper state from the savedInstanceState
        if(savedInstanceState != null && savedInstanceState.containsKey("movies")) {
            mMovies = savedInstanceState.getParcelableArrayList("movies");
            mCursorAdapter = new GridMoviePosterCursorAdapter(getActivity(), null, 0);
            //mAdapter = new GridMoviePosterAdapter(getActivity(), mMovies);
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
            mCursorAdapter = new GridMoviePosterCursorAdapter(getActivity(), null, 0);
            //mAdapter = new GridMoviePosterAdapter(getActivity(), mMovies);
        } else {
            if(mRestored == false) {
                if(Utils.checkIfSectionDataIsCurrent(getActivity())) {

                } else {
                    MovieAsyncTask task = new MovieAsyncTask();
                    task.execute();
                }
            }
        }

        mRestored = false;
        savedState = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
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
        if(mPrefSort == 1) {
            mActionBar.setTitle(R.string.most_popular);
        } else if (mPrefSort == 5) { //highest rated if selected
            mActionBar.setTitle(R.string.highest_rated);
        } else if(mPrefSort == 11) { //highest revenue if selected
            mActionBar.setTitle(R.string.highest_revenue);
        } else if(mPrefSort == 20) {
            mActionBar.setTitle(R.string.favorites);
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

        mPrefSort = Utils.getSortSection(getActivity());

        MenuItem sort_hr = menu.findItem(R.id.action_sort_highest_rated);
        MenuItem sort_p = menu.findItem(R.id.action_sort_popular);
        MenuItem sort_rev = menu.findItem(R.id.action_sort_highest_revenue);
        MenuItem sort_fav = menu.findItem(R.id.action_sort_favorite);

        //Popular by default, so only show highest rated
        if(mPrefSort == 1) {
            sort_p.setVisible(false);
            sort_rev.setVisible(true);
            sort_hr.setVisible(true);
            sort_fav.setVisible(true);
        } else if (mPrefSort == 5) { //highest rated if selected
            sort_p.setVisible(true);
            sort_rev.setVisible(true);
            sort_hr.setVisible(false);
            sort_fav.setVisible(true);
        } else if (mPrefSort == 11) { //highest revenue if selected
            sort_p.setVisible(true);
            sort_hr.setVisible(true);
            sort_rev.setVisible(false);
            sort_fav.setVisible(true);
        } else if(mPrefSort == 20) { //favorite
            sort_p.setVisible(true);
            sort_hr.setVisible(true);
            sort_rev.setVisible(true);
            sort_fav.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Utils.setSortSection(getActivity(), id);

        if (id == R.id.action_sort_popular) {
            searchParam = mPrefSearchPopular;
            mPrefSort = 1;
            if(Utils.checkIfSectionDataIsCurrent(getActivity())) {
                onSortChanged();
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
            return true;
        } else if(id == R.id.action_sort_highest_rated) {
            searchParam = mPrefSearchHRated;
            mPrefSort = 5;
            if(Utils.checkIfSectionDataIsCurrent(getActivity())) {
                onSortChanged();
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
            return true;
        } else if(id == R.id.action_sort_highest_revenue) {
            searchParam = mPrefSearchHRevenue;
            mPrefSort = 11;
            if(Utils.checkIfSectionDataIsCurrent(getActivity())) {
                onSortChanged();
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
            return true;
        } else if(id == R.id.action_sort_favorite) {
            searchParam = mPrefSearchHRevenue;
            mPrefSort = 20;
            onSortChanged();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    // since we read the sort section when we create the loader, all we need to do is restart things
    void onSortChanged( ) {
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if(mPrefSort == 1) {
            mActionBar.setTitle(R.string.most_popular);
        } else if (mPrefSort == 5) { //highest rated if selected
            mActionBar.setTitle(R.string.highest_rated);
        } else if(mPrefSort == 11) { //highest revenue if selected
            mActionBar.setTitle(R.string.highest_revenue);
        } else if(mPrefSort == 20) {
            mActionBar.setTitle(R.string.favorites);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        String sortOrder = null;

        int sortSection = Utils.getSortSection(getActivity());
        Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(sortSection);

        return new CursorLoader(getActivity(),
                moviesSortedUri,
                MOVIES_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
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
            //mAdapter = new GridMoviePosterAdapter(getActivity(), movies);
            mMovies = movies;
            // Binds the Adapter to the ListView
            //mGridView.setAdapter(mAdapter);

            ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

            //Popular by default, so only show highest rated
            if(mPrefSort == 1) {
                mActionBar.setTitle(R.string.most_popular);
            } else if (mPrefSort == 5) { //highest rated if selected
                mActionBar.setTitle(R.string.highest_rated);
            } else if (mPrefSort == 11) { //highest revenue if selected
                mActionBar.setTitle(R.string.highest_revenue);
            }

            mProgressDialog.dismiss();
            onSortChanged();
        }

        private ArrayList<Movie> parseJson(String stream) {

            String stringFromStream = stream;
            ArrayList<Movie> results = new ArrayList<Movie>();
            try {
                JSONObject jsonObject = new JSONObject(stringFromStream);
                JSONArray array = (JSONArray) jsonObject.get("results");

                //Save only our favorite movies, than delete the rest from this section
                Utils.cleanUpOldData(getActivity());

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
                    StringBuilder strGenre = new StringBuilder();
                    ArrayList<Integer> genre_ids = new ArrayList<Integer>();
                    for (int x = 0; x < genres.length(); x++) {
                        genre_ids.add(genres.getInt(x));
                        strGenre.append(genres.getInt(x) + "");
                        if(x < (genres.length() - 1)) {
                            strGenre.append(",");
                        }
                    }
                    movie.setGenres(strGenre.toString());
                    results.add(movie);
                    Log.d(LOG_TAG, "Added movie: " + movie.getTitle());

                    //Check if the movie still exists in the db, if it does update the section, otherwise add to db
                    Cursor curMov = getActivity().getContentResolver().query(MovieContract.MovieEntry.buildMovieUri(0, movie.getId()), null, null, null, null);
                    if(curMov.moveToFirst()) {
                        Time dayTime = new Time();
                        dayTime.setToNow();

                        // we start at the day returned by local time. Otherwise this is a mess.
                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                        // now we work exclusively in UTC
                        dayTime = new Time();

                        ContentValues cv = new ContentValues();

                        double sort = curMov.getDouble(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_SORT));
                        int addSort = Utils.getSortSection(getActivity());
                        sort += addSort;

                        cv.put(MovieContract.MovieEntry.COLUMN_SORT, sort);
                        cv.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));
                        getActivity().getContentResolver().update(MovieContract.MovieEntry.buildMovieUri(0, movie.getId()), cv, MovieContract.MovieEntry._ID + " = ?", new String[] { movie.getId() + ""});
                    } else {
                        Time dayTime = new Time();
                        dayTime.setToNow();

                        // we start at the day returned by local time. Otherwise this is a mess.
                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                        // now we work exclusively in UTC
                        dayTime = new Time();

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.MovieEntry._ID, movie.getId());
                        cv.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                        cv.put(MovieContract.MovieEntry.COLUMN_GENRES, strGenre.toString());
                        cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                        cv.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                        cv.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                        cv.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                        cv.put(MovieContract.MovieEntry.COLUMN_SORT, Utils.getSortSection(getActivity()));
                        cv.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));

                        getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,cv);
                    }
                }
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(LOG_TAG, "Error parsing JSON. String was: " + stringFromStream);
            }
            return results;
        }
    }
}
