package phil.nanodegree.com.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import phil.nanodegree.com.popularmovies.utilities.Utils;

/**
 * Created by Phil on 8/13/2015.
 */
public class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    //Used to get all movies
    static final int MOVIES = 100;
    static final int MOVIES_SORTED = 101;
    static final int MOVIE_WITH_ID = 105;
    static final int GENRE_WITH_ID = 200;
    static final int CAST_WITH_ID = 300;
    static final int TRAILER_WITH_ID = 400;
    static final int REVIEWS_WITH_ID = 500;

    private static final SQLiteQueryBuilder sMovieWithOneGenreQueryBuilder;

    static{
        sMovieWithOneGenreQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //movies INNER JOIN genre ON movie._id = genre.movie_id
        sMovieWithOneGenreQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.GenreEntry.TABLE_NAME +
                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID +
                        " = " + MovieContract.GenreEntry.TABLE_NAME +
                        "." + MovieContract.GenreEntry.COLUMN_MOVIE_ID);
    }

    //movie.sort = ?
    private static final String sMovieSortSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_SORT + " IN ? ";

    //movie.id = ?
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry._ID + " = ? ";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES); //Return all movies in the db
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIES_SORTED); //Return movies from certain categories
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#/#", MOVIE_WITH_ID); //Return 1 specific movie, sort than id

        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // movies/#
            case MOVIES_SORTED: {
                retCursor = getMoviesBySortOrder(uri, projection, sortOrder);
                break;
            }
            // movies/#/#
            case MOVIE_WITH_ID: {
                retCursor = getMovieWithId(uri, projection, sortOrder);
                break;
            }
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME, //table
                        projection, //columns/all
                        selection, //selection query (by sort #)
                        selectionArgs, //sort value
                        null, //group by
                        null, //having
                        sortOrder //sort order
                    );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES_SORTED:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(0, _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MovieContract.MovieEntry.COLUMN_DATE_ADDED)) {
            long dateValue = values.getAsLong(MovieContract.MovieEntry.COLUMN_DATE_ADDED);
            values.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, MovieContract.normalizeDate(dateValue));
        }
    }

    private Cursor getMoviesBySortOrder (Uri uri, String[] projection, String sortOrder) {
        int sort = MovieContract.MovieEntry.getSortFromUri(uri);
        Utils utils = new Utils();
        String sortString = utils.getSortString(sort);

        return sMovieWithOneGenreQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection, //columns/all
                sMovieSortSelection, //selection query (by sort #)
                new String[] {"(" + sortString + ")"}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
        );
    }

    private Cursor getMovieWithId (Uri uri, String[] projection, String sortOrder) {
        int id = MovieContract.MovieEntry.getIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME, //table
                projection, //columns/all
                sMovieIdSelection, //selection query (by sort #)
                new String[] {id + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }
}
