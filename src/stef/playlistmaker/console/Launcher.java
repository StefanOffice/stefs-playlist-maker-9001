package stef.playlistmaker.console;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Launcher {
    
    public static void main(String[] args) {
        //for reading user input from console
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Stef's Playlist Maker 9001!");
        String choice = null;
        List<String> options = List.of("1", "2", "q");
        //give the user a choice of options
        do {
            if(choice != null)
                System.out.println("Invalid option selected.");
            System.out.println("Select an option:");
            System.out.println("1 - Create a playlist for the specified directory.");
            System.out.println("2 - Create a playlist for each subfolder(only direct subfolders, not recursive) of the specified directory.");
            System.out.println("q - Exit");
            choice = scanner.nextLine();
        }while(!options.contains(choice.trim().toLowerCase()));
        
        if(choice.equals("q")){
            System.out.println("Program terminated...");
            return;
        }
        
        System.out.println("Please enter the absolute path to the source folder: ");
        //read the user input
        String sourceString = scanner.nextLine();
        
        //check if the path user entered leads to an existing directory
        File sourceDir = new File(sourceString);
        if (!sourceDir.isDirectory()) {
            System.out.println("Wrong path, specified directory not found");
            //if not stop the program
            return;
        }
        
        System.out.println("Please enter the absolute path to the location you wish to save the playlist(s) to: ");
        //read the user input
        String saveString = scanner.nextLine();
        //check if the path leads to a file
        File saveDir = new File(saveString);
        if (saveDir.isFile()) {
            System.out.println("Wrong path, can't save to a file, specify a save directory\n" +
                    "If folder doesn't exist, it will be created");
            //if yes stop the program
            return;
        }
        
        //create the folder if it doesn't exist
        if(!saveDir.exists()){
            saveDir.mkdirs();
        }
    
        //if user selected option 1 just create a playlist
        if(choice.trim().equals("1")) {
            PlaylistMaker.createAPlaylist(sourceDir, saveDir, sourceDir.getName());
            System.out.println("Playlist: " + sourceDir.getName() + " created, and saved to " + saveDir);
        
        } else if(choice.trim().equals("2")) {
            String title = sourceDir.getAbsolutePath().substring(sourceDir.getAbsolutePath().lastIndexOf("\\") + 1);
            //combine the playlists
            PlaylistMaker.createPlaylistsForSubFolders(sourceDir, saveDir, title);
        }
    }
    
}
