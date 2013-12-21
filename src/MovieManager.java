import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.MovieDb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A movie manager backend designed to store information about a contained set of
 * movies. This class is designed for use with a frontend.
 */
public class MovieManager {

    public static final Logger LOGGER = Logger.getLogger(MovieManager.class.getName());
    private static final String API_KEY = "MY_API_KEY";

    private TheMovieDbApi movieDatabase;
    private List<Movie> movieList;

    public MovieManager() {
        try {
            movieDatabase = new TheMovieDbApi(API_KEY);
            movieList = new ArrayList<>();
        }
        catch (MovieDbException e) {
            LOGGER.log(Level.SEVERE, "Could not initialise api.");
            System.exit(1);
        }
    }

    /**
     * Add a movie to the current movie list, if found.
     * @param movieName The name of the movie to add.
     * @return The movie added, or null if the movie could not be found.
     */
    public Movie addMovie(String movieName, File movieFile) {

        MovieDb movieDb = findMovie(movieName);
        if (movieDb != null) {
            Movie movie = new Movie(this, movieDb, movieFile);
            addSorted(movie);
            return movie;
        }

        return null;
    }

    /**
     * Removes a movie from the current movie list.
     * @param movie The movie to remove.
     */
    public void removeMovie(Movie movie) {
        movieList.remove(movie);
    }

    /**
     * Retrieves the current movie list in this movie manager.
     * @return The current movie list.
     */
    public List<Movie> getMovieList() {
        return movieList;
    }

    /**
     * Returns the complete URL of a specified partial image path.
     * @param path The path of the image to get the complete URL for.
     * @return The image URL, or null if an image could not be obtained.
     */
    public URL getImageUrl(String path, String size) {
        try {
            return movieDatabase.createImageUrl(path, size);
        } catch (MovieDbException e) {
            LOGGER.log(Level.SEVERE, e.getResponse());
            return null;
        }
    }

    /**
     * Returns the release year of the specified movie.
     * @param movie The movie to get the year for.
     * @return The release year of the specified movie.
     */
    public String getReleaseYear(Movie movie) {
        String releaseDate = movie.getMovie().getReleaseDate();
        return releaseDate.substring(0, releaseDate.indexOf('-'));
    }

    /**
     * Searches for a given movie using the TheMovieDB API.
     * @param movieName The name of the movie to search.
     * @return The first movie found, or if none were found then null.
     */
    private MovieDb findMovie(String movieName) {

        LOGGER.log(Level.INFO, "Searching for movie: " + movieName);
        MovieDb result;

        try {
            List<MovieDb> results = movieDatabase.searchMovie(
                    movieName, 0, null, false, 0).getResults();

            for (MovieDb potentialMovie : results) {
                LOGGER.log(Level.INFO, "Potential movie: " + potentialMovie.getTitle());
            }

            if (results.isEmpty()) {
                throw new MovieDbException(MovieDbException
                        .MovieDbExceptionType.MOVIE_ID_NOT_FOUND, movieName);
            }
            else {
                MovieDb chosenMovie = results.get(0);
                LOGGER.log(Level.INFO, "Picking first  matching movie: " + chosenMovie.getTitle());
                String searchTerms = "belongsToCollection,genres,homepage," +
                        "imdbID,overview,productionCompanies,spokenLanguages," +
                        "tagline,status,alternativeTitles,casts,images,keywords," +
                        "releases,trailers,translations,similarMovies,reviews,lists";
                result = movieDatabase.getMovieInfo(chosenMovie.getId(), "en", searchTerms);
            }
        }
        catch (MovieDbException e) {
            LOGGER.log(Level.WARNING, "No movies found.");
            result = null;
        }

        return result;
    }

    /**
     * Adds a movie the current movie list, keeping the list sorted.
     * @param movie The movie to add.
     */
    private void addSorted(Movie movie) {

        if (movieList.size() == 0) {
            movieList.add(movie);
            return;
        }

        for (int i = 0; i < movieList.size(); i++) {
            Movie listMovie = movieList.get(i);
            if (movie.getMovie().getTitle().compareToIgnoreCase(listMovie.getMovie().getTitle()) < 0) {
                movieList.add(i, movie);
                return;
            }
        }

        movieList.add(movie);
    }

    public static void main(String[] args) throws IOException {

        MovieManager movieManager = new MovieManager();
        movieManager.addMovie(new BufferedReader(new InputStreamReader(System.in)).readLine(), null);
        for (Movie movie : movieManager.movieList) {
            System.out.println(movie.toString());
        }
    }

}
