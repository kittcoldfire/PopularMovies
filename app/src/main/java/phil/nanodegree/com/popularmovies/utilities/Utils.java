package phil.nanodegree.com.popularmovies.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.Time;

import phil.nanodegree.com.popularmovies.R;
import phil.nanodegree.com.popularmovies.data.MovieContract;

public class Utils {

    private static final String PREF_SORT = "pref_sort"; //0 for popular, 1 highest rated, 2 highest revenue
    private static final String PREF_SEARCH = "pref_search"; //0 for popular, 1 highest rated, 2 highest revenue
    private static String mPrefSearchPopular = "popularity.desc";
    private static String mPrefSearchHRated = "vote_average.desc";
    private static String mPrefSearchHRevenue = "revenue.desc";

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_GENRES,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_SORT,
            MovieContract.MovieEntry.COLUMN_DATE_ADDED
    };

    public Utils() {

    }

    /**
     * Helper method to determine if the date is older than a day old
     * @param date the date to check if its older than a day old
     * @return boolean false means the date is older than 1 day
     */
    public static boolean checkIfDatesAreFresh(long date) {
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        if (date <= dayTime.setJulianDay(julianStartDay - 1)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Helper method to build the correct URL to retrieve pictures for the movie posters.
     * @param url The String URL value from the specific movie poster
     * @param imageSize int variable for the different sizes, options are 0="w92", 1="w154", 2="w185", 3="w342", 4="w500", 5="w780", or 6="original", recommended is 2="w185"
     * @return The URL as a String to be downloaded
     */
    public String constructMoviePosterURL(String url, int imageSize) {
        /*
        http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
        "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”
        */
        String size;
        switch (imageSize) {
            case 0:
                size = "w92";
                break;
            case 1:
                size = "w154";
                break;
            case 2:
                size = "w185";
                break;
            case 3:
                size = "w342";
                break;
            case 4:
                size = "w500";
                break;
            case 5:
                size = "w780";
                break;
            case 6:
                size = "original";
                break;
            default:
                size = "w185";
                break;
        }


        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath(size)
                .appendEncodedPath(url);

        return builder.build().toString();
    }

    /*
        Popular, Rated, Revenue, Favorite
        1 - Popular
        5 - Rated
        11 - Revenue
        20 - Favorite

        6 = Popular/Rated
        12 = Popular/Revenue
        21 = Popular/Favorite
        16 = Rated/Revenue
        25 = Rated/Favourite
        31 = Revenue/Favorite

        17 = Popular/Rated/Revenue
        26 = Popular/Rated/Favorite
        32 = Popular/Revenue/Favorite
        36 = Rated/Revenue/Favorite

        37 = Popular/Rated/Revenue/Favorite
     */

    /**
     * Helper method to return a comma delimited String of all number values that may be stored in the database for sorting
     * sections of movies.
     * @param sort - int value of the sort section, 1 - Most Popular, 5 - Highest Rated, 11 - Highest Revenue, 20 - Favourite Movies
     * @return A string delimited by commas to be used in SQL queries to find movies in each section
     */
    public static String getSortString(int sort) {
        String sortString = "";

        switch (sort) {
            case 1: //Anything with Popular in it
                sortString = "1, 6, 12, 21, 17, 26, 32, 37";
                break;
            case 5: //Anything with Rated in it
                sortString = "5, 6, 16, 25, 17, 26, 36, 37";
                break;
            case 11: //Anything with Revenue in it
                sortString = "11, 12, 16, 31, 17, 32, 36, 37";
                break;
            case 20: //Anything with Favourite in it
                sortString = "20, 21, 25, 31, 26, 32, 36, 37";
                break;
        }

        return sortString;
    }

    /***
     * Helper method that returns the updated Sort value to be stored in the DB when updating records.
     * This way we retain all other sections this record was from.
     * @param currentSection The section we're upating, popular, revenue, rating, favorite
     * @param currentRecordSortValue The value from the DB that we're updating
     * @return Either the same value, or the new value without the current section
     */
    public static int getUpdatedMovieSortValue(int currentSection, int currentRecordSortValue) {
        String[] sortString;

        switch (currentSection) {
            case 1: //Anything with Popular in it
                sortString = new String[]{"1", "6", "12", "21", "17", "26", "32", "37"};
                break;
            case 5: //Anything with Rated in it
                sortString = new String[]{"5", "6", "16", "25", "17", "26", "36", "37"};
                break;
            case 11: //Anything with Revenue in it
                sortString = new String[]{"11", "12", "16", "31", "17", "32", "36", "37"};
                break;
            case 20: //Anything with Favourite in it
                sortString = new String[]{"20", "21", "25", "31", "26", "32", "36", "37"};
                break;
            default:
                sortString = new String[]{};
        }
        for(int x = 0; x < sortString.length; x++) {
            if(currentRecordSortValue == Integer.parseInt(sortString[x])) {
                return currentRecordSortValue;
            }
        }

        return (currentRecordSortValue + currentSection);
    }

    public static String[] getSortStringArray(int sort) {
        String[] sortString;

        switch (sort) {
            case 1: //Anything with Popular in it
                sortString = new String[] { "1", "6", "12", "21", "17", "26", "32", "37" };
                break;
            case 5: //Anything with Rated in it
                sortString = new String[] { "5", "6", "16", "25", "17", "26", "36", "37" };
                break;
            case 11: //Anything with Revenue in it
                sortString = new String[] { "11", "12", "16", "31", "17", "32", "36", "37" };
                break;
            case 20: //Anything with Favourite in it
                sortString = new String[] { "20", "21", "25", "31", "26", "32", "36", "37" };
                break;
            default:
                sortString = new String[] {};
        }

        return sortString;
    }

    /**
     * Helper method to return a comma seperated String of genres based on an ArrayList of genre id's
     * @param strGen String containing all the genre id's for a specific movie in comma delimited string
     * @return A String containing a readable comma seperated list of genres.
     */
    public String getGenres(String strGen) {
        StringBuilder genres;
        genres = new StringBuilder();

        String[] g = strGen.split(",");

        for(int x = 0; x < g.length; x++) {
            int genId = Integer.parseInt(g[x]);

            if(genres.length() != 0) {
                genres.append(", ");
            }
            switch (genId) {
                case 28:
                    genres.append("Action");
                    break;
                case 12:
                    genres.append("Adventure");
                    break;
                case 16:
                    genres.append("Animation");
                    break;
                case 35:
                    genres.append("Comedy");
                    break;
                case 80:
                    genres.append("Crime");
                    break;
                case 99:
                    genres.append("Documentary");
                    break;
                case 18:
                    genres.append("Drama");
                    break;
                case 10751:
                    genres.append("Family");
                    break;
                case 14:
                    genres.append("Fantasy");
                    break;
                case 10769:
                    genres.append("Foreign");
                    break;
                case 36:
                    genres.append("History");
                    break;
                case 27:
                    genres.append("Horror");
                    break;
                case 10402:
                    genres.append("Music");
                    break;
                case 9648:
                    genres.append("Mystery");
                    break;
                case 10749:
                    genres.append("Romance");
                    break;
                case 878:
                    genres.append("Science Fiction");
                    break;
                case 10770:
                    genres.append("TV Movie");
                    break;
                case 53:
                    genres.append("Thriller");
                    break;
                case 10752:
                    genres.append("War");
                    break;
                case 37:
                    genres.append("Western");
                    break;
                default:
                    break;
            }
        }

        return genres.toString();
    }

    public static int getSortSection(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getInt(PREF_SORT, 1);
    }

    public static void setSortSection(Context context, int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (id == R.id.action_sort_popular) {
            editor.putInt(PREF_SORT, 1);
            editor.putString(PREF_SEARCH, mPrefSearchPopular);
            editor.commit();
        } else if(id == R.id.action_sort_highest_rated) {
            editor.putInt(PREF_SORT, 5);
            editor.putString(PREF_SEARCH, mPrefSearchHRated);
            editor.commit();
        } else if(id == R.id.action_sort_highest_revenue) {
            editor.putInt(PREF_SORT, 11);
            editor.putString(PREF_SEARCH, mPrefSearchHRevenue);
            editor.commit();
        } else if(id == R.id.action_sort_favorite) {
            editor.putInt(PREF_SORT, 20);
            editor.putString(PREF_SEARCH, "");
            editor.commit();
        }
    }

    public static boolean checkIfSectionDataIsCurrent(Context context) {
        String sortOrder = null;

        int sortSection = Utils.getSortSection(context);
        Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(sortSection);

        Cursor curMov = context.getContentResolver().query(moviesSortedUri, null, null, null, null);

        if(curMov.moveToFirst()) {
            while(curMov.moveToNext()) {
                long time = curMov.getLong(curMov.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE_ADDED));

                if(checkIfDatesAreFresh(time)) {

                } else {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    public static boolean isRecordInFavorite(Context context, int sortNum) {
        String[] favArray = getSortStringArray(20); //20 is for favorite

        for(int x = 0; x < favArray.length; x++) {
            if(favArray[x].equals(sortNum + "")) {
                return true;
            }
        }

        return false;
    }

    public static void cleanUpOldData(Context context) {
        //Find all favorite movies, update their section to only favorite, then delete rest of entries
        String sortOrder = null;

        int favSection = 20;
        Uri moviesSortedUri = MovieContract.MovieEntry.buildMoviesSorted(favSection);
        Cursor favCursor = context.getContentResolver().query(
                moviesSortedUri,
                MOVIES_COLUMNS,
                null,
                null,
                sortOrder);

        if(favCursor.moveToFirst()) {
            do {
                Time dayTime = new Time();
                dayTime.setToNow();

                // we start at the day returned by local time. Otherwise this is a mess.
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

                // now we work exclusively in UTC
                dayTime = new Time();

                ContentValues cv = new ContentValues();

                int id = favCursor.getInt(favCursor.getColumnIndex(MovieContract.MovieEntry._ID));

                cv.put(MovieContract.MovieEntry.COLUMN_SORT, 20); //Fav column value
                cv.put(MovieContract.MovieEntry.COLUMN_DATE_ADDED, Long.toString(dayTime.setJulianDay(julianStartDay)));
                context.getContentResolver().update(MovieContract.MovieEntry.buildMovieUri(20, id), cv, MovieContract.MovieEntry._ID + " = ?", new String[] { id + ""});
            } while (favCursor.moveToNext());
        }
        //Delete all other entries in that section
        // delete old data so we don't build up an endless history
        Uri deleteURI = MovieContract.MovieEntry.buildMoviesSorted(Utils.getSortSection(context));
        context.getContentResolver().delete(deleteURI,
                MovieContract.MovieEntry.COLUMN_SORT + " IN ( ?, ?, ?, ?, ?, ?, ?, ? )",
                Utils.getSortStringArray(Utils.getSortSection(context)));
    }
}