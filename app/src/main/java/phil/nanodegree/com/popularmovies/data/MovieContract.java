package phil.nanodegree.com.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by Phil on 8/13/2015.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.nanodegree.phil.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";

    public static final String PATH_GENRES = "genres";

    public static final String PATH_CAST = "cast";

    public static final String PATH_TRAILERS = "trailers";

    public static final String PATH_REVIEWS = "reviews";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class GenreEntry implements BaseColumns {
        public static final String TABLE_NAME = "genres";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_ADDED = "date_added";

        public static final String COLUMN_GENRE_ID = "genre_id";

        public static final String COLUMN_MOVIE_ID = MovieEntry._ID;
    }

    public static final class CastEntry implements BaseColumns {
        public static final String TABLE_NAME = "cast";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_ADDED = "date_added";

        public static final String COLUMN_CAST_ID = "cast_id";

        public static final String COLUMN_MOVIE_ID = MovieEntry._ID;

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_CHARACTER = "character";

        public static final String COLUMN_PROFILE_PATH = "profile_path";
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailers";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_ADDED = "date_added";

        public static final String COLUMN_TRAILER_ID = "trailer_id";

        public static final String COLUMN_MOVIE_ID = MovieEntry._ID;

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_SOURCE = "source";
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_ADDED = "date_added";

        public static final String COLUMN_REVIEW_ID = "review_id";

        public static final String COLUMN_MOVIE_ID = MovieEntry._ID;

        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_CONTENT = "content";
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movies";

        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_ADDED = "date_added";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        //Need to figure out genres

        public static final String COLUMN_TAGLINE = "tagline";

        public static final String COLUMN_RUNTIME = "runtime";

        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";

        public static final String COLUMN_SORT = "sort";

        //figure out cast
        //trailers
        //reviews

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static Uri buildMovieUri(int sort, long id) {
            return ContentUris.withAppendedId(buildMoviesSorted(sort), id); //id at the end after sort
        }

        public static Uri buildMoviesSorted(int sort) {
            return CONTENT_URI.buildUpon().appendPath(sort + "").build(); //only add sort value
        }

        public static int getSortFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }

        public static int getIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }
    }
}
