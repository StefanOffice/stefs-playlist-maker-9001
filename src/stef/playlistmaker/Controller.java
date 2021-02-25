package stef.playlistmaker;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import stef.playlistmaker.console.PlaylistMaker;

import java.io.File;
import java.util.Optional;

public class Controller {
    
    //connect to all the control objects created in main-window.fxml
    @FXML
    private BorderPane mainPane;
    @FXML
    private Button btnSelectSourceDir;
    @FXML
    private Button btnSelectSaveDir;
    @FXML
    private TextField txtFieldSourceDir;
    @FXML
    private TextField txtFieldSaveDir;
    @FXML
    private TextField txtFieldName;
    @FXML
    private Label lblInfo;
    @FXML
    private Label lblName;
    @FXML
    private RadioButton btnSingleOption;
    @FXML
    private RadioButton btnMultiOption;
    
    //save last opened locations for easier folder selection
    String lastOpenedSourceDir;
    String lastOpenedSaveDir;
    
    @FXML
    public void handleSelectDirButtons(ActionEvent event){
        
        //Create a directory chooser dialog window and set initial dir based on last browsed(if there was any)
        DirectoryChooser dirChooser = new DirectoryChooser();
        if(event.getSource() == btnSelectSourceDir && lastOpenedSourceDir != null)
                dirChooser.setInitialDirectory(new File(lastOpenedSourceDir));
        else if(event.getSource() == btnSelectSaveDir && lastOpenedSaveDir != null)
                dirChooser.setInitialDirectory(new File(lastOpenedSaveDir));
        
        
        //Show the dialog and get a reference to the chosen directory
        File chosenDirectory = dirChooser.showDialog(mainPane.getScene().getWindow());
        
        if(chosenDirectory != null && chosenDirectory.isDirectory()){
            if(event.getSource() == btnSelectSourceDir) {
                //write the absolute path to the source dir text field
                txtFieldSourceDir.setText(chosenDirectory.getAbsolutePath());
                //remember what was opened last
                lastOpenedSourceDir = chosenDirectory.getAbsolutePath();
            } else if(event.getSource() == btnSelectSaveDir) {
                //write the absolute path to the save dir text field
                txtFieldSaveDir.setText(chosenDirectory.getAbsolutePath());
                //remember what was opened last
                lastOpenedSaveDir = chosenDirectory.getAbsolutePath();
            }
        }
    }
    
    @FXML
    public void handleCreateBtn(){
        //check if all the required data is valid before running PlaylistMaker
        //if not report it to the user through info label.
        if(txtFieldSourceDir.getText().trim().equals("")) {
            lblInfo.setText("Please choose a source directory.");
            return;
        }else if(txtFieldSaveDir.getText().trim().equals("")){
            lblInfo.setText("Please choose a save location.");
            return;
        }else if(txtFieldName.getText().trim().equals("") || !txtFieldName.getText().matches("^[a-zA-Z0-9\\s]+$")){
            lblInfo.setText("Please choose a valid name. A name can contain alphanumeric characters or white space.");
            return;
        }
        
        File sourceDir = new File(txtFieldSourceDir.getText());
        File saveDir = new File(txtFieldSaveDir.getText());
        String name = txtFieldName.getText().trim();
        
        if(btnSingleOption.isSelected()){
            lblInfo.setText("Creating a playlist from files in " + sourceDir.getAbsolutePath()
                    + "\nAnd saving it to " + saveDir.getAbsolutePath());
            
            //setup and open up a confirmation dialog and prompt the user to confirm action
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Create a new playlist");
            alert.setHeaderText(String.format("Create playlist named %s in : %s", name,  sourceDir.getAbsolutePath()));
            alert.setContentText("Are you sure? Press 'OK' to continue of 'cancel' to go back to main window.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            
            //report status back to the user based on the success of the chosen action
            if (result.isPresent() && (result.get() == ButtonType.OK)) {
                if(PlaylistMaker.createAPlaylist(sourceDir, saveDir, name)){
                    lblInfo.setText("Playlist created and saved to " + saveDir.getAbsolutePath());
                } else {
                    lblInfo.setText("Something went wrong, please select a valid source folder, with at least 1 eligible media file.");
                }
            } else {
                lblInfo.setText("Creating the playlist was canceled...");
            }
            
            
        }else if(btnMultiOption.isSelected()){
            lblInfo.setText("Creating a playlist for each sub-folder in " + sourceDir.getAbsolutePath()
                    + "\nAnd saving them to " + saveDir.getAbsolutePath());
    
            //setup and open up a confirmation dialog and prompt the user to confirm action
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Create new playlists");
            alert.setHeaderText(String.format("Create playlists for each sub-folder in : %s",  sourceDir.getAbsolutePath()));
            alert.setContentText("Are you sure? Press 'OK' to continue or 'cancel' to go back to main window.");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            
            //report status back to the user based on the success of the chosen action
            if (result.isPresent() && (result.get() == ButtonType.OK)) {
                if(PlaylistMaker.createPlaylistsForSubFolders(sourceDir,saveDir, name)) {
                    lblInfo.setText("Playlists created and saved to " + saveDir.getAbsolutePath());
                } else {
                    lblInfo.setText("Something went wrong, please select a directory with at least 1 sub-folder that contains eligible media files.");
                }
            } else {
                lblInfo.setText("Creating the playlists was canceled...");
            }
        }
    }
    
    @FXML
    public void handleOptionSelectionBtn(Event event){
        if(event.getSource() == btnSingleOption)
            lblName.setText("Playlist name :");
        else
            lblName.setText("Folder name :");
    }
    
    @FXML
    public void handleCancelBtn(Event event){
        Platform.exit();
    }
}
