import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
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
    private JList<Movie> movieList;
    private JScrollPane movieListScrollPane;

    private JPanel outputPanel;
    private JLabel movieNameLabel;
    private JLabel taglineLabel;
    private JLabel overviewLabel;
    private JLabel backdropLabel;
    private JLabel posterLabel;
    private JLabel castLabel;
    private JTextPane overviewTextArea;
    private JTextPane castTextArea;

    private MovieManager movieManager = new MovieManager();
    private List<Movie> movieInfoList = new ArrayList<>();

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

        createMoviePanel();
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
     * Initialise an empty panel with space for movie information.
     */
    private void createMoviePanel() {
        movieNameLabel = new JLabel();
        movieNameLabel.setFont(new Font("Tahoma", Font.BOLD, 24));

        taglineLabel = new JLabel();
        taglineLabel.setFont(new Font("Tahoma", Font.ITALIC, 18));

        backdropLabel = new JLabel();
        posterLabel = new JLabel();
        overviewLabel = new JLabel("Overview: ");
        castLabel = new JLabel("Cast: ");

        overviewTextArea = new JTextPane();
        castTextArea = new JTextPane();

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout());
        infoPanel.add(overviewLabel);
        infoPanel.add(overviewTextArea, "wrap");
        infoPanel.add(castLabel);
        infoPanel.add(castTextArea, "wrap");

        outputPanel.setVisible(false);
        outputPanel.setLayout(new MigLayout());

        outputPanel.add(movieNameLabel, "wrap");
        outputPanel.add(taglineLabel, "wrap");
        outputPanel.add(backdropLabel);
        outputPanel.add(posterLabel, "wrap");
        outputPanel.add(infoPanel, "span");
    }

    /**
     * Switch the output panel to the selected movie's information.
     * @param movie The movie to switch to.
     */
    private void changeMovie(final Movie movie) {

        if (movie == null) {
            return;
        }

        movieNameLabel.setText(movie.getMovie().getTitle() + " ("
                + movieManager.getReleaseYear(movie) + ")");
        taglineLabel.setText(movie.getMovie().getTagline());

        backdropLabel.setIcon(movie.getImage(ImageType.BACKDROP));
        posterLabel.setIcon(movie.getImage(ImageType.POSTER));

        overviewTextArea.setText(movie.getMovie().getOverview());
        castTextArea.setText(movie.getCastList());

        outputPanel.setVisible(true);
    }

    private class MovieListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Movie) {
                Movie movie = (Movie) value;
                setText(movie.getMovie().getTitle());
            }

            return this;
        }
    }

    private class MovieListModel extends AbstractListModel<Movie> {

        private MovieManager movieManager;

        public MovieListModel(MovieManager movieManager) {
            this.movieManager = movieManager;
        }

        public void addElement(final String element) {
            changeMovie(movieManager.addMovie(element));
            int interval = getSize() == 0 ? getSize() : getSize() - 1;
            fireIntervalAdded(this, interval, interval);
        }

        @Override
        public int getSize() {
            return movieManager.getMovieList().size();
        }

        @Override
        public Movie getElementAt(int index) {
            return movieManager.getMovieList().get(index);
        }
    }

    private class MovieSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {

            if (!e.getValueIsAdjusting()) {

                final Movie movie = ((JList<Movie>) e.getSource()).getSelectedValue();

                if (movie != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            changeMovie(movie);
                        }
                    });
                }
            }
        }
    }

}