package stef.playlistmaker.console;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaylistCombiner {
    
    public static void combinePlaylists(File directory, String plName, String saveDir) {
        //filter the files by the extension (look for existing playlists in the specified folder)
        FilenameFilter filter = (File dir, String name) -> name.matches(".*\\.xspf");
        File[] files = directory.listFiles(filter);
        //if there is not at least 2 playlists then there is nothing to combine
        if (files.length < 2) {
            System.out.println("Specified folder contains less than 2 playlists, nothing to combine.");
            return;
        }
        System.out.println("\n");
        
        File combinedPlaylist = new File(saveDir + "\\" + plName + "(combined).xspf");
        System.err.println(combinedPlaylist.getAbsolutePath());
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(combinedPlaylist));) {
            System.out.println("Combining Playlists...");
            //write data required for any playlist
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            bw.write("<playlist xmlns=\"http://xspf.org/ns/0/\" xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\" version=\"1\">\n");
            bw.write(String.format("\t<title>%s</title>\n", plName));
            
            bw.write("\t<trackList>\n");
            int trackCount = 0;
            ArrayList<String> titles = new ArrayList<>();
            Map<String, Integer> linesPerTitle = new HashMap<>();
            //pattern for taking individual track info from each playlist, this line is where all required track info is
            Pattern locationPattern = Pattern.compile("^\\s*<location>.*");
            for (File file : files) {
                //to avoid recursive reading and writing to itself
                if (file.equals(combinedPlaylist))
                    continue;
                
                
                int numOfTracks = 0;
                //for each playlist save the title only once, this var is used to make sure of that
                boolean titleSaved = false;
                String title = null;
                
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    //read each line of each playlist file
                    while (true) {
                        String currentLine = br.readLine();
                        if (currentLine == null)
                            break;
                        Matcher matcher = locationPattern.matcher(currentLine);
                        //if line contains info about the track, specified in the pattern above then save write it to the combined playlist
                        if (matcher.matches()) {
                            //save the track title
                            if (!titleSaved) {
                                
                                //this is what current line looks like for example
                                //<location>file:///C:/Users/.../mainfolder/subfolder/trackname.mp4</location>
                                //so in order to get the subfolder name we need the substring between 3rd last '/' and second last '/'
                                //first '/' is before location (</location>)
                                //second '/' is before track name
                                //and the third is before the title we need
                                title = currentLine.substring(nthLastIndexOf(3, "/", currentLine) + 1, nthLastIndexOf(2, "/", currentLine));
                                
                                //reformat the title back to original
                                title = title
                                        .replace("%20", " ")
                                        .replace("/", "\\")
                                        .replace("&amp;", "&")
                                        .replace("%23", "#");
                                titles.add(title);
                                titleSaved = true;
                                
                                System.out.println(title);
                            }
                            //write track specific info, use the line found above and generate the rest of the data
                            bw.write("\t\t<track>\n");
                            bw.write(currentLine);
                            System.out.println(currentLine);
                            bw.write("\t\t\t<duration></duration>\n");
                            bw.write("\t\t\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\n");
                            bw.write(String.format("\t\t\t\t<vlc:id>%d</vlc:id>\n", trackCount++));
                            bw.write("\t\t\t</extension>\n");
                            bw.write("\t\t</track>\n");
                            numOfTracks++;
                        }
                    }
                }
                //used for creating sub-folders inside the playlist
                if (title != null)
                    linesPerTitle.put(title, numOfTracks);
            }
            
            bw.write("\t</trackList>\n");
            
            int trackCount2 = 0;
            bw.write("\t<extension application=\"http://www.videolan.org/vlc/playlist/0\">\n");
            
            //this kind of formatting will create sub-sections in the playlist(in vlc media player)
            for (String title : titles) {
                //node defines a sub-section in vlc media player playlist
                bw.write(String.format("\t\t<vlc:node title=\"%s\">\n", title));
                int numOfTracks = linesPerTitle.get(title);
                //write each track's id
                for (int i = 0; i < numOfTracks; i++) {
                    bw.write(String.format("\t\t\t<vlc:item tid=\"%d\"/>\n", trackCount2++));
                }
                bw.write("\t\t</vlc:node>\n");
            }
            //write remaining info
            bw.write("\t</extension>\n");
            bw.write("</playlist>");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //used while extracting look above for explanation the name of a folder
    private static int nthLastIndexOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }
    
}
