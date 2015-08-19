package phil.nanodegree.com.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Movie implements Parcelable {
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
    private ArrayList<String[]> cast; //0 Name, 1 Character, 2 profile pic
    private ArrayList<String[]> trailers; //0 Name, 1 video link
    private ArrayList<String[]> reviews; //0 Author, 1 Content

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

    /*
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
     */
    public Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        release_date = in.readString();
        overview = in.readString();
        poster_path = in.readString();
        vote_average = in.readDouble();
        genres = (ArrayList<Integer>) in.readSerializable();
        run_time = in.readInt();
        tagline = in.readString();
        backdrop_path = in.readString();
        cast = (ArrayList<String[]>) in.readSerializable();
        trailers = (ArrayList<String[]>) in.readSerializable();
        reviews = (ArrayList<String[]>) in.readSerializable();
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

    public void setCast(ArrayList<String[]> cast) {
        this.cast = cast;
    }

    public ArrayList<String[]> getCast() {
        return cast;
    }

    public void setTrailers(ArrayList<String[]> trailers) {
        this.trailers = trailers;
    }

    public ArrayList<String[]> getTrailers() {
        return trailers;
    }

    public void setReviews(ArrayList<String[]> reviews) {
        this.reviews = reviews;
    }

    public ArrayList<String[]> getReviews() {
        return reviews;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(release_date);
        parcel.writeString(overview);
        parcel.writeString(poster_path);
        parcel.writeDouble(vote_average);
        parcel.writeSerializable(genres);
        parcel.writeString(tagline);
        parcel.writeString(backdrop_path);
        parcel.writeSerializable(cast);
        parcel.writeSerializable(trailers);
        parcel.writeSerializable(reviews);
    }

    public final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
