package phil.nanodegree.com.popularmovies.models;

import java.util.ArrayList;

public class Movie {
    private int id;
    private String title;
    private String release_date;
    private String overview;
    private String poster_path;
    private double vote_average;
    private ArrayList<Integer> genres;
    private int run_time;
    private String tagline;
    private String backdrop_path;

    public Movie(int id) {
        this.id = id;
    }

    public Movie(int id, String title, String release_date, String overview, String poster_path, double vote_average) {
        this.id = id;
        this.title = title;
        this.release_date = release_date;
        this.overview = overview;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
    }

    public int getId() { return this.id; }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setReleaseDate(String release_date) {
        this.release_date = release_date;
    }

    public String getReleaseDate() {
        return this.release_date;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOverview() {
        return this.overview;
    }

    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getPosterPath() {
        return this.poster_path;
    }

    public void setVoteAverage(double vote_average) {
        this.vote_average = vote_average;
    }

    public double getVoteAverage() {
        return this.vote_average;
    }

    public void setGenres(ArrayList<Integer> genres) {
        this.genres = genres;
    }

    public ArrayList<Integer> getGenres() {
         return genres;
     }

    public void setTagline (String tagline) {
        this.tagline = tagline;
    }

    public String getTagline() {
        return this.tagline;
    }

    public void setRunTime (int run_time) {
        this.run_time = run_time;
    }

    public int getRunTime() {
        return this.run_time;
    }

    public void setBackdrop (String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public String getBackdrop() {
        return this.backdrop_path;
    }
}
