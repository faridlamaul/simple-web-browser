import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
    public static String urlNow = "";

    public static void main(String[] args) {
       Scanner userInput = new Scanner(System.in);
//        String urlNow = "";

        System.out.println("============== Simple Web Browser ==============");
        System.out.println("Silahkan pilih menu :\n1. Buka web dengan link\n2. Download file contoh\n3. Buka web dengan autentikasi");

        int chosenNumber = userInput.nextInt();
        switch(chosenNumber) {
            case 1:
                Scanner userLink = new Scanner(System.in);
                System.out.println("Silahkan masukkan link yang ingin dibuka : ");
                // sample link : info.cern.ch
                // sample redirect link : https://pdhejjcoffee.000webhostapp.com/admin
                String link = userLink.nextLine();
                openWeb(link);
                break;
            case 2:
                Scanner userLink2 = new Scanner(System.in);
                System.out.println("Silahkan masukkan link file yang ingin didownload : ");
                // sample link 1 : http://www.africau.edu/images/default/sample.pdf
                // sample link 2 : https://filesamples.com/samples/video/m4v/sample_1280x720_surfing_with_audio.m4v
                String link2 = userLink2.nextLine();
                try { 
                    download(link2, "./");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    openWebAuth();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Input tidak valid");
        }
    }

    private static void openWebAuth() throws IOException {
        URL url = new URL("http://httpbin.org/basic-auth/farid/farid123");
        // HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // connection.setRequestProperty("Authorization", "Basic dGVzdGluZzEyM0BnbWFpbC5jb206dGVzdGluZzEyMw==");
        // openWeb(url.toString());
        Socket socket = new Socket("httpbin.org", 80);
        String protocols = "GET /basic-auth/farid/farid123 HTTP/1.1\r\nHost: httpbin.org\r\n\r\n ";
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
        
        System.out.println(resp);
    }

    public static void openWeb(String link) {
        String host = getUrlDomainName(link);
        String urn = link.substring(link.lastIndexOf("/") + 1);
        if(urn.equals(host) || urn.contains("www."))
            urn = "";

        UrlContent(host, urn);
        System.out.println("\nClickable Links : \n");
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
            System.out.println(statusMessage + "\n");
            Document doc = Jsoup.parse(resp);
            Elements elements = doc.select("body").first().children();
            //or only `<p>` elements
            //Elements elements = doc.select("p"); 
            for (Element el : elements)
                System.out.println(el);
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
        System.out.println("\nDownload on progress...");
        Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Download success\n");

        return targetPath;
    }
}
