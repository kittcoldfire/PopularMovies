package phil.nanodegree.com.popularmovies;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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

import phil.nanodegree.com.popularmovies.data.MovieContract;
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

    public LinearLayout linGenre, linDate, linLength, linCast, linTrail, linRev;

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
        linCast = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_cast);
        linTrail = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_trailers);
        linRev = (LinearLayout) fragmentView.findViewById(R.id.detail_lin_reviews);

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
                //Picasso.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getPosterPath(), 3)).into(imgPoster);
                Glide.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getPosterPath(), 3)).into(imgPoster);
                if(mMovie.getBackdrop().equals("") || mMovie.getBackdrop() == null || mMovie.getBackdrop().equals("null")) {
                    imgBackdrop.setVisibility(View.GONE);
                    txtTagline.setVisibility(View.GONE);
                } else {
                    //Picasso.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getBackdrop(), 4)).into(imgBackdrop);
                    Glide.with(getActivity()).load(utils.constructMoviePosterURL(mMovie.getBackdrop(), 4)).into(imgBackdrop);
                    if(!mMovie.getTagline().equals("")) {
                        txtTagline.setText("\"" + mMovie.getTagline() + "\"");
                    }
                }
                if(mMovie.getCast().isEmpty()) {

                } else {
                    ArrayList<String[]> cast = mMovie.getCast();
                    for (int x = 0; x < cast.size(); x++ ) {
                        View card = getActivity().getLayoutInflater().inflate(R.layout.castcard, null);

                        ImageView img = (ImageView) card.findViewById(R.id.castcard_img);
                        TextView txtName = (TextView) card.findViewById(R.id.castcard_txt_name);
                        TextView txtCharacter = (TextView) card.findViewById(R.id.castcard_txt_character);

                        txtName.setText(cast.get(x)[1]);
                        txtCharacter.setText(cast.get(x)[2]);
                        //ImageView imageView = new ImageView(getActivity());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(3, 0, 3, 0);
                        card.setLayoutParams(lp);
                        linCast.addView(card);
                        //Picasso.with(getActivity()).load(utils.constructMoviePosterURL(cast.get(x)[2], 3)).into(img);
                        String sil = cast.get(x)[3];
                        if(sil.isEmpty() || sil.equals("null")) {
                            Glide.with(getActivity()).load(R.drawable.silhouette).centerCrop().into(img);
                        } else {
                            Glide.with(getActivity()).load(utils.constructMoviePosterURL(cast.get(x)[3], 3)).centerCrop().into(img);
                        }

                    }
                }

                if(mMovie.getTrailers().isEmpty()) {

                } else {
                    final ArrayList<String[]> trailers = mMovie.getTrailers();
                    for(int p = 0; p < trailers.size(); p++) {
                        View card = getActivity().getLayoutInflater().inflate(R.layout.trailercard, null);

                        ImageView img = (ImageView) card.findViewById(R.id.trailercard_img);
                        TextView txtName = (TextView) card.findViewById(R.id.trailercard_txt_name);

                        txtName.setText(trailers.get(p)[0]);
                        //ImageView imageView = new ImageView(getActivity());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(10, 0, 10, 0);
                        card.setLayoutParams(lp);
                        linTrail.addView(card);
                        final String videoURL = trailers.get(p)[1];
                        String trailerPoster = "http://img.youtube.com/vi/" + videoURL + "/0.jpg";
                        //Picasso.with(getActivity()).load(trailerPoster).into(img);
                        Glide.with(getActivity()).load(trailerPoster).centerCrop().into(img);

                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoURL)));
                            }
                        });

                    }
                }
                if(mMovie.getReviews().isEmpty()) {

                } else {
                    final ArrayList<String[]> reviews = mMovie.getReviews();
                    for(int r = 0; r < reviews.size(); r++) {
                        View card = getActivity().getLayoutInflater().inflate(R.layout.reviewcard, null);

                        TextView txtAuthor = (TextView) card.findViewById(R.id.reviewcard_author);
                        TextView txtContent = (TextView) card.findViewById(R.id.reviewcard_content);

                        txtAuthor.setText(reviews.get(r)[0]);
                        txtContent.setText(reviews.get(r)[1]);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(10, 0, 10, 0);
                        card.setLayoutParams(lp);
                        linRev.addView(card);
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
                //add on the append_to_response to get credits, reviews and trailers all in one network request
                String completeUrl = builder.build().toString() + "&append_to_response=credits,reviews,trailers";
                URL url = new URL(completeUrl);

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
                //Picasso.with(getActivity()).load(utils.constructMoviePosterURL(movie.getPosterPath(), 3)).into(imgPoster);
                Glide.with(getActivity()).load(utils.constructMoviePosterURL(movie.getPosterPath(), 3)).into(imgPoster);
                if(movie.getBackdrop().equals("") || movie.getBackdrop() == null || movie.getBackdrop().equals("null")) {
                    imgBackdrop.setVisibility(View.GONE);
                    txtTagline.setVisibility(View.GONE);
                } else {
                    //Picasso.with(getActivity()).load(utils.constructMoviePosterURL(movie.getBackdrop(), 4)).into(imgBackdrop);
                    Glide.with(getActivity()).load(utils.constructMoviePosterURL(movie.getBackdrop(), 4)).into(imgBackdrop);
                    if(!movie.getTagline().equals("")) {
                        txtTagline.setText("\"" + movie.getTagline() + "\"");
                    }
                }

                if(movie.getCast().isEmpty()) {

                } else {
                    ArrayList<String[]> cast = movie.getCast();
                    for (int x = 0; x < cast.size(); x++ ) {
                        View card = getActivity().getLayoutInflater().inflate(R.layout.castcard, null);

                        ImageView img = (ImageView) card.findViewById(R.id.castcard_img);
                        TextView txtName = (TextView) card.findViewById(R.id.castcard_txt_name);
                        TextView txtCharacter = (TextView) card.findViewById(R.id.castcard_txt_character);

                        txtName.setText(cast.get(x)[1]);
                        txtCharacter.setText(cast.get(x)[2]);
                        //ImageView imageView = new ImageView(getActivity());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(3, 0, 3, 0);
                        card.setLayoutParams(lp);
                        linCast.addView(card);
                        //Picasso.with(getActivity()).load(utils.constructMoviePosterURL(cast.get(x)[2], 3)).into(img);
                        String sil = cast.get(x)[3];
                        if(sil.isEmpty() || sil.equals("null")) {
                            Glide.with(getActivity()).load(R.drawable.silhouette).centerCrop().into(img);
                        } else {
                            Glide.with(getActivity()).load(utils.constructMoviePosterURL(cast.get(x)[3], 3)).centerCrop().into(img);
                        }
                    }
                }

                if(movie.getTrailers().isEmpty()) {

                } else {
                    final ArrayList<String[]> trailers = movie.getTrailers();
                    for(int p = 0; p < trailers.size(); p++) {
                        View card = getActivity().getLayoutInflater().inflate(R.layout.trailercard, null);

                        ImageView img = (ImageView) card.findViewById(R.id.trailercard_img);
                        TextView txtName = (TextView) card.findViewById(R.id.trailercard_txt_name);

                        txtName.setText(trailers.get(p)[0]);
                        //ImageView imageView = new ImageView(getActivity());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(10, 0, 10, 0);
                        card.setLayoutParams(lp);
                        linTrail.addView(card);
                        final String videoURL = trailers.get(p)[1];
                        String trailerPoster = "http://img.youtube.com/vi/" + videoURL + "/0.jpg";
                        //Picasso.with(getActivity()).load(trailerPoster).into(img);
                        Glide.with(getActivity()).load(trailerPoster).centerCrop().into(img);

                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoURL)));
                            }
                        });

                    }
                }

                if(movie.getReviews().isEmpty()) {

                } else {
                    final ArrayList<String[]> reviews = movie.getReviews();
                    for(int r = 0; r < reviews.size(); r++) {
                        View card = getActivity().getLayoutInflater().inflate(R.layout.reviewcard, null);

                        TextView txtAuthor = (TextView) card.findViewById(R.id.reviewcard_author);
                        TextView txtContent = (TextView) card.findViewById(R.id.reviewcard_content);

                        txtAuthor.setText(reviews.get(r)[0]);
                        txtContent.setText(reviews.get(r)[1]);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(15, 15, 15, 15);
                        card.setLayoutParams(lp);
                        linRev.addView(card);
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
                StringBuilder genre_ids = new StringBuilder();
                for (int x = 0; x < genres.length(); x++) {
                    JSONObject jsonGenreObject = genres.getJSONObject(x);
                    genre_ids.append(Integer.parseInt(jsonGenreObject.getString("id")) + "");
                    if(x < (genres.length() - 1)) {
                        genre_ids.append(",");
                    }
                }
                movie.setGenres(genre_ids.toString());

                JSONObject credits = (JSONObject) jsonMovieObject.get("credits");
                JSONArray cast = (JSONArray) credits.get("cast");
                ArrayList<String[]> cast_det = new ArrayList<String[]>();
                for(int p = 0; p < cast.length(); p++) {
                    JSONObject jsonCastObject = cast.getJSONObject(p);
                    cast_det.add(new String[] { jsonCastObject.getString("cast_id"), jsonCastObject.getString("name"), jsonCastObject.getString("character"),
                            jsonCastObject.getString("profile_path"), jsonCastObject.getString("order") });

                    Cursor curCast = getActivity().getContentResolver().query(MovieContract.CastEntry.buildCastUri(movie.getId(), Integer.parseInt(cast_det.get(p)[0])), null,
                            null, null, MovieContract.CastEntry.COLUMN_ORDER + " ASC");

                    if(curCast.moveToFirst()) {

                    } else {
                        Time dayTime = new Time();
                        dayTime.setToNow();

                        // we start at the day returned by local time. Otherwise this is a mess.
                        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                        // now we work exclusively in UTC
                        dayTime = new Time();

                        ContentValues cv = new ContentValues();
                        cv.put(MovieContract.CastEntry.COLUMN_MOVIE_ID, movie.getId());
                        cv.put(MovieContract.CastEntry.COLUMN_CAST_ID, cast_det.get(p)[0]);
                        cv.put(MovieContract.CastEntry.COLUMN_NAME, cast_det.get(p)[1]);
                        cv.put(MovieContract.CastEntry.COLUMN_CHARACTER, cast_det.get(p)[2]);
                        cv.put(MovieContract.CastEntry.COLUMN_PROFILE_PATH, cast_det.get(p)[3]);
                        cv.put(MovieContract.CastEntry.COLUMN_ORDER, cast_det.get(p)[4]);
                        cv.put(MovieContract.CastEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));

                        getActivity().getContentResolver().insert(MovieContract.CastEntry.buildCastUri(movie.getId(), Integer.parseInt(cast_det.get(p)[0])), cv);
                    }

                }
                movie.setCast(cast_det);

                JSONObject trailers = (JSONObject) jsonMovieObject.get("trailers");
                JSONArray youtube = (JSONArray) trailers.get("youtube");
                ArrayList<String[]> trailer_det = new ArrayList<String[]>();
                for(int v = 0; v < youtube.length(); v++) {
                    JSONObject jsonTrailerObject = youtube.getJSONObject(v);
                    trailer_det.add(new String[] { jsonTrailerObject.getString("name"), jsonTrailerObject.getString("source") });
                }
                movie.setTrailers(trailer_det);

                JSONObject reviews = (JSONObject) jsonMovieObject.get("reviews");
                JSONArray results = (JSONArray) reviews.get("results");
                ArrayList<String[]> reviews_det = new ArrayList<String[]>();
                for(int r = 0; r < results.length(); r++) {
                    JSONObject jsonReviewObject = results.getJSONObject(r);
                    reviews_det.add(new String[] { jsonReviewObject.getString("author"), jsonReviewObject.getString("content") });
                }
                movie.setReviews(reviews_det);

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
