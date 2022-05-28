package com.metait.findregexfile;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FindRegexFileApplication extends Application {
    private Stage m_primaryStage;
    private FindRegexFileController controller = new FindRegexFileController();
    public FindRegexFileController getController()
    {
        return controller;
    }

    @Override
    public void start(Stage stage) throws IOException {

        Parameters parameters = getParameters();

        Map<String, String> namedParameters = parameters.getNamed();
        List<String> rawArguments = parameters.getRaw();
        List<String> unnamedParameters = parameters.getUnnamed();

        FXMLLoader fxmlLoader = new FXMLLoader(FindRegexFileApplication.class.getResource("findregexfile-view.fxml"));
        fxmlLoader.setController(controller);
        m_primaryStage = stage;
        controller.setPrimaryStage(m_primaryStage);
        Scene scene = new Scene(fxmlLoader.load(), 1040, 650);
        scene.getStylesheets().add("app.css");

        stage.setTitle("FindRegexFiles: list or find files after file names or content!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}