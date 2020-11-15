package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BrowserSimulator {

    public static String sessionId = "1";

    public static void main(String[] args) {

        try {
            URL url = new URL("http://ec2-13-48-129-56.eu-north-1.compute.amazonaws.com:8080/");
            String responseFromServer = sendingGetRequest(url);
            if(responseFromServer.contains("Welcome")) {
                int randomGuess = (int) (Math.random() * 100);
              //  sendingPostRequest(url, String.valueOf(randomGuess));
            } else if(responseFromServer.contains("HIGHER")) {

            } else if(responseFromServer.contains("LOWER")) {

            } else if(responseFromServer.contains("CORRECT")) {
            }

        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    // HTTP GET request
    private static String sendingGetRequest(URL url) throws IOException {

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // By default it is GET request
        con.setRequestMethod("GET");
        con.setRequestProperty("Cookie", "sessionid=" + sessionId);

        int responseCode = con.getResponseCode();
        String cookie = con.getHeaderField("Set-Cookie");
        System.out.println(cookie);
//        if(sessionId != null) {
//            String[] split = cookie.split("=");
//            sessionId = split[1];
//        }
        System.out.println(sessionId);
        System.out.println("Sending get request : "+ url);
        System.out.println("Response code : "+ responseCode);

        // Reading response from input Stream
        return getResponse(con);
    }

    // HTTP Post request
    private static String sendingPostRequest(URL url, String body) throws IOException {

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");


        // Send post request
        con.setDoOutput(true);
        PrintStream printStream = new PrintStream(con.getOutputStream());
        printStream.println(body);
        printStream.flush();
        printStream.close();

        int responseCode = con.getResponseCode();
        String cookie = con.getHeaderField("Set-Cookie");
        if(sessionId != null) {
            String[] split = cookie.split("=");
            sessionId = split[1];
        }
        System.out.println(sessionId);
        System.out.println("Sending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        return getResponse(con);
    }

    private static String getResponse(HttpURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;

        StringBuilder sb = new StringBuilder();
        while ((output = in.readLine()) != null) {
            sb.append(output).append("\n");
        }
        in.close();

        //printing result from response
        String response = sb.toString();
        System.out.println(response);
        return response;
    }
}

