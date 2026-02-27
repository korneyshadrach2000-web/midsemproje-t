package com.studentrecords;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JavaFX controller: main screen with table, filters, and actions.
 */
public class MainViewController {

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> idColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, String> programColumn;
    @FXML private TableColumn<Student, Integer> yearColumn;
    @FXML private TableColumn<Student, String> gpaColumn;

    @FXML private ComboBox<String> programFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final Database db = new Database();
    private final ObservableList<Student> students = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        try {
            db.init();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }

        idColumn.setCellValueFactory(cell -> javafx.beans.property.SimpleStringProperty
            .stringExpression(new javafx.beans.property.SimpleStringProperty(cell.getValue().getId())));
        nameColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getFullName()
        ));
        emailColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getEmail()
        ));
        programColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getProgram()
        ));
        yearColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(
            cell.getValue().getYear()
        ).asObject());
        gpaColumn.setCellValueFactory(cell -> {
            Double gpa = cell.getValue().getGpa();
            return new javafx.beans.property.SimpleStringProperty(
                gpa != null ? String.format("%.2f", gpa) : ""
            );
        });

        studentTable.setItems(students);

        yearFilter.setItems(FXCollections.observableArrayList("", "1", "2", "3", "4", "5", "6"));

        searchField.textProperty().addListener((obs, oldV, newV) -> loadStudents());

        refreshProgramFilter();
        loadStudents();
    }

    private void refreshProgramFilter() {
        try {
            List<String> programs = db.getPrograms();
            Set<String> all = new LinkedHashSet<>();
            all.add("");
            all.add("CS");
            all.add("ECE");
            all.add("MATH");
            all.add("ENG");
            all.add("BIO");
            all.add("PHYS");
            all.addAll(programs);
            programFilter.setItems(FXCollections.observableArrayList(all));
            programFilter.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            programFilter.setItems(FXCollections.observableArrayList(""));
            programFilter.getSelectionModel().selectFirst();
        }
    }

    private void loadStudents() {
        String pf = programFilter.getSelectionModel().getSelectedItem();
        if (pf != null && pf.isBlank()) pf = null;

        String yStr = yearFilter.getSelectionModel().getSelectedItem();
        Integer yf = null;
        if (yStr != null && !yStr.isBlank()) {
            try {
                yf = Integer.parseInt(yStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        String search = searchField.getText();
        if (search != null && search.isBlank()) search = null;

        try {
            List<Student> list = db.listStudents(pf, yf, search);
            students.setAll(list);
            statusLabel.setText("Showing " + list.size() + " student(s).");
        } catch (SQLException e) {
            showError("Error Loading Students", e.getMessage());
        }
    }

    @FXML
    private void onAdd() {
        Student s = showStudentDialog("Add Student", null);
        if (s != null) {
            try {
                db.addStudent(s);
                refreshProgramFilter();
                loadStudents();
                statusLabel.setText("Added student " + s.getId() + ".");
            } catch (SQLException e) {
                showError("Error Adding Student", e.getMessage());
            }
        }
    }

    @FXML
    private void onEdit() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Edit", "Select a student to edit.");
            return;
        }
        Student updated = showStudentDialog("Edit Student", selected);
        if (updated != null) {
            try {
                db.updateStudent(selected.getId(), updated);
                refreshProgramFilter();
                loadStudents();
                statusLabel.setText("Updated student " + updated.getId() + ".");
            } catch (SQLException e) {
                showError("Error Updating Student", e.getMessage());
            }
        }
    }

    @FXML
    private void onDelete() {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Delete", "Select a student to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete student '" + selected.getId() + "'?");
        applyDialogStyle(confirm);
        confirm.initOwner(getWindow());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    db.deleteStudent(selected.getId());
                    refreshProgramFilter();
                    loadStudents();
                    statusLabel.setText("Deleted " + selected.getId() + ".");
                } catch (SQLException e) {
                    showError("Error Deleting Student", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("students_export.csv");
        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;

        String pf = programFilter.getSelectionModel().getSelectedItem();
        if (pf != null && pf.isBlank()) pf = null;
        String yStr = yearFilter.getSelectionModel().getSelectedItem();
        Integer yf = null;
        if (yStr != null && !yStr.isBlank()) {
            try { yf = Integer.parseInt(yStr.trim()); } catch (NumberFormatException ignored) {}
        }
        String search = searchField.getText();
        if (search != null && search.isBlank()) search = null;

        try {
            int n = Reports.exportCsv(Paths.get(file.getAbsolutePath()), pf, yf, search);
            statusLabel.setText("Exported " + n + " record(s) to " + file.getPath());
            showInfo("Export Complete", "Exported " + n + " record(s) to CSV.");
        } catch (Exception e) {
            showError("Export Error", e.getMessage());
        }
    }

    @FXML
    private void onSummary() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Summary Report");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        chooser.setInitialFileName("student_summary.txt");
        File file = chooser.showSaveDialog(getWindow());
        if (file == null) return;
        try {
            String content = Reports.generateSummaryReport(Paths.get(file.getAbsolutePath()));
            statusLabel.setText("Summary report saved to " + file.getPath());
            showInfo("Summary Report", "Report saved.\n\n" + content);
        } catch (Exception e) {
            showError("Report Error", e.getMessage());
        }
    }

    @FXML
    private void onAdmin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/studentrecords/admin-view.fxml")
            );
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Admin Panel");
            stage.initOwner(getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 800, 420);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showError("Error Opening Admin Panel", e.getMessage());
        }
    }

    private Student showStudentDialog(String title, Student existing) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        applyDialogStyle(dialog);
        dialog.initOwner(getWindow());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setStyle("-fx-padding: 10;");

        TextField idField = new TextField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        ComboBox<String> programBox = new ComboBox<>();
        programBox.setEditable(true);
        programBox.setItems(programFilter.getItems());
        Spinner<Integer> yearSpinner = new Spinner<>(1, 6, 1);
        TextField gpaField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        int row = 0;
        grid.add(new Label("Student ID *"), 0, row);
        grid.add(idField, 1, row++);
        grid.add(new Label("First name *"), 0, row);
        grid.add(firstNameField, 1, row++);
        grid.add(new Label("Last name *"), 0, row);
        grid.add(lastNameField, 1, row++);
        grid.add(new Label("Email *"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Program *"), 0, row);
        grid.add(programBox, 1, row++);
        grid.add(new Label("Year (1–6) *"), 0, row);
        grid.add(yearSpinner, 1, row++);
        grid.add(new Label("GPA (0–4)"), 0, row);
        grid.add(gpaField, 1, row++);
        grid.add(new Label("Notes"), 0, row);
        grid.add(notesArea, 1, row);

        if (existing != null) {
            idField.setText(existing.getId());
            idField.setDisable(true);
            firstNameField.setText(existing.getFirstName());
            lastNameField.setText(existing.getLastName());
            emailField.setText(existing.getEmail());
            programBox.getSelectionModel().select(existing.getProgram());
            yearSpinner.getValueFactory().setValue(existing.getYear());
            gpaField.setText(existing.getGpa() != null ? String.valueOf(existing.getGpa()) : "");
            notesArea.setText(existing.getNotes());
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                try {
                    Student validated = Validation.validateStudent(
                        idField.getText(),
                        firstNameField.getText(),
                        lastNameField.getText(),
                        emailField.getText(),
                        programBox.getEditor().getText(),
                        String.valueOf(yearSpinner.getValue()),
                        gpaField.getText(),
                        notesArea.getText(),
                        existing != null,
                        existing != null ? existing.getId() : null
                    );
                    return validated;
                } catch (ValidationException e) {
                    showError("Validation Error", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
        return dialog.getResult();
    }

    private Window getWindow() {
        return statusLabel.getScene() != null ? statusLabel.getScene().getWindow() : null;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyDialogStyle(alert);
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        applyDialogStyle(alert);
        alert.initOwner(getWindow());
        alert.showAndWait();
    }

    private void applyDialogStyle(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/studentrecords/student-theme.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
    }
}

