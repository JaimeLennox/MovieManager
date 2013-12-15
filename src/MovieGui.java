import com.omertron.themoviedbapi.model.MovieDb;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.EventQueue;
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
    private JLabel imageLabel;

    private MovieManager movieManager = new MovieManager();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        frame.setSize(600, 400);
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
        frame.getContentPane().add(movieListPanel, "grow");
        movieListPanel.setLayout(new MigLayout("", "[grow]", "[grow]"));

        movieListModel = new MovieListModel(movieManager);
        movieList = new JList<>(movieListModel);
        movieList.setCellRenderer(new MovieListRenderer());
        movieList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    MovieDb movie = ((JList<MovieDb>) e.getSource()).getSelectedValue();
                    if (movie != null) {
                        try {
                            BufferedImage backdrop = ImageIO.read(movieManager.getBackdropUrl(movie));
                            imageLabel.setIcon(new ImageIcon(backdrop));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        movieListScrollPane = new JScrollPane(movieList);
        movieListPanel.add(movieListScrollPane, "grow");

        outputPanel = new JPanel();
        frame.getContentPane().add(outputPanel, "grow");
        outputPanel.setLayout(new MigLayout("", "[grow]","[grow]"));

        imageLabel = new JLabel();
        outputPanel.add(imageLabel, "grow");
    }

    private void addMovie() {
        String name = movieEnterTextField.getText();

        if (!name.isEmpty()) {
            movieListModel.addElement(name);
        }
    }

    private class MovieListRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

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
            movieManager.addMovie(element);
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

}