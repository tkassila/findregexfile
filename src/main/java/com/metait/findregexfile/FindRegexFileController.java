package com.metait.findregexfile;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindRegexFileController {
    @FXML
    private Button buttonSelectDirectory;
    @FXML
    private Label labelMsg;
    @FXML
    private TextField textFieldSelectDirectory;
    @FXML
    private TextField textFieldFileName;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private CheckBox checkBoxNotInFiles;
    @FXML
    private CheckBox checkBoxSeekInDirs;
    @FXML
    private CheckBox checkBoxNoRegex;
    @FXML
    private CheckBox checkBoxListOnlyFiles;
    @FXML
    private Button buttonStartSearch;
    @FXML
    private ListView<String> listFoundedFiles;
    @FXML
    private Button buttonRead;
    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonFindFirstMatch;
    @FXML
    private Button buttonFindNext;
    @FXML
    private Button buttonFindPrev;
    @FXML
    private TextArea textAreaFileContent;
    @FXML
    private SplitPane splitPane;
    private DirectoryChooser dirChooser = new DirectoryChooser();
    private Stage primaryStage;
    private File fileDirChooser = null;
    private ExtensionsFilter selectedFilter = null;
    private String [] arrFileExtenstions = null;
    private boolean bListAllFiles = false;
    private String strRegexSearch = null;
    private Pattern patternSearch = null;
    private ObservableList<String> listResultItems = FXCollections.observableArrayList();
    // private List<String> stringList  = new ArrayList<>();
    private File selectedFile = null;
    private boolean b_textAreaFileContent = false;
    private boolean yes_selected = false;
    private boolean no_selected = false;
    private boolean cancel_selected = false;
    private boolean bHasWildCards = false;
    private Matcher matcheContent = null;
    private int iCurrentMatchGroup = -1;
    private Stream<MatchResult> matchResultStream = null;
    private MatchResult [] arrMatchResults = null;

    @FXML
    public void initialize() {
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.5);

      // SplitPane.Divider divider = splitPane.getDividers().get(0);
        dirChooser.setTitle("Open search directory to seek files and text or regex");
        dirChooser.setInitialDirectory(new File("."));
        buttonStartSearch.setDisable(true);
        listFoundedFiles.setItems(listResultItems);
        textAreaFileContent.setStyle("-fx-font-weight: bold; fx-highlight-fill: lightgray; -fx-highlight-text-fill: firebrick; -fx-font-size: 14px;");
        listFoundedFiles.setStyle("-fx-font-weight: bold");


        disAblesomeUiControls(true);

        listFoundedFiles.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 2) {
                    //Use ListView's getSelected Item
                    String selected = listFoundedFiles.getSelectionModel().getSelectedItem();
                    if (selected == null || selected.trim().length()==0)
                        return;
                    File f = new File(selected);
                    if (!f.exists())
                        return;
                    boolean bContinue = true;
                    if (b_textAreaFileContent) {
                        bContinue = getShouldContinueAfterChangeFileContent();
                        if (!bContinue)
                            return;
                    }
                    readFileIntoTextArea(f);
                }
            }
        });

        textFieldSelectDirectory.textProperty().addListener((obs, oldText, newText) -> {
            // do what you need with newText here, e.g.
           someWidgetChanged_DisAble_OrNot_StartSerchButton();
        });
        textFieldFileName.textProperty().addListener((obs, oldText, newText) -> {
            // do what you need with newText here, e.g.
            someWidgetChanged_DisAble_OrNot_StartSerchButton();
        });
        textFieldSearch.textProperty().addListener((obs, oldText, newText) -> {
            // do what you need with newText here, e.g.
            someWidgetChanged_DisAble_OrNot_StartSerchButton();
        });

        textAreaFileContent.textProperty().addListener((obs, oldText, newText) -> {
            // do what you need with newText here, e.g.
            if (oldText == null && newText == null)
                return;
            if (oldText.equals(newText))
                return;
            if (!b_textAreaFileContent && oldText.isEmpty() && !oldText.equals(newText))
                return;
            b_textAreaFileContent = true;
            buttonSave.setDisable(false);;
        });

        /*
        listResultItems.add("1");
        listResultItems.add("11");
        listResultItems.add("111");
         */
        /*
        checkBoxListOnlyFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                someWidgetChanged_DisAble_OrNot_StartSerchButton();
            }
        });
         */
    }

    public void setPrimaryStage(Stage stage)
    {
        primaryStage = stage;
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                appIsClosing();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    protected void appIsClosing()
    {
    }

    @FXML
    protected void pressed_buttonSelectDirectory() {
        System.out.println("pressed_buttonSelectDirectory");
        if (fileDirChooser != null)
            dirChooser.setInitialDirectory(fileDirChooser);
        File selectedDirectory = dirChooser.showDialog(this.primaryStage);
        if (selectedDirectory != null && selectedDirectory.exists()) {
            fileDirChooser = selectedDirectory;
            textFieldSelectDirectory.setText(selectedDirectory.getAbsolutePath());
            someWidgetChanged_DisAble_OrNot_StartSerchButton();
        }
    }

    private boolean isBlandOrEmptyOrNullString(String str)
    {
        if (str == null)
            return true;
        if (str.isBlank())
            return true;
        if (str.isEmpty())
            return true;
        return false;
    }

    @FXML
    protected void someWidgetChanged_DisAble_OrNot_StartSerchButton()
    {
        System.out.println("someWidgetChanged");
        buttonStartSearch.setDisable(false);
        String str_textFieldSelectDirectory = textFieldSelectDirectory.getText();
        String str_textFieldFileName = textFieldFileName.getText();
        String str_textFieldSearch = textFieldSearch.getText();

        if (!checkBoxListOnlyFiles.isSelected())
        {
           if (isBlandOrEmptyOrNullString(str_textFieldSearch)
                    || (isBlandOrEmptyOrNullString(str_textFieldSelectDirectory)
                    || (isBlandOrEmptyOrNullString(str_textFieldFileName))))
                buttonStartSearch.setDisable(true);
        }
        else
        if (checkBoxListOnlyFiles.isSelected())
        {
            if (isBlandOrEmptyOrNullString(str_textFieldSelectDirectory)
                /* || isBlandOrEmptyOrNullString(textFieldFileName) */)
                buttonStartSearch.setDisable(false);
        }

        File dir = new File(textFieldSelectDirectory.getText());
        if (!dir.exists())
        {
            labelMsg.setText("Directory '" +textFieldSelectDirectory.getText() +"' does not exist!");
            buttonStartSearch.setDisable(true);
        }
        else
        if (!dir.isDirectory())
        {
            labelMsg.setText("Directory '" +textFieldSelectDirectory.getText() +"' is a file, not directory!");
            buttonStartSearch.setDisable(true);
        }
        else
        {
            ;
        }
    }

    @FXML
    protected void pressed_checkBoxListOnlyFiles() {
        System.out.println("pressed_checkBoxListOnlyFiles");
        if (checkBoxListOnlyFiles.isSelected())
        {
            boolean bValue = true;
            checkBoxNotInFiles.setDisable(bValue);
            checkBoxNoRegex.setDisable(bValue);
            textFieldSearch.setDisable(bValue);
        }
        else
        {
            boolean bValue = false;
            checkBoxNotInFiles.setDisable(bValue);
            checkBoxNoRegex.setDisable(bValue);
            textFieldSearch.setDisable(bValue);
        }
        someWidgetChanged_DisAble_OrNot_StartSerchButton();
    }

    @FXML
    protected void pressed_checkBoxNoRegex() {
        System.out.println("pressed_checkBoxNoRegex");
        someWidgetChanged_DisAble_OrNot_StartSerchButton();
    }

    @FXML
    protected void pressed_checkBoxSeekInDirs() {
        System.out.println("pressed_checkBoxSeekInDirs");
        someWidgetChanged_DisAble_OrNot_StartSerchButton();
    }

    @FXML
    protected void pressed_checkBoxNotInFiles() {
        System.out.println("pressed_checkBoxNotInFiles");
        someWidgetChanged_DisAble_OrNot_StartSerchButton();
    }

    @FXML
    protected void pressed_buttonStartSearch() {
        System.out.println("pressed_buttonStartSearch");
        startSearchFilesAndText();
    }

    protected void startSeekInThisDir(File fDir)
    {
        File [] files = null;
        if (bListAllFiles)
        {
    //        selectedFilter = null
            if (selectedFilter != null) {
                Path dir = Paths.get(fDir.getParentFile().getAbsolutePath());
                selectedFilter.setPathDir(dir);
                files = fDir.listFiles(selectedFilter);
            }
            else
                files = fDir.listFiles();
        }
        else
        {
            Path dir = Paths.get(fDir.getParentFile().getAbsolutePath());
            selectedFilter.setPathDir(dir);
            files = fDir.listFiles(selectedFilter);
        }

        // search text inside these files:
        for (File f : files)
        {
            if (f.isFile())
                searchThisFile(f);
        }

        if (checkBoxSeekInDirs.isSelected()) {
            files = fDir.listFiles();
            for (File f : files) {
                if (f.isDirectory())
                    startSeekInThisDir(f);
            }
        }
    }

    private void disAblesomeUiControls(boolean bvalue)
    {
        buttonRead.setDisable(bvalue);
        if (bvalue)
            buttonSave.setDisable(bvalue);
        textAreaFileContent.setDisable(bvalue);
        if (!bvalue) {
            /*
            if (textFieldSearch.getText().trim().length()>0) {
                buttonFindNext.setDisable(bvalue);
                buttonFindPrev.setDisable(bvalue);
                buttonFindFirstMatch.setDisable(bvalue);
            }
             */
        }
        else
        {
            buttonFindNext.setDisable(bvalue);
            buttonFindPrev.setDisable(bvalue);
            buttonFindFirstMatch.setDisable(bvalue);
        }
    }
    private void startSearchFilesAndText()
    {
        selectedFile = null;
        listResultItems.clear();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                labelMsg.setText("Search started....");
            }
        });

        textAreaFileContent.setText("");
        b_textAreaFileContent = false;

        disAblesomeUiControls(true);

        File fDir = new File(textFieldSelectDirectory.getText());
        if (!fDir.exists())
        {
            labelMsg.setText("Directory '" +fDir.getAbsolutePath() +"' does not exist!");
            return;
        }
        if (!fDir.isDirectory())
        {
            labelMsg.setText("Directory '" +fDir.getAbsolutePath() +"' is not a directory!");
            return;
        }

        bHasWildCards = false;
        bListAllFiles = checkBoxListOnlyFiles.isSelected();
        arrFileExtenstions = getFileFilterArray();

        File [] files = null;
        if (bListAllFiles)
        {
            selectedFilter = null;
            files = fDir.listFiles();
        }

        // arrFileExtenstions = getFileFilterArray();
        if (arrFileExtenstions != null) {
            if (bHasWildCards) {
                selectedFilter = new ExtensionsFilter(Arrays.asList(arrFileExtenstions));
            }
            else
                selectedFilter = new ExtensionsFilter(arrFileExtenstions);
//          files = fDir.listFiles(selectedFilter);
        }
        else {
            files = fDir.listFiles();
            selectedFilter = null;
            bListAllFiles = true;
        }

        if (bListAllFiles && (files == null || files.length == 0))
        {
            labelMsg.setText("No files to search!");
            return;
        }

        strRegexSearch = textFieldSearch.getText();
        if (strRegexSearch != null && strRegexSearch.trim().length()>0 && !checkBoxListOnlyFiles.isSelected())
        {
            if (checkBoxNoRegex.isSelected()) {
                strRegexSearch = Pattern.quote(strRegexSearch);
            }

            try {
                patternSearch = Pattern.compile(strRegexSearch);
            }catch (PatternSyntaxException pse){
                labelMsg.setText(pse.getMessage());
                return;
            }
        }
        else
        {
            patternSearch = null;
        }

        startSeekInThisDir(fDir);
        if (listFoundedFiles.getItems().size()>0) {
            disAblesomeUiControls(false);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    labelMsg.setText("The search is done.");
                }
            });
        }
        else
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    labelMsg.setText("No files founded.");
                }
            });
        /*
        // search text inside tgese files:
        for (File f : files)
        {
            if (f.isFile())
              searchThisFile(f);
        }

        if (checkBoxSeekInDirs.isSelected()) {
            files = fDir.listFiles();
            for (File f : files) {
                if (f.isDirectory() && checkBoxSeekInDirs.isSelected())
                    searchThisDir(f);
            }
        }
        */
        /*
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // listResultItems.addAll(stringList);
                // listFoundedFiles.setItems(listResultItems);
                listFoundedFiles.refresh();
            }
        });
*/
    }
    private void searchThisDir(File fDir) {
        File[] p_files = null;
        if (bListAllFiles) {
            p_files = fDir.listFiles();
        }
        else {
            if (selectedFilter != null) {
                Path dir = Paths.get(fDir.getParentFile().getAbsolutePath());
                selectedFilter.setPathDir(dir);
                p_files = fDir.listFiles(selectedFilter);
            }
            else
                p_files = fDir.listFiles();
            for (File f : p_files) {
                if (f.isFile())
                    searchThisFile(f);
            }

            p_files = fDir.listFiles();
            for (File f : p_files) {
                if (f.isDirectory())
                    searchThisDir(f);
            }
        }
    }

    private boolean isRigthFileName(File f)
    {
        return true;
        /*
        boolean ret = false;
        if (f != null && f.getName().trim().length()>0)
        {
            if (ExtensionsFilter.acceptFileName(f, arrFileExtenstions))
                return true;
        }
        return ret;
         */
    }

    private void searchThisFile(File f)
    {
       if (f.isFile())
       {
            String strFName = textFieldFileName.getText();
            if (checkBoxListOnlyFiles.isSelected() && isBlandOrEmptyOrNullString(strFName))
                listResultItems.add(f.getAbsolutePath());
            else
            if (checkBoxListOnlyFiles.isSelected() && !isBlandOrEmptyOrNullString(strFName)) {
                // which is allways true! (because own file filter class, selecting is done all ready:
                if (isRigthFileName(f))
                    listResultItems.add(f.getAbsolutePath());
            }
            else
            {
                try {
                    Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
                    String s = Files.readString(path, StandardCharsets.UTF_8);
                    if (!s.isEmpty() && !s.isBlank()) {
                        seekSearchTextFrom(f, s);
                    }
                }catch (java.io.IOException ioe){
                    ; // cannot read this fiile f, ok
                }catch (Exception e){
                    labelMsg.setText(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void seekSearchTextFrom(File f, String s)
    {
        Matcher matcher = patternSearch.matcher(s);
        boolean bMatch = matcher.find();
        if (checkBoxNotInFiles.isSelected()) {
            if (!bMatch) {
                listResultItems.add("" + f.getAbsoluteFile());
            }
        }
        else
        if (bMatch) {
            listResultItems.add("" + f.getAbsoluteFile());
        }
    }
    
    private boolean constainsWildCards(String textFName)
    {
        boolean ret = false;
        if (textFName != null && textFName.trim().length() > 0 )
        {
            int indQuestionMark = textFName.indexOf("?");
            int indQuestionJoker = textFName.indexOf("*");
            if (indQuestionMark > -1 || indQuestionJoker > -1)
                return true;
        }
        return ret;
    }
    private String [] getFileFilterArray()
    {
        String [] ret = null;
        String textFName = textFieldFileName.getText();
        bHasWildCards = constainsWildCards(textFName);
        if (textFName == null || textFName.trim().length() == 0)
            return ret;
        textFName = textFName.trim();
        int indcommand = textFName.indexOf(',');
        if (indcommand == -1) {
            ret = new String[1];
            ret[0] = textFName;
            return ret;
        }
        String [] arrStr = textFName.split(",");
        ret = new String[arrStr.length];
        int i = 0;
        for(String fname : arrStr)
        {
            ret[i] = fname.trim();
            i++;
        }
        /*
        new String[]
                {".mp3", ".html",".htm", ".wav",".mp4",".aiff", ".aif", ".aifc", ".m4a", ".m4b", ".m4p", ".m4r", ".m4v", ".3gp",
                        ".flv", ".f4v", ".f4p", ".f4a", ".f4b"}; // add also html files, because this class
        // can open daisy html files to listen!
         */
        return ret;
    }

    private void readFileIntoTextArea(File f)
    {
        try {
            Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
            String s = Files.readString(path, StandardCharsets.UTF_8);
            buttonSave.setDisable(true);
            buttonRead.setDisable(true);
            buttonFindNext.setDisable(true);
            buttonFindPrev.setDisable(true);
            buttonFindFirstMatch.setDisable(true);
            selectedFile = f;
            if (!s.isEmpty() && !s.isBlank()) {
                textAreaFileContent.setText(s);
                b_textAreaFileContent = false;
                buttonRead.setDisable(false);
                boolean bvalue = false;
                if (textFieldSearch.getText().trim().length()>0) {
                    buttonFindNext.setDisable(bvalue);
                    buttonFindPrev.setDisable(bvalue);
                    buttonFindFirstMatch.setDisable(bvalue);

                    if (!checkBoxListOnlyFiles.isSelected() && patternSearch != null)
                    {
                        Matcher m = patternSearch.matcher(s);
                        matchResultStream = m.results();
                        List<MatchResult> listObjItems = matchResultStream.collect(Collectors.toUnmodifiableList());
                        int items = (listObjItems == null ? 0 : listObjItems.size());
                        if (items == 0)
                        {
                            matchResultStream = null;
                            arrMatchResults = null;
                        }
                        else
                        {
                            if (listObjItems == null || listObjItems.size() == 0)
                                return;
                            arrMatchResults = new MatchResult[listObjItems.size()];
                            arrMatchResults = listObjItems.toArray(arrMatchResults);
                        }
                    }
                }
                else
                {

                }
            }
        } catch (java.io.IOException ioe) {
            ; // cannot read this fiile f, ok
            labelMsg.setText(ioe.getMessage());
        }
    }

    private boolean getShouldContinueAfterChangeFileContent()
    {
        boolean ret = false;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Current file content is modified");
        alert.setContentText("Will you read still a new file content?");
        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
        yes_selected = false;
        no_selected = false;
        cancel_selected = false;
        alert.showAndWait().ifPresent(type -> {
            if (type == okButton) {
                yes_selected = true;
            } else if (type == noButton) {
                no_selected = true;
            } else {
                cancel_selected = true;
            }
        });
        if (yes_selected)
            return true;
        return ret;
    }

    @FXML
    protected void pressed_buttonRead() {
        System.out.println("pressed_buttonRead");
        String strSelected = listFoundedFiles.getSelectionModel().getSelectedItem();
        if (strSelected == null || strSelected.trim().length() == 0)
            return;
        File f = new File(strSelected);
        if (!f.exists())
            return;
        boolean bContinue = true;
        if (b_textAreaFileContent)
            bContinue = getShouldContinueAfterChangeFileContent();
        if (!bContinue)
            return;
        selectedFile = f;
        readFileIntoTextArea(f);
    }

    @FXML
    protected void pressed_buttonSave() {
        System.out.println("pressed_buttonSave");
        if (!b_textAreaFileContent || selectedFile == null)
            return;
        Path path = Paths.get(selectedFile.getAbsolutePath());
        String strText = this.textAreaFileContent.getText();
        if (strText == null || strText.trim().length() == 0)
            return;
        try {
            Files.write(path, strText.getBytes());
            b_textAreaFileContent = false;
            buttonSave.setDisable(true);
        } catch (java.io.IOException ioe) {
            ; // cannot read this fiile f, ok
            labelMsg.setText(ioe.getMessage());
        }
    }

    @FXML
    protected void pressed_buttonFindFirstMatch() {
        System.out.println("pressed_buttonFindFirstMatch");
        String strConntent = textAreaFileContent.getText();
        if (strConntent == null || strConntent.trim().length()==0)
            return;
        iCurrentMatchGroup = -1;
        if (arrMatchResults == null || arrMatchResults.length == 0)
            return;
        iCurrentMatchGroup = 0;
        selectTextAreaContent(iCurrentMatchGroup);
    }

    @FXML
    protected void pressed_buttonFindNext() {
        System.out.println("pressed_buttonFindNext");
        if (arrMatchResults == null || arrMatchResults.length == 0)
            return;
        if ((iCurrentMatchGroup+1) >= arrMatchResults.length)
            return;
        iCurrentMatchGroup++;
        selectTextAreaContent(iCurrentMatchGroup);
    }

    private void selectTextAreaContent( int iMachIndex)
    {
        if (iMachIndex < 0 || iMachIndex >= arrMatchResults.length)
            return;
        MatchResult mResult = arrMatchResults[iMachIndex];
        if (mResult == null)
            return;
        int iStart = mResult.start();
        int iEnd = mResult.end();
        /*
        int iLen = iEnd - iStart;
        if (iLen < 1)
            return;
         */
        textAreaFileContent.selectRange(iStart, iEnd);
    }

    @FXML
    protected void pressed_buttonFindPrev() {
        System.out.println("pressed_buttonFindPrev");
        if (arrMatchResults == null || arrMatchResults.length == 0)
            return;
        if ((iCurrentMatchGroup-1) < 0)
            return;
        iCurrentMatchGroup--;
        selectTextAreaContent(iCurrentMatchGroup);
    }
}