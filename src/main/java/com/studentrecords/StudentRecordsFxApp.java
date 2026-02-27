package com.studentrecords;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point with CSS-themed UI.
 */
public class StudentRecordsFxApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/studentrecords/main-view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 1040, 620);
        scene.getStylesheets().add(
            getClass().getResource("/com/studentrecords/student-theme.css").toExternalForm()
        );

        stage.setTitle("Student Records Manager");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

