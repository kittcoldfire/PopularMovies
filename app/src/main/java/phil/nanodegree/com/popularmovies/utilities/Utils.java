package phil.nanodegree.com.popularmovies.utilities;

import android.net.Uri;

import java.util.ArrayList;

public class Utils {

    public Utils() {

    }

    /**
     * Helper method to build the correct URL to retrieve pictures for the movie posters.
     * @param url The String URL value from the specific movie poster
     * @param imageSize int variable for the different sizes, options are 0="w92", 1="w154", 2="w185", 3="w342", 4="w500", 5="w780", or 6="original", recommended is 2="w185"
     * @return The URL as a String to be downloaded
     */
    public String constructMoviePosterURL(String url, int imageSize) {
        //http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
        //"w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”
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
    public String getSortString(int sort) {
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

    /**
     * Helper method to return a comma seperated String of genres based on an ArrayList of genre id's
     * @param g ArrayList<Integer> containing all the genre id's for a specific movie
     * @return A String containing a readable comma seperated list of genres.
     */
    public String getGenres(ArrayList<Integer> g) {
        StringBuilder genres;
        genres = new StringBuilder();

        for(int x = 0; x < g.size(); x++) {
            int genId = g.get(x);

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
}