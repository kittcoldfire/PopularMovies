package phil.nanodegree.com.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static phil.nanodegree.com.popularmovies.data.MovieContract.CastEntry;
import static phil.nanodegree.com.popularmovies.data.MovieContract.MovieEntry;
import static phil.nanodegree.com.popularmovies.data.MovieContract.ReviewEntry;
import static phil.nanodegree.com.popularmovies.data.MovieContract.TrailerEntry;

/**
 * Created by Phil on 8/13/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;

    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                MovieEntry.COLUMN_RELEASE_DATE + " REAL NOT NULL," +
                MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT," +
                MovieEntry.COLUMN_GENRES + " TEXT," +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                MovieEntry.COLUMN_BACKDROP_PATH + " TEXT," +
                MovieEntry.COLUMN_TAGLINE + " TEXT," +
                MovieEntry.COLUMN_RUNTIME + " REAL," +
                MovieEntry.COLUMN_SORT + " INTEGER, " +
                MovieEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;


        final String SQL_CREATE_CAST_TABLE = "CREATE TABLE " + CastEntry.TABLE_NAME + " (" +
                CastEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CastEntry.COLUMN_CAST_ID + " INTEGER NOT NULL, " +
                CastEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                CastEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                CastEntry.COLUMN_CHARACTER + " TEXT, " +
                CastEntry.COLUMN_PROFILE_PATH + " TEXT, " +
                CastEntry.COLUMN_ORDER + " INTEGER, " +
                CastEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrailerEntry.COLUMN_TRAILER_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_SOURCE + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReviewEntry.COLUMN_REVIEW_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_DATE_ADDED + " REAL NOT NULL);"
                ;

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
        db.execSQL(SQL_CREATE_CAST_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CastEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
