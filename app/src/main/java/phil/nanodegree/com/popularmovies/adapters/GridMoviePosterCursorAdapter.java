package phil.nanodegree.com.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import phil.nanodegree.com.popularmovies.R;
import phil.nanodegree.com.popularmovies.data.MovieContract;
import phil.nanodegree.com.popularmovies.utilities.Utils;

/**
 * Created by Phil on 8/20/2015.
 */
public class GridMoviePosterCursorAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView imageView;
        public final TextView txtTitle;
        public final TextView txtGenre;
        public final RatingBar rtbRating;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.gmp_image);
            txtTitle = (TextView) view.findViewById(R.id.gmp_title);
            txtGenre = (TextView) view.findViewById(R.id.gmp_genre);
            rtbRating = (RatingBar) view.findViewById(R.id.gmp_rating);
        }
    }

    public GridMoviePosterCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_movie_poster, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Utils util = new Utils();

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
        viewHolder.txtTitle.setText(title);

        String imagePath = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
        Glide.with(context).load(util.constructMoviePosterURL(imagePath, 3)).into(viewHolder.imageView);

        String g = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_GENRES));
        if(!g.isEmpty() && !g.equals("null") && !g.equals("")) {
            String genres = util.getGenres(g);
            viewHolder.txtGenre.setText(genres);
        }

        double rating = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE));
        viewHolder.rtbRating.setRating((float) rating / 2);
    }
}
