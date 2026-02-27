package com.studentrecords;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin controller: shows high-level stats and provides maintenance actions.
 */
public class AdminController {

    @FXML private TableView<ProgramCount> programTable;
    @FXML private TableView<YearCount> yearTable;
    @FXML private Label totalStudentsLabel;

    private final Database db = new Database();

    private final ObservableList<ProgramCount> programCounts =
        FXCollections.observableArrayList();
    private final ObservableList<YearCount> yearCounts =
        FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            List<Object[]> byProgram = db.countByProgram();
            List<Object[]> byYear = db.countByYear();

            programCounts.clear();
            int total = 0;
            for (Object[] row : byProgram) {
                String program = (String) row[0];
                int count = (Integer) row[1];
                total += count;
                programCounts.add(new ProgramCount(program, count));
            }

            yearCounts.clear();
            for (Object[] row : byYear) {
                int year = (Integer) row[0];
                int count = (Integer) row[1];
                yearCounts.add(new YearCount(year, count));
            }

            programTable.setItems(programCounts);
            yearTable.setItems(yearCounts);
            totalStudentsLabel.setText("Total students: " + total);
        } catch (SQLException e) {
            showError("Error loading statistics", e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        loadStats();
    }

    @FXML
    private void onBackupDatabase() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Backup Database");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Database Files", "*.db"));
        chooser.setInitialFileName("student_records_backup.db");
        File dst = chooser.showSaveDialog(getWindow());
        if (dst == null) return;

        File src = new File(System.getProperty("user.dir"), "student_records.db");
        if (!src.exists()) {
            showError("Backup Error", "Database file not found: " + src.getAbsolutePath());
            return;
        }

        try {
            copyFile(src, dst);
            showInfo("Backup Complete", "Database backed up to:\n" + dst.getAbsolutePath());
        } catch (Exception e) {
            showError("Backup Error", e.getMessage());
        }
    }

    @FXML
    private void onClearAll() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Clear All Records");
        confirm.setHeaderText("This will permanently delete all student records.");
        confirm.setContentText("Are you sure you want to continue?");
        confirm.initOwner(getWindow());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    db.deleteAllStudents();
                    loadStats();
                    showInfo("Records Cleared",
                        "All student records have been deleted.\n\n" +
                        "If this was a mistake, restore from a backup.");
                } catch (SQLException e) {
                    showError("Error Deleting Records", e.getMessage());
                }
            }
        });
    }

    private void copyFile(File src, File dst) throws Exception {
        try (FileChannel in = new FileInputStream(src).getChannel();
             FileChannel out = new FileOutputStream(dst).getChannel()) {
            long size = in.size();
            long transferred = 0;
            while (transferred < size) {
                transferred += in.transferTo(transferred, size - transferred, out);
            }
        }
    }

    private Window getWindow() {
        return totalStudentsLabel.getScene() != null
            ? totalStudentsLabel.getScene().getWindow()
            : null;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    /** Row for program statistics. */
    public static class ProgramCount {
        private final String program;
        private final int count;

        public ProgramCount(String program, int count) {
            this.program = program;
            this.count = count;
        }

        public String getProgram() {
            return program;
        }

        public int getCount() {
            return count;
        }
    }

    /** Row for year statistics. */
    public static class YearCount {
        private final int year;
        private final int count;

        public YearCount(int year, int count) {
            this.year = year;
            this.count = count;
        }

        public int getYear() {
            return year;
        }

        public int getCount() {
            return count;
        }
    }
}

