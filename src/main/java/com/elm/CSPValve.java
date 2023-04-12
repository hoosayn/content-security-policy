package com.elm;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Logger;

public class CSPValve extends ValveBase {

    private static final Logger logger = Logger.getLogger(CSPValve.class.getName());

    boolean restart = true;
    String hashes = "";
    public String getHashes() throws IOException, URISyntaxException {
        String operatingSystem = System.getProperty("os.name");
        char slash = operatingSystem.contains("Windows")?'\\':'/';
        restart = false;
        StringBuilder builder = new StringBuilder();
        String file = new File(CSPValve.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getPath();
        String tomcatPath = "";
        for(int i=file.indexOf("tomcat");i<file.length()-file.indexOf("tomcat");i++){
            if(slash == file.charAt(i)){
                tomcatPath = file.substring(0,i);
                break;
            }
        }
        try {

            String absolutePath = getFile(tomcatPath+slash+"webapps"+slash+"ROOT"+slash);

            File myObj = new File(absolutePath);
            Scanner myReader = new Scanner(myObj);
            String content = new String(Files.readAllBytes(Paths.get(absolutePath)));
            Document doc = Jsoup.parse(content);
            Elements script = doc.select("script");
            script.stream().forEach(e -> {
                if(!e.attr("integrity").isEmpty()){
                    builder.append("'").append(e.attr("integrity")).append("' ");
                }
            });

            Elements sytle = doc.select("style");
            sytle.stream().forEach(e -> {
                if(!e.attr("integrity").isEmpty()){
                    builder.append("'").append(e.attr("integrity")).append("' ");
                }
            });

            Elements link = doc.select("link");
            link.stream().forEach(e -> {
                if(!e.attr("integrity").isEmpty()){
                    builder.append("'").append(e.attr("integrity")).append("' ");
                }
            });
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        try {
            if(restart){
                hashes = getHashes();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpServletRequest httpServletRequest = request.getRequest();
        response.setHeader("Content-Security-Policy",
                "img-src 'self' data: https: http:;connect-src 'self' https: http:;" +
                "font-src 'self';default-src 'none';base-uri 'self';object-src 'none';" +
                "frame-ancestors 'none'; " +
                "form-action 'self'; " +
                "script-src 'self' 'strict-dynamic' " +hashes+
                ";" +

                "style-src 'self' 'unsafe-inline';");

        getNext().invoke(request, response);
    }

    private String getFile(String rootPath ){
        File root = new File(rootPath);
        String fileName = "index.html";
        try {
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root, null, recursive);

            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                if (file.getName().equals(fileName)) {
                    System.out.println(file.getAbsolutePath());
                    return file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
