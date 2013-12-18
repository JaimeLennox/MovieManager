import com.omertron.themoviedbapi.model.MovieDb;
import com.omertron.themoviedbapi.model.PersonCast;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;

public class Movie implements Comparable<Movie> {

    private MovieManager movieManager;
    private MovieDb movie;
    private String castList;
    private Map<ImageType, ImageIcon> images = new HashMap<>();

    public Movie(MovieManager movieManager, MovieDb movie) {

        this.movieManager = movieManager;
        this.movie = movie;
        createCast(movie);
        createImages(movie);
    }

    public MovieDb getMovie() {
        return movie;
    }

    public String getCastList() {
        return castList;
    }

    public ImageIcon getImage(ImageType imageType) {
        return images.get(imageType);
    }

    @Override
    public String toString() {
        return getMovie().getTitle();
    }

    @Override
    public int compareTo(Movie o) {
        return movie.getTitle().compareTo(o.getMovie().getTitle());
    }

    private void createCast(MovieDb movie) {

        StringBuilder castListBuilder = new StringBuilder();
        List<PersonCast> cast = movie.getCast();

        final int maxCastMembers = 10;

        for (int i = 0; i < cast.size() && i <= maxCastMembers; i++) {
            PersonCast person = cast.get(i);
            castListBuilder.append(person.getName());

            if (i != maxCastMembers) {
                castListBuilder.append(", ");
            }
        }

        castList = castListBuilder.toString();
    }

    private void createImages(MovieDb movie) {

        BufferedImage backdrop = null;
        BufferedImage poster = null;
        try {
            backdrop = ImageIO.read(movieManager.getImageUrl(
                    movie.getBackdropPath(), "w780"));
            poster = ImageIO.read(movieManager.getImageUrl(
                    movie.getPosterPath(), "w342"));
        } catch (IOException e) {
            MovieManager.LOGGER.log(Level.WARNING, "Images could not be parsed.");
        }


        // Scale image to poster height, adjusting width as necessary (via -1).
        ImageIcon backdropImage = new ImageIcon(backdrop.getScaledInstance(
                -1, poster.getHeight(), Image.SCALE_SMOOTH));
        ImageIcon posterImage = new ImageIcon(poster);

        images.put(ImageType.BACKDROP, backdropImage);
        images.put(ImageType.POSTER, posterImage);
    }

}