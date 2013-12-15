import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.MovieDb;

import java.io.BufferedReader;
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

    private static final Logger logger = Logger.getLogger(MovieManager.class.getName());
    private static final String API_KEY = "MY_API_KEY";

    private TheMovieDbApi movieDatabase;
    private List<MovieDb> movieList;

    public MovieManager() {
        try {
            movieDatabase = new TheMovieDbApi(API_KEY);
            movieList = new ArrayList<>();
        }
        catch (MovieDbException e) {
            logger.log(Level.SEVERE, "Could not initialise api.", e);
        }
    }

    /**
     * Add a movie to the current movie list.
     * @param movieName The name of the movie to add.
     */
    public void addMovie(String movieName) {
        movieList.add(findMovie(movieName));
    }

    /**
     * Remove a movie from the current movie list.
     * @param movieName The name of the movie to remove.
     */
    public void removeMovie(String movieName) {
        movieList.remove(findMovie(movieName));
    }

    /**
     * Retrieves the current movie list in this movie manager.
     * @return The current movie list.
     */
    public List<MovieDb> getMovieList() {
        return movieList;
    }

    /**
     * Returns the backdrop image URL for the specified movie.
     * @param movie The movie to get the backdrop URL for.
     * @return The backdrop image URL, or null if an image could not be obtained.
     */
    public URL getBackdropUrl(MovieDb movie) {
        try {
            return movieDatabase.createImageUrl(movie.getBackdropPath(), "w780");
        } catch (MovieDbException e) {
            logger.log(Level.SEVERE, e.getResponse(), e);
            return null;
        }
    }

    /**
     * Searches for a given movie using the TheMovieDB API.
     * @param movieName The name of the movie to search.
     * @return The first movie found, or if none were found then null.
     */
    private MovieDb findMovie(String movieName) {

        logger.log(Level.INFO, "Searching for movie: " + movieName);
        MovieDb result;

        try {
            List<MovieDb> results = movieDatabase.searchMovie(
                    movieName, 0, null, false, 0).getResults();

            for (MovieDb potentialMovie : results) {
                logger.log(Level.INFO, "Potential movie: " + potentialMovie.getTitle());
            }

            if (results.isEmpty()) {
                throw new MovieDbException(MovieDbException
                        .MovieDbExceptionType.MOVIE_ID_NOT_FOUND, movieName);
            }
            else {
                MovieDb chosenMovie = results.get(0);
                logger.log(Level.INFO, "Picking first  matching movie: " + chosenMovie.getTitle());
                result = chosenMovie;
            }
        }
        catch (MovieDbException e) {
            logger.log(Level.WARNING, "No movies found.", e);
            result = null;
        }

        return result;
    }

    public static void main(String[] args) throws IOException {

        MovieManager movieManager = new MovieManager();
        movieManager.addMovie(new BufferedReader(new InputStreamReader(System.in)).readLine());
        for (MovieDb movie : movieManager.movieList) {
            System.out.println(movie.toString());
        }
    }

}
