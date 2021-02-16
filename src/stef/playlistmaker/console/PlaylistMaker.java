package stef.playlistmaker.console;

import java.io.*;
import java.util.Scanner;

public class PlaylistMaker {
    
    public static void main(String[] args) {
        //for reading user input from console
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Welcome to Stef's Playlist Maker 9001!");
        System.out.println("Please enter the absolute path to the folder containing the files for the playlist: ");
        //read the user input
        String pathLine = scanner.nextLine();
        
        //check if the path user entered leads to an existing directory
        File readDir = new File(pathLine);
        if (!readDir.isDirectory()) {
            System.out.println("Wrong path, specified directory not found");
            //if not stop the program
            return;
        }
        
        System.out.println("Please enter the absolute path to the location you wish to save the playlist to: ");
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
        
        System.out.println("Please enter the name for the playlist (or 'quit' to exit): ");
        //read the user input
        String title = scanner.nextLine();
        //check if user input is valid. If not, offer a chance to re-enter a different name.
        while (!title.trim().matches("^[a-zA-Z0-9\s]{1,15}$")) {
            System.out.println("Title can only contain 1-15 alphanumeric characters.");
            System.out.println("Please enter the name for the playlist (or 'quit' to exit): ");
            title = scanner.nextLine();
        }
        
        //if user entered 'quit' it will pass the regex check but cancel creating the playlist
        if (title.trim().equalsIgnoreCase("quit"))
            return;
        
        //filter the files by the extension (only add .mp4 files to the playlist)
        FilenameFilter filter = (File dir, String name) -> name.matches(".*\\.mp4");
        //save the list of eligible files
        File[] files = readDir.listFiles(filter);
        
        //print eligible files to console
        for (File current : files) {
            System.out.println(current.getAbsolutePath());
        }
        
        //create a new file for the playlist
        File playlist = new File(saveString + "\\" + title + ".xspf");
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(playlist));) {
            //write starting info
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<playlist xmlns=\"http://xspf.org/ns/0/\" xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\" version=\"1\">\n");
            bw.write("\t<title>Playlist</title>\n");
            bw.write("\t<trackList>\n");
            
            int trackCount = 0;
            //for each of the files write the information and path to the xspf file
            for (File currentFile : files) {
                //replace the special characters so vlc can read the file without errors
                String currentFilePath = currentFile.getAbsolutePath()
                        .replace(" ", "%20")
                        .replace("\\", "/");
                
                //track specific information
                bw.write("\t\t<track>\n");
                bw.write("\t\t\t<location>file:///" + currentFilePath + "</location>\n");
                bw.write("\t\t\t<duration></duration>\n");
                bw.write("\t\t\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\n");
                bw.write(String.format("\t\t\t\t<vlc:id>%d</vlc:id>\n", trackCount++));
                bw.write("\t\t\t</extension>\n");
                bw.write("\t\t</track>\n");
            }
            bw.write("\t</trackList>\n");
            
            bw.write("\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\n");
            //for each track write it's id to xspf file
            trackCount = 0;
            for (File file : files) {
                bw.write(String.format("\t\t<vlc:item tid=\"%d\"/>\n", trackCount++));
            }
            
            bw.write("\t</extension>\n");
            bw.write("</playlist>");
            
        } catch (IOException e) {
            //if any error occurs give user the info about it and auto-terminate the app...
            System.out.println("Something bad happened, please try re-running the program.");
            e.printStackTrace();
        }
        
        //inform the user of the result
        if(playlist.exists())
            System.out.println("Playlist created successfully: " + playlist.getAbsolutePath());
        else
            System.out.println("Something went wrong...");
    }
    
}
