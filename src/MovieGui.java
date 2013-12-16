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
import java.io.IOException;

public class MovieGui {

    private JFrame frame;

    private JPanel movieEnterPanel;
    private JTextField movieEnterTextField;
    private JButton addMovieButton;

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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));

        movieEnterPanel = new JPanel();
        frame.getContentPane().add(movieEnterPanel, "north");
        movieEnterPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

        movieEnterTextField = new JTextField();
        movieEnterTextField.setFont(movieEnterTextField.getFont().deriveFont((float)18));
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
        addMovieButton.setFont(addMovieButton.getFont().deriveFont((float)18));
        addMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMovie();
            }
        });
        movieEnterPanel.add(addMovieButton, "cell 1 0");

        movieListPanel = new JPanel();
        movieListPanel.setMinimumSize(new Dimension(300, 0));
        movieListPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        frame.getContentPane().add(movieListPanel, "grow");
        movieListPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

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

    /**
     * Dynamically create a movie panel for each movie. This is added to a
     * global CardLayout to switch when needed.
     * @param movie The movie to create the panel for.
     */
    private void createMoviePanel(final MovieDb movie) {

        final JPanel newMoviePanel = new JPanel();
        newMoviePanel.setVisible(false);
        newMoviePanel.setLayout(new MigLayout());

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

        JTextArea overviewTextArea = new JTextArea(movie.getOverview());
        JTextArea castTextArea = new JTextArea(castList.toString());

        newMoviePanel.add(movieNameLabel, "cell 0 0, flowy");
        newMoviePanel.add(taglineLabel, "cell 0 0");
        newMoviePanel.add(backdropLabel, "cell 0 1");
        newMoviePanel.add(posterLabel, "cell 0 1");
        newMoviePanel.add(overviewLabel, "cell 0 2");
        newMoviePanel.add(overviewTextArea, "cell 0 2");
        newMoviePanel.add(castLabel, "cell 0 3");
        newMoviePanel.add(castTextArea, "cell 0 3");

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

            setFont(getFont().deriveFont((float)18));

            return this;
        }
    }

    private class MovieListModel extends AbstractListModel<MovieDb> {

        private MovieManager movieManager;

        public MovieListModel(MovieManager movieManager) {
            this.movieManager = movieManager;
        }

        public void addElement(String element) {
            createMoviePanel(movieManager.addMovie(element));
            fireIntervalAdded(this, getSize() - 1, getSize() - 1);
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