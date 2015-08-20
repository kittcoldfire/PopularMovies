package phil.nanodegree.com.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    static final int CAST_WITH_ID = 200;
    static final int CAST_FOR_MOVIE = 201;
    static final int TRAILER_WITH_ID = 300;
    static final int TRAILERS_FOR_MOVIE = 301;
    static final int REVIEWS_WITH_ID = 400;
    static final int REVIEWS_FOR_MOVIE = 401;

    //movie.sort = ?
    private static final String sMovieSortSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_SORT + " IN ? ";

    //movie.id = ?
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry._ID + " = ? ";

    //cast.id = ? AND movie.id = ?
    private static final String sCastIdSelection =
            MovieContract.CastEntry.COLUMN_MOVIE_ID + " = ? " +
            "AND " + MovieContract.CastEntry.COLUMN_CAST_ID + " = ? ";

    //movie.id = ?
    private static final String sCastMovieSelection =
            MovieContract.CastEntry.COLUMN_MOVIE_ID + " = ? ";

    //trailer.id = ? AND movie.id = ?
    private static final String sTrailerIdSelection =
            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? " +
                    "AND " + MovieContract.TrailerEntry.COLUMN_TRAILER_ID + " = ? ";

    //movie.id = ?
    private static final String sTrailerMovieSelection =
            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    //review.id = ? AND movie.id = ?
    private static final String sReviewIdSelection =
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? " +
                    "AND " + MovieContract.ReviewEntry.COLUMN_REVIEW_ID + " = ? ";

    //review.id = ?
    private static final String sReviewMovieSelection =
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIES, MOVIES); //Return all movies in the db
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", MOVIES_SORTED); //Return movies from certain categories
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#/#", MOVIE_WITH_ID); //Return 1 specific movie, sort than id

        matcher.addURI(authority, MovieContract.PATH_CAST + "/#", CAST_FOR_MOVIE); //MovieId
        matcher.addURI(authority, MovieContract.PATH_CAST + "/#/#", CAST_WITH_ID); //MovieId, CastId

        matcher.addURI(authority, MovieContract.PATH_TRAILERS + "/#", TRAILERS_FOR_MOVIE); //MovieId
        matcher.addURI(authority, MovieContract.PATH_TRAILERS + "/#/#", TRAILER_WITH_ID); //MovieId, TrailerId

        matcher.addURI(authority, MovieContract.PATH_REVIEWS + "/#", REVIEWS_FOR_MOVIE); //MovieId
        matcher.addURI(authority, MovieContract.PATH_REVIEWS + "/#/#", REVIEWS_WITH_ID); //MovieId, ReciewId

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
            case CAST_WITH_ID: {
                retCursor = getCastWithId(uri, projection, sortOrder);
                break;
            }
            case CAST_FOR_MOVIE: {
                retCursor = getCastForMovie(uri, projection, sortOrder);
                break;
            }
            case TRAILER_WITH_ID: {
                retCursor = getTrailerWithId(uri, projection, sortOrder);
                break;
            }
            case TRAILERS_FOR_MOVIE: {
                retCursor = getTrailerForMovie(uri, projection, sortOrder);
                break;
            }
            case REVIEWS_WITH_ID: {
                retCursor = getReviewWithId(uri, projection, sortOrder);
                break;
            }
            case REVIEWS_FOR_MOVIE: {
                retCursor = getReviewForMovie(uri, projection, sortOrder);
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
            case CAST_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case CAST_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILER_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS_FOR_MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case REVIEWS_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEWS_FOR_MOVIE:
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
            case CAST_WITH_ID: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.CastEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.CastEntry.buildCastUri(MovieContract.CastEntry.getMovieIdFromUri(uri), _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER_WITH_ID: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(MovieContract.TrailerEntry.getMovieIdFromUri(uri), _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS_WITH_ID: {
                normalizeDate(values);
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(MovieContract.ReviewEntry.getMovieIdFromUri(uri), _id);
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
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
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
        } else if (values.containsKey(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)) {
            long dateValue = values.getAsLong(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, MovieContract.normalizeDate(dateValue));
        }
    }

    private Cursor getMoviesBySortOrder (Uri uri, String[] projection, String sortOrder) {
        int sort = MovieContract.MovieEntry.getSortFromUri(uri);
        Utils utils = new Utils();
        String sortString = utils.getSortString(sort);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection, //columns/all
                sMovieSortSelection, //selection query (by sort #)
                new String[] {"(" + sortString + ")"}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
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

    private Cursor getCastWithId (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.CastEntry.getMovieIdFromUri(uri);
        int castId = MovieContract.CastEntry.getCastIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.CastEntry.TABLE_NAME, //table
                projection, //columns/all
                sCastIdSelection, //selection query (by sort #)
                new String[] {movieId + "", castId + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }

    private Cursor getCastForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.CastEntry.getMovieIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.CastEntry.TABLE_NAME, //table
                projection, //columns/all
                sCastMovieSelection, //selection query (by sort #)
                new String[] {movieId + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }

    private Cursor getTrailerWithId (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);
        int trailerId = MovieContract.TrailerEntry.getTrailerIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME, //table
                projection, //columns/all
                sTrailerIdSelection, //selection query (by sort #)
                new String[] {movieId + "", trailerId + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }

    private Cursor getTrailerForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME, //table
                projection, //columns/all
                sTrailerMovieSelection, //selection query (by sort #)
                new String[] {movieId + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }

    private Cursor getReviewWithId (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);
        int reviewId = MovieContract.ReviewEntry.getReviewIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME, //table
                projection, //columns/all
                sReviewIdSelection, //selection query (by sort #)
                new String[] {movieId + "", reviewId + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }

    private Cursor getReviewForMovie (Uri uri, String[] projection, String sortOrder) {
        int movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);

        return (mOpenHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME, //table
                projection, //columns/all
                sReviewMovieSelection, //selection query (by sort #)
                new String[] {movieId + ""}, //sort value
                null, //group by
                null, //having
                sortOrder //sort order
            )
        );
    }
}
