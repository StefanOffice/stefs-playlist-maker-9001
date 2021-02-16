package stef.playlistmaker.console;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;
import java.util.Scanner;

public class PlaylistMaker {
    
    public static void main(String[] args) {
        //for reading user input from console
        Scanner scanner = new Scanner(System.in);
    
        System.out.println("Welcome to Stef's Playlist Maker 9001!");
        System.out.println("Please enter the absolute path to the folder containing the files for the playlist: ");
        String pathLine = scanner.nextLine();
    
        File readDir = new File(pathLine);
        if (!readDir.isDirectory()) {
            System.out.println("Wrong path, specified directory not found");
            return;
        }
    
        System.out.println("Please enter the absolute path to the location you wish to save the playlist to: ");
        String saveString = scanner.nextLine();
        File saveDir = new File(saveString);
        if (saveDir.isFile()) {
            System.out.println("Wrong path, can't save to a file, specify a save directory\n" +
                    "If folder doesn't exist, it will be created");
            return;
        }
    
        System.out.println("Please enter the name for the playlist (or 'quit' to exit): ");
        String title = scanner.nextLine();
        while(!title.trim().matches("^[a-zA-Z0-9\s]{1,15}$")){
            System.out.println("Title can only contain 1-15 alphanumeric characters.");
            System.out.println("Please enter the name for the playlist (or 'quit' to exit): ");
            title = scanner.nextLine();
        }
        
        //if user entered 'quit' it will pass the regex check but cancel creating the playlist
        if(title.trim().equalsIgnoreCase("quit"))
            return;
    
        FilenameFilter filter = (File dir, String name) -> name.matches(".*\\.mp4");
        File[] files = readDir.listFiles(filter);
    
        
        for (File current : files) {
            System.out.println(current.getAbsolutePath());
        }
        
        
        
        
    }
    
    
}
