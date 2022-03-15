import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static String urlNow = "";

    public static void main(String[] args) {
       Scanner userInput = new Scanner(System.in);
//        String urlNow = "";

        System.out.println("============== Simple Web Browser ==============");
        System.out.println("Silahkan pilih menu :\n1. Buka web dengan link\n2. Download file contoh");

        int chosenNumber = userInput.nextInt();
        switch(chosenNumber) {
            case 1:
                openWeb();
                break;
            case 2:
                Scanner userLink = new Scanner(System.in);
                System.out.println("Silahkan masukkan link file yang ingin didownload : ");
                // sample link 1 : http://www.africau.edu/images/default/sample.pdf
                // sample link 2 : https://filesamples.com/samples/video/m4v/sample_1280x720_surfing_with_audio.m4v
                String link = userLink.nextLine();
                try { 
                    download(link, "./");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
    }

    public static void openWeb() {
        Scanner userLink = new Scanner(System.in);
        System.out.println("Silahkan masukkan link yang ingin dibuka : ");
        String link = userLink.nextLine();
        String host = getUrlDomainName(link);
        String urn = link.substring(link.lastIndexOf("/") + 1);
        if(urn.equals(host))
            urn = "";

        UrlContent(host, urn);
        System.out.println("Clickable Links : \n");
        System.out.println(extractAnchorLinks(urlNow));
    }

    public static String getUrlDomainName(String url) {
        String domainName = new String(url);

        int index = domainName.indexOf("://");

        if (index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3);
        }

        index = domainName.indexOf('/');

        if (index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index);
        }

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        domainName = domainName.replaceFirst("^www.*?\\.", "");

        return domainName;
    }

    public static void UrlContent(String urlInput, String urn) {
        try {
            Socket socket = new Socket(urlInput, 80);
            String protocols = "GET /" + urn + " HTTP/1.1\r\nHost: " + urlInput + "\r\n\r\n ";
            System.out.println(protocols);

            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            bos.write(protocols.getBytes());
            bos.flush();

            int bufferSize = 100;
            byte[] bResp = new byte[bufferSize];
            int c = bis.read(bResp);
            String resp = "";

            while(c != -1) {
                resp += (new String(bResp));
                bResp = new byte[bufferSize];
                c = bis.read(bResp);

            }
            String[] getHeader = resp.split("\n");
            String[] statusCode = getHeader[0].split(" ");
            String statusMessage = "Response status code :";
            for(int i = 1; i < statusCode.length; i++) {
                statusMessage += " ";
                statusMessage += statusCode[i];
            }
            // System.out.println(statusCode.length);
            // System.out.println(Arrays.toString(statusCode));
            System.out.println(statusMessage + "\n");
            Pattern pattern = Pattern.compile("(?><(?:(?:(?:(script|style|object|embed|applet|noframes|noscript|noembed)(?:\\s+(?>\"[\\S\\s]*?\"|'[\\S\\s]*?'|(?:(?!/>)[^>])?)+)?\\s*>)[\\S\\s]*?</\\1\\s*(?=>))|(?:/?(?!body)[\\w:]+\\s*/?)|(?:(?!body)[\\w:]+\\s+(?:\"[\\S\\s]*?\"|'[\\S\\s]*?'|[^>]?)+\\s*/?)|\\?[\\S\\s]*?\\?|(?:!(?:(?:DOCTYPE[\\S\\s]*?)|(?:\\[CDATA\\[[\\S\\s]*?\\]\\])|(?:--[\\S\\s]*?--)|(?:ATTLIST[\\S\\s]*?)|(?:ENTITY[\\S\\s]*?)|(?:ELEMENT[\\S\\s]*?))))>|[\\S\\s])*?<body(?:\\s+(?>\"[\\S\\s]*?\"|'[\\S\\s]*?'|(?:(?!/>)[^>])?)+)?\\s*>((?:<(?:(?:(?:(script|style|object|embed|applet|noframes|noscript|noembed)(?:\\s+(?>\"[\\S\\s]*?\"|'[\\S\\s]*?'|(?:(?!/>)[^>])?)+)?\\s*>)[\\S\\s]*?</\\3\\s*(?=>))|(?:/?(?!body)[\\w:]+\\s*/?)|(?:(?!body)[\\w:]+\\s+(?:\"[\\S\\s]*?\"|'[\\S\\s]*?'|[^>]?)+\\s*/?)|\\?[\\S\\s]*?\\?|(?:!(?:(?:DOCTYPE[\\S\\s]*?)|(?:\\[CDATA\\[[\\S\\s]*?\\]\\])|(?:--[\\S\\s]*?--)|(?:ATTLIST[\\S\\s]*?)|(?:ENTITY[\\S\\s]*?)|(?:ELEMENT[\\S\\s]*?))))>|[\\S\\s])*)</body\\s*>");
            Matcher matcher = pattern.matcher(resp);
            matcher.find();
            System.out.println(matcher.group(2));
            urlNow += resp;

            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<String> extractAnchorLinks(String string) {
        List<String> anchorLinkList = new ArrayList<String>();
        final String TAG = "a href=\"";
        final int TAG_LENGTH = TAG.length();
        int startIndex = 0, endIndex = 0;
        String nextSubstring = "";
        do {
            startIndex = string.indexOf(TAG);
            if (startIndex != -1) {
                nextSubstring = string.substring(startIndex + TAG_LENGTH);
                endIndex = nextSubstring.indexOf("\">");
                if (endIndex != -1) {
                    anchorLinkList.add("\n" + nextSubstring.substring(0, endIndex));
                }
                string = nextSubstring;
            }
        } while (startIndex != -1 && endIndex != -1);
        return anchorLinkList;
    }

    private static Path download(String sourceURL, String targetDirectory) throws IOException
    {
        URL url = new URL(sourceURL);
        String fileName = sourceURL.substring(sourceURL.lastIndexOf('/') + 1, sourceURL.length());
        Path targetPath = new File(targetDirectory + File.separator + fileName).toPath();
        Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Download success\n");

        return targetPath;
    }
}
