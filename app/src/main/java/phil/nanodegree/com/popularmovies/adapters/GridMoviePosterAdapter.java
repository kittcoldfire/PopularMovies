package phil.nanodegree.com.popularmovies.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import phil.nanodegree.com.popularmovies.MainActivity;
import phil.nanodegree.com.popularmovies.R;
import phil.nanodegree.com.popularmovies.models.Movie;
import phil.nanodegree.com.popularmovies.utilities.Utils;

public class GridMoviePosterAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Movie> mMovies;
    private ViewHolder mHolder;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    public GridMoviePosterAdapter(Context context, ArrayList<Movie> movies) {
        this.mContext = context;
        this.mMovies = movies;
    }

    @Override
    public int getCount() {
        if(mMovies.size() > 0) {
            return mMovies.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{
        public ImageView imgPoster;
        public TextView txtTitle, txtGenre;
        public RatingBar rbBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Utils utils = new Utils();

        if (v == null ) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.grid_movie_poster, null);
            mHolder = new ViewHolder();
            mHolder.txtTitle = (TextView) v.findViewById(R.id.gmp_title);
            mHolder.txtGenre = (TextView) v.findViewById(R.id.gmp_genre);
            mHolder.rbBar = (RatingBar) v.findViewById(R.id.gmp_rating);
            mHolder.imgPoster = (ImageView) v.findViewById(R.id.gmp_image);
            v.setTag(mHolder);
        } else {
            mHolder=(ViewHolder)v.getTag();
        }

        //update the view
        mHolder.txtTitle.setText(mMovies.get(position).getTitle());
        mHolder.txtTitle.setSelected(true);
        mHolder.txtGenre.setText(utils.getGenres(mMovies.get(position).getGenres()));
        mHolder.txtGenre.setSelected(true);
        mHolder.rbBar.setRating((float) (mMovies.get(position).getVoteAverage() / 2));
        //http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
        Log.d(LOG_TAG, "Image url:" + mMovies.get(position).getPosterPath());
        Log.d(LOG_TAG, "Image path:" + utils.constructMoviePosterURL(mMovies.get(position).getPosterPath(), 3));
        //Picasso.with(mContext).setIndicatorsEnabled(true);
        //Picasso.with(mContext).load(utils.constructMoviePosterURL(mMovies.get(position).getPosterPath(), 3)).into(mHolder.imgPoster);
        Glide.with(mContext).load(utils.constructMoviePosterURL(mMovies.get(position).getPosterPath(), 3)).into(mHolder.imgPoster);

        return v;
    }
}
