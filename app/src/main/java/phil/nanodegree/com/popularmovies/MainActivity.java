package phil.nanodegree.com.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements  MainActivityFragment.Callback {

    private static final String MOVIEDETAILFRAGMENT_TAG = "MDFTAG";

    private boolean mTwoPane;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), MOVIEDETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // update the location in our second pane using the fragment manager
        MainActivityFragment maf = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_movies);
        if ( null != maf ) {
            maf.setIfTwoPane(mTwoPane);
        }
        MovieDetailFragment mdf = (MovieDetailFragment)getSupportFragmentManager().findFragmentByTag(MOVIEDETAILFRAGMENT_TAG);
        if ( null != mdf ) {
            mdf.setIfTwoPane(mTwoPane);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int movieId, boolean preLoaded) {
        if(!isConnected) {
            if(!preLoaded) {
                Toast.makeText(this, "Content not downloaded for this title, please reconnect internet connection to view details!", Toast.LENGTH_SHORT).show();
            } else {
                if (mTwoPane) {
                    // In two-pane mode, show the detail view in this activity by
                    // adding or replacing the detail fragment using a
                    // fragment transaction.
                    Bundle args = new Bundle();
                    args.putInt("movieId", movieId);

                    MovieDetailFragment fragment = new MovieDetailFragment();
                    fragment.setArguments(args);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, fragment, MOVIEDETAILFRAGMENT_TAG)
                            .commit();
                } else {
                    Intent intent = new Intent(this, MovieDetailActivity.class)
                            .putExtra("movieId", movieId);
                    startActivity(intent);
                }
            }
        } else {
            if (mTwoPane) {
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                Bundle args = new Bundle();
                args.putInt("movieId", movieId);

                MovieDetailFragment fragment = new MovieDetailFragment();
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, fragment, MOVIEDETAILFRAGMENT_TAG)
                        .commit();
            } else {
                Intent intent = new Intent(this, MovieDetailActivity.class)
                        .putExtra("movieId", movieId);
                startActivity(intent);
            }
        }
    }
}
