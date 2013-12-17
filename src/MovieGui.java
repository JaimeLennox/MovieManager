import com.omertron.themoviedbapi.model.MovieDb;
import com.omertron.themoviedbapi.model.PersonCast;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MovieGui {

    private JFrame frame;

    private JPanel movieEnterPanel;
    private JTextField movieEnterTextField;
    private JButton addMovieButton;

    private JButton scanButton;

    private JPanel movieListPanel;
    private MovieListModel movieListModel;
    private JList<MovieDb> movieList;
    private JScrollPane movieListScrollPane;

    private JPanel outputPanel;
    private CardLayout outputLayout;


    private MovieManager movieManager = new MovieManager();

    /**
     * Launch the application.
     */
    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MovieGui();
            }
        });
    }

    /**
     * Create the application.
     */
    public MovieGui() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        frame = new JFrame("Movie Manager");
        frame.setVisible(true);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setMinimumSize(new Dimension(1280, 720));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));

        movieEnterPanel = new JPanel();
        frame.getContentPane().add(movieEnterPanel, "north");
        movieEnterPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

        movieEnterTextField = new JTextField();
        movieEnterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    addMovie();
                }
            }
        });
        movieEnterPanel.add(movieEnterTextField, "cell 0 0, grow");

        addMovieButton = new JButton("Add movie");
        addMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMovie();
            }
        });
        movieEnterPanel.add(addMovieButton, "cell 0 0");

        movieListPanel = new JPanel();
        movieListPanel.setMinimumSize(new Dimension(400, 0));
        movieListPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        frame.getContentPane().add(movieListPanel, "grow");
        movieListPanel.setLayout(new MigLayout("flowy", "[grow]", "[grow]"));

        scanButton = new JButton("Scan for movies");
        scanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                scanForMovies(fileChooser.getSelectedFile());
            }
        });
        movieListPanel.add(scanButton, "growx, split");

        movieListModel = new MovieListModel(movieManager);
        movieList = new JList<>(movieListModel);
        movieList.setCellRenderer(new MovieListRenderer());
        movieList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieList.addListSelectionListener(new MovieSelectionListener());
        movieListScrollPane = new JScrollPane(movieList);
        movieListPanel.add(movieListScrollPane, "grow");

        outputPanel = new JPanel();
        frame.getContentPane().add(outputPanel, "grow");
        outputLayout = new CardLayout();
        outputPanel.setLayout(outputLayout);
    }

    private void addMovie() {
        String name = movieEnterTextField.getText();

        if (!name.isEmpty()) {
            movieListModel.addElement(name);
        }
    }

    private void scanForMovies(File folder) {

        for (File file : folder.listFiles()) {
            if (file.isFile() && isMovieFile(file)) {
                // Remove extensions from name and add to model.
                movieListModel.addElement(getMovieName(file.getName()));
            }
            // Recurse into other directories.
            else if (file.isDirectory()) {
                scanForMovies(file);
            }
        }

    }

    private String getMovieName(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> parts = Arrays.asList(fileName.split("\\."));

        for (String part : parts) {
            try {
                Integer.parseInt(part);
                break;
            }
            catch (NumberFormatException e) {
                stringBuilder.append(part).append(" ");
            }
        }

        return stringBuilder.toString();
    }

    private boolean isMovieFile(File file) {

        final List<String> movieExtensions = Arrays.asList("mp4", "avi", "flv",
                "webm", "ogg", "mov" ,"3gp", "wmv");

        for (String extension : movieExtensions) {
            if (file.getName().endsWith(extension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Dynamically create a movie panel for each movie. This is added to a
     * global CardLayout to switch when needed.
     * @param movie The movie to create the panel for.
     */
    private void createMoviePanel(final MovieDb movie) {

        if (movie == null) {
            return;
        }

        BufferedImage backdrop = null;
        BufferedImage poster = null;
        try {
            backdrop = ImageIO.read(movieManager.getImageUrl(
                    movie.getBackdropPath(), "w780"));
            poster = ImageIO.read(movieManager.getImageUrl(
                    movie.getPosterPath(), "w342"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Scale image to poster height, adjusting width as necessary (via -1).
        ImageIcon backdropImage = new ImageIcon(backdrop.getScaledInstance(
                -1, poster.getHeight(), Image.SCALE_SMOOTH));
        ImageIcon posterImage = new ImageIcon(poster);

        StringBuilder castList = new StringBuilder();
        for (PersonCast person : movie.getCast()) {
            castList.append(person.getName());
            castList.append(", ");
        }

        JLabel movieNameLabel = new JLabel(movie.getTitle() + " ("
                + movieManager.getReleaseYear(movie) + ")");
        movieNameLabel.setFont(new Font("Tahoma", Font.BOLD, 24));

        JLabel taglineLabel = new JLabel(movie.getTagline());
        taglineLabel.setFont(new Font("Tahoma", Font.ITALIC, 18));

        JLabel backdropLabel = new JLabel(backdropImage);
        JLabel posterLabel = new JLabel(posterImage);
        JLabel overviewLabel = new JLabel("Overview: ");
        JLabel castLabel = new JLabel("Cast: ");

        JTextPane overviewTextArea = new JTextPane();
        JTextPane castTextArea = new JTextPane();

        overviewTextArea.setText(movie.getOverview());
        castTextArea.setText(castList.toString());

        JPanel newMoviePanel = new JPanel();
        newMoviePanel.setVisible(false);
        newMoviePanel.setLayout(new MigLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout());
        infoPanel.add(overviewLabel);
        infoPanel.add(overviewTextArea, "wrap");
        infoPanel.add(castLabel);
        infoPanel.add(castTextArea, "wrap");

        newMoviePanel.add(movieNameLabel, "wrap");
        newMoviePanel.add(taglineLabel, "wrap");
        newMoviePanel.add(backdropLabel);
        newMoviePanel.add(posterLabel, "wrap");
        newMoviePanel.add(infoPanel, "span");

        outputPanel.add(newMoviePanel, Integer.toString(movie.getId()));
    }

    private class MovieListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof MovieDb) {
                MovieDb movie = (MovieDb) value;
                setText(movie.getTitle());
            }

            return this;
        }
    }

    private class MovieListModel extends AbstractListModel<MovieDb> {

        private MovieManager movieManager;

        public MovieListModel(MovieManager movieManager) {
            this.movieManager = movieManager;
        }

        public void addElement(String element) {
            MovieDb movie = movieManager.addMovie(element);
            if (movie != null) {
                createMoviePanel(movie);
                fireIntervalAdded(this, getSize() - 1, getSize() - 1);
            } else {
                JOptionPane.showMessageDialog(frame, "No movies found.");
            }
        }

        @Override
        public int getSize() {
            return movieManager.getMovieList().size();
        }

        @Override
        public MovieDb getElementAt(int index) {
            return movieManager.getMovieList().get(index);
        }
    }

    private class MovieSelectionListener implements ListSelectionListener{

        @Override
        public void valueChanged(ListSelectionEvent e) {

            if (!e.getValueIsAdjusting()) {

                final MovieDb movie = ((JList<MovieDb>) e.getSource()).getSelectedValue();

                if (movie != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            outputLayout.show(outputPanel, Integer.toString(movie.getId()));
                        }
                    });
                }
            }
        }
    }

}