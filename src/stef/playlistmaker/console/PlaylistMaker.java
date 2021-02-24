package stef.playlistmaker.console;

import java.io.*;


public class PlaylistMaker {
    
    public static boolean createAPlaylist(File sourceDir, File saveDir, String plName) {
        
        //filter the files by the extension (only add .mp4 files to the playlist)
        FilenameFilter filter = (File dir, String name) -> name.matches(".*\\.mp4");
        
        //save the list of eligible files
        File[] files = sourceDir.listFiles(filter);
        if (files.length < 1)
            return false;
        
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
                String currentFilePath = current.getAbsolutePath()
                        .replace(" ", "%20")
                        .replace("\\", "/")
                        .replace("&", "&amp;")
                        .replace("#", "%23");
                
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
    
}
