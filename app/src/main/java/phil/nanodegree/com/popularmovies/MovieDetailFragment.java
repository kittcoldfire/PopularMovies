package phil.nanodegree.com.popularmovies;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

import phil.nanodegree.com.popularmovies.models.Movie;
import phil.nanodegree.com.popularmovies.utilities.Utils;

public class MovieDetailFragment extends Fragment {

    private ArrayList<Movie> mMovies;
    public Movie mMovie;
    public int movieId;

    public TextView txtTitle, txtTagline, txtReleaseDate, txtRunTime, txtGenre, txtRating, txtOverview;
    public RatingBar ratingBar;
    public ImageView imgPoster, imgBackdrop;
    public ActionBar mActionBar;

    public LinearLayout linGenre, linDate, linLength;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the root view of the fragment
        View fragmentView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if(getArguments() != null) {
            movieId = getArguments().getInt("movieId");
        }

        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.show();

        txtTitle = (TextView) fragmentView.findViewById(R.id.detail_txt_title);
        txtTagline = (TextView) fragmentView.findViewById(R.id.detail_txt_tagline);
        txtGenre = (TextView) fragmentView.findViewById(R.id.detail_txt_genre);
        txtRunTime = (TextView) fragmentView.findViewById(R.id.detail_txt_running_time);
        txtReleaseDate = (TextView) fragmentView.findViewById(R.id.detail_txt_release_date);
        txtRating = (TextView) fragmentView.findViewById(R.id.detail_txt_rating);
        txtOverview = (TextView) fragmentView.findViewById(R.id.detail_txt_overview);
        imgPoster = (ImageView) fragmentView.findViewById(R.id.detail_img_poster);
        imgBackdrop = (ImageView) fragmentView.findViewById(R.id.detail_img_backdrop);
        ratingBar = (RatingBar) fragmentView.findViewById(R.id.detail_rat_rating);
        linDate = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_date);
        linGenre = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_genre);
        linLength = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_duration);

        //Has the option menu so it listens for the up caret press
        setHasOptionsMenu(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("movie", mMovie);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null || !savedInstanceState.containsKey("movie")) {
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute();
        } else {
            mMovie = savedInstanceState.getParcelable("movie");
            if(mMovie != null) {
                Utils utils = new Utils();
                txtTitle.setText(mMovie.getTitle());
                if(mMovie.getGenres().isEmpty()) {
                    linGenre.setVisibility(View.GONE);
                } else {
                    txtGenre.setText(utils.getGenres(mMovie.getGenres()));
                }
                if(mMovie.getRunTime() == 0) {
                    linLength.setVisibility(View.GONE);
                } else {
                    txtRunTime.setText(mMovie.getRunTime() + " min");
                }
                if(mMovie.getReleaseDate().equals("") || mMovie.getReleaseDate() == null) {
                    linDate.setVisibility(View.GONE);
                } else {
                    txtReleaseDate.setText(mMovie.getReleaseDate());
                }
                Picasso.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getPosterPath(), 3)).into(imgPoster);
                if(mMovie.getBackdrop().equals("") || mMovie.getBackdrop() == null || mMovie.getBackdrop().equals("null")) {
                    imgBackdrop.setVisibility(View.GONE);
                    txtTagline.setVisibility(View.GONE);
                } else {
                    Picasso.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getBackdrop(), 4)).into(imgBackdrop);
                    if(!mMovie.getTagline().equals("")) {
                        txtTagline.setText("\"" + mMovie.getTagline() + "\"");
                    }
                }
                txtRating.setText(mMovie.getVoteAverage() + " / 10");
                ratingBar.setRating((float) mMovie.getVoteAverage());
                txtOverview.setText(mMovie.getOverview());
                mActionBar.setTitle(mMovie.getTitle());
            } else {
                MovieAsyncTask task = new MovieAsyncTask();
                task.execute();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get item selected and deal with it
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up caret in actionbar is pressed
                getActivity().onBackPressed();
                return true;
        }

        return false;
    }

    public class MovieAsyncTask extends AsyncTask<Void, Void, Movie> {

        private final String API_KEY = "68c3c2327abf90fbb62940c05520d2da";
        private final String LOG_TAG = MainActivity.class.getSimpleName();
        private ProgressDialog mProgressDialog;
        public Movie movieObj;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(getActivity());
            // Set progressdialog title
            mProgressDialog.setTitle("Popular Movies");
            // Set progressdialog message
            mProgressDialog.setMessage("Retrieving Movie Info...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Movie doInBackground(Void... params) {
            //Build our URL to make our request
            //http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=[YOUR API KEY]
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(movieId + "")
                    .appendQueryParameter("api_key", API_KEY);

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

                movieObj = parseJson(json);
                return(movieObj);

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
        protected void onPostExecute(Movie movie) {
            super.onPostExecute(movie);

            mMovie = movie;

            Utils utils = new Utils();
            if(movie != null) {
                txtTitle.setText(movie.getTitle());
                if(movie.getGenres().isEmpty()) {
                    linGenre.setVisibility(View.GONE);
                } else {
                    txtGenre.setText(utils.getGenres(movie.getGenres()));
                }
                if(movie.getRunTime() == 0) {
                    linLength.setVisibility(View.GONE);
                } else {
                    txtRunTime.setText(movie.getRunTime() + " min");
                }
                if(movie.getReleaseDate().equals("") || movie.getReleaseDate() == null) {
                    linDate.setVisibility(View.GONE);
                } else {
                    txtReleaseDate.setText(movie.getReleaseDate());
                }
                Picasso.with(getActivity()).load(utils.constructMoviePosterURL(movie.getPosterPath(), 3)).into(imgPoster);
                if(movie.getBackdrop().equals("") || movie.getBackdrop() == null || movie.getBackdrop().equals("null")) {
                    imgBackdrop.setVisibility(View.GONE);
                    txtTagline.setVisibility(View.GONE);
                } else {
                    Picasso.with(getActivity()).load(utils.constructMoviePosterURL(movie.getBackdrop(), 4)).into(imgBackdrop);
                    if(!movie.getTagline().equals("")) {
                        txtTagline.setText("\"" + movie.getTagline() + "\"");
                    }
                }
                txtRating.setText(movie.getVoteAverage() + " / 10");
                ratingBar.setRating((float) movie.getVoteAverage());
                txtOverview.setText(movie.getOverview());
                mActionBar.setTitle(movie.getTitle());
            }

            mProgressDialog.dismiss();
        }

        private Movie parseJson(String stream) {

            String stringFromStream = stream;

            try {
                JSONObject jsonMovieObject = new JSONObject(stringFromStream);

                Movie movie = new Movie(
                        Integer.parseInt(jsonMovieObject.getString("id")),              //id
                        jsonMovieObject.getString("title"),                             //title
                        jsonMovieObject.getString("release_date"),                      //release_date
                        jsonMovieObject.getString("overview"),                          //overview
                        jsonMovieObject.getString("poster_path"),                       //poster_path
                        Double.parseDouble(jsonMovieObject.getString("vote_average"))   //vote_average
                );

                JSONArray genres = (JSONArray) jsonMovieObject.get("genres");
                ArrayList<Integer> genre_ids = new ArrayList<Integer>();
                for (int x = 0; x < genres.length(); x++) {
                    JSONObject jsonGenreObject = genres.getJSONObject(x);
                    genre_ids.add(Integer.parseInt(jsonGenreObject.getString("id")));
                }
                movie.setGenres(genre_ids);

                movie.setBackdrop(jsonMovieObject.getString("backdrop_path"));
                movie.setTagline(jsonMovieObject.getString("tagline"));
                String runtime = jsonMovieObject.getString("runtime");
                if(!runtime.equals("null") && !runtime.equals("") && runtime != null) {
                    movie.setRunTime(Integer.parseInt(runtime));
                }

                return movie;
            } catch (JSONException e) {
                System.err.println(e);
                Log.d(LOG_TAG, "Error parsing JSON. String was: " + stringFromStream);
            }

            return null;
        }
    }
}
