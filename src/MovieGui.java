import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

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

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

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
        movieList.addMouseListener(new ListAction(movieList, new Action() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(movieList.getSelectedValue().getMovieFile());
                } catch (IOException e1) {
                    MovieManager.LOGGER.log(Level.WARNING, "Could not play movie.");
                }
            }

            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {
            }

            @Override
            public void setEnabled(boolean b) {
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
            }
        }));
        movieListScrollPane = new JScrollPane(movieList);
        movieListPanel.add(movieListScrollPane, "grow");

        outputPanel = new JPanel();
        frame.getContentPane().add(outputPanel, "grow");

        createMoviePanel();

        frame.pack();
    }

    private void addMovie() {
        String name = movieEnterTextField.getText();

        if (!name.isEmpty()) {
            movieListModel.addElement(name, null, true);
        }
    }

    private void scanForMovies(File folder) {

        for (final File file : folder.listFiles()) {

            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    if (file.isFile() && isMovieFile(file)) {
                        synchronized (this) {
                            // Remove extensions from name and add to model.
                            movieListModel.addElement(getMovieName(file.getName()),
                                    file, false);
                        }
                    }
                    // Recurse into other directories.
                    else if (file.isDirectory()) {
                        scanForMovies(file);
                    }
                }
            });
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

        public void addElement(String element, File movieFile, boolean switchToMovie) {

            Movie movie = movieManager.addMovie(element, movieFile);
            if (switchToMovie) {
                changeMovie(movie);
            }

            int listSize = getSize();
            int interval = listSize == 0 ? listSize : listSize - 1;
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

    private class ListAction extends MouseAdapter {

        private JList list;
        private KeyStroke keyStroke;

        public ListAction(JList list, Action action) {
            this.list = list;
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

            //  Add the KeyStroke to the InputMap.
            InputMap inputMap = list.getInputMap();
            inputMap.put(keyStroke, keyStroke);

            //  Add the Action to the ActionMap.
            setAction(action);

            //  Handle mouse double click.
            list.addMouseListener( this );
        }

        public void setAction(Action action) {
            list.getActionMap().put(keyStroke, action);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                Action action = list.getActionMap().get(keyStroke);

                if (action != null) {
                    ActionEvent event = new ActionEvent(list,
                            ActionEvent.ACTION_PERFORMED, "");
                    action.actionPerformed(event);
                }
            }
        }

    }

}