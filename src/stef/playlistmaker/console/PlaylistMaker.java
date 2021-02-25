package stef.playlistmaker.console;

import java.io.*;
import java.util.Arrays;


public class PlaylistMaker {
    
    public static boolean createAPlaylist(File sourceDir, File saveDir, String plName) {
        
        //filter the files by the extension (only add .mp4 files to the playlist)
        FilenameFilter filter = (File dir, String name) -> name.matches(".*\\.mp4");
        
        //save the list of eligible files
        File[] files = sourceDir.listFiles(filter);
        if (files.length < 1)
            return false;
    
        //if file names start with numbers then sort them based on it
        //if not leave them as is
        if(areNumerical(files))
            sort(files);
        
        //create a new file for the playlist
        File playlist = new File(saveDir.getAbsolutePath() + "\\" + plName + ".xspf");
        
        //print eligible files to console(for debugging)
        for (File current : files) {
            System.out.println(current.getAbsolutePath());
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(playlist));) {
            //write starting info
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<playlist xmlns=\"http://xspf.org/ns/0/\" xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\" version=\"1\">\n");
            bw.write("\t<title>Playlist</title>\n");
            bw.write("\t<trackList>\n");
            
            int trackCount = 0;
            //for each of the files write the information and path to the xspf file
            for (File current : files) {
                //replace the special characters so vlc can read the file without errors
                String currentFilePath = replaceSpecialChars(current.getAbsolutePath());
                
                //write track specific information
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
            System.out.println("Something bad happened, try again");
            e.printStackTrace();
        }
        
        if(playlist.exists())
            System.out.println("Playlist created successfully: " + playlist.getAbsolutePath());
        else
            System.out.println("Something went wrong...");
        
        return playlist.exists();
    }
    
    public static boolean createPlaylistsForSubFolders(File sourceDir, File saveDirParent, String playlistFolderName) {
        
        String[] folders = sourceDir.list();
        if (folders == null)
            return false;
        
        File saveDir = new File(saveDirParent.getAbsolutePath()  + "/" + playlistFolderName);
        if(!saveDir.exists())
            saveDir.mkdirs();
        
        //create a playlist for each of the subfolders
        for (String str : folders) {
            File currentFile = new File(sourceDir.getAbsolutePath() + "\\" + str);
            if (currentFile.isDirectory()) {
                StringBuilder name = new StringBuilder();
                //if folders are numbered append zero to single digits to help keep things in order
                if (str.matches("\\d{1}\\D{1}.*"))
                    name.append(0);
                //name for each playlist will be the name of the subfolder for which the playlist was created
                name.append(str);
                createAPlaylist(currentFile, saveDir, name.toString());
            }
        }
        
        String sourceString = sourceDir.getAbsolutePath();
        String unitedPlName = sourceString.substring(sourceString.lastIndexOf("\\") + 1);
        PlaylistCombiner.combinePlaylists(saveDir, saveDir, unitedPlName);
        
        return true;
    }
    
    private static boolean areNumerical(File[] files){
        String path = files[0].getAbsolutePath();
        String trackName = path.substring(path.lastIndexOf("\\") + 1);
        return "123456789".indexOf(trackName.charAt(0)) != -1;
    }
    
    private static void sort(File[] files) {
        String path = files[0].getAbsolutePath();
        //substring from last index of \ + 1 till the end will give the file name
        String trackName = path.substring(path.lastIndexOf("\\") + 1);
        
        char d = Character.MIN_VALUE;
        for (int i = 0; i < trackName.length(); i++) {
            char temp = trackName.charAt(i);
            if(!Character.isDigit(temp)){
                d = temp;
                break;
            }
        }
        //find the delimiter after the number
        //assumes that all file names follow the same pattern
        final char delimiter = d;
        
        //sort the files based on the ordinal number in their name
        Arrays.sort(files, (o1, o2) -> {
            String currentFile1Path = o1.getAbsolutePath();
            String name = currentFile1Path.substring(currentFile1Path.lastIndexOf("\\") + 1);
            int trackCount = Integer.parseInt(name.substring(0, name.indexOf("" + delimiter)));
            
            String currentFile2Path = o2.getAbsolutePath();
            String name2 = currentFile2Path.substring(currentFile2Path.lastIndexOf("\\") + 1);
            int trackCount2 = Integer.parseInt(name2.substring(0, name2.indexOf("" + delimiter)));
            
            return Integer.compare(trackCount, trackCount2);
        });
    }
    
    //used to convert special character to symbols that vlc media player understands
    private static String replaceSpecialChars(String string){
        return string
                .replace(" ", "%20")
                .replace("\\", "/")
                .replace("&", "&amp;")
                .replace("#", "%23")
                .replace("[", "%5B")
                .replace("]", "%5D")
                .replace("'", "&#39;");
    }
    
    
}
