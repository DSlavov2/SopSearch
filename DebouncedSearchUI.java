package SopSearch;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

public class DebouncedSearchUI extends JFrame {

    private static final long DEBOUNCE_MS = 500;

    private final JTextField searchField = new JTextField();
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> resultList = new JList<>(listModel);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    private ScheduledFuture<?> pendingTask;

    // Only 5 words
    private final List<String> words = List.of(
            "apple",
            "banana",
            "computer",
            "school",
            "java");

    public DebouncedSearchUI() {
        setTitle("Basic Debounced Search");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        resultList.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(resultList), BorderLayout.CENTER);

        add(panel);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                onUserInput();
            }

            public void removeUpdate(DocumentEvent e) {
                onUserInput();
            }

            public void changedUpdate(DocumentEvent e) {
                onUserInput();
            }
        });
    }

    private void onUserInput() {
        String query = searchField.getText().toLowerCase();

        if (pendingTask != null && !pendingTask.isDone()) {
            pendingTask.cancel(false);
        }

        pendingTask = scheduler.schedule(() -> searchExecutor.submit(() -> performSearch(query)),
                DEBOUNCE_MS,
                TimeUnit.MILLISECONDS);
    }

    private void performSearch(String query) {
        List<String> results = words.stream()
                .filter(word -> word.contains(query))
                .toList();

        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            results.forEach(listModel::addElement);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DebouncedSearchUI().setVisible(true));
    }
}
