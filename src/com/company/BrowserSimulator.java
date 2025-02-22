package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BrowserSimulator {

    private static String sessionId = "";
    private static final int numberOfGamesToIterate = 100;
    private static int totalSuccess = 0;

    public static void main(String[] args) {

        try {
            String address = "http://ec2-13-48-129-56.eu-north-1.compute.amazonaws.com:8080/";
            String local = "http://127.0.0.1:8080/";
            URL url = new URL(local);
            String responseFromServer = sendingGetRequest(url);
            GuessMachine machine = new GuessMachine();
            for (int i = 1; i < numberOfGamesToIterate;) {
                if (responseFromServer.contains("Welcome")) {
                    String randomGuess = machine.makeAGuess(null);
                    responseFromServer = sendingPostRequest(url, randomGuess);
                } else if (responseFromServer.contains("HIGHER")) {
                    String higherGuess = machine.makeAGuess(Result.HIGHER);
                    responseFromServer = sendingPostRequest(url, higherGuess);
                } else if (responseFromServer.contains("LOWER")) {
                    String lowerGuess = machine.makeAGuess(Result.LOWER);
                    responseFromServer = sendingPostRequest(url, lowerGuess);
                } else if (responseFromServer.contains("CORRECT")) {
                    String finalResult = machine.makeAGuess(Result.CORRECT);
                    responseFromServer = sendingGetRequest(url);
                    i++;
                    totalSuccess = Integer.parseInt(finalResult) + totalSuccess;
                }
            }
            System.out.println(totalSuccess/100);
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
        con.setRequestProperty("Accept", "text/html");

        int responseCode = con.getResponseCode();
        String cookie = con.getHeaderField("Set-Cookie");
        if(cookie != null) {
            String[] split = cookie.split("=");
            sessionId = split[1];
        }
        System.out.println("SESSION ID: " + sessionId);
        System.out.println("Sending get request : "+ url);
        System.out.println("Response code : "+ responseCode);

        // Reading response from input Stream
        return getResponse(con);
    }

    // HTTP Post request
    private static String sendingPostRequest(URL url, String body) throws IOException {

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Cookie", "sessionid=" + sessionId);
        con.setRequestProperty("Accept", "text/html");

        // Send post request
        con.setDoOutput(true);
        PrintStream printStream = new PrintStream(con.getOutputStream());
        printStream.print("answer=" + body);
        printStream.flush();
        printStream.close();

        int responseCode = con.getResponseCode();
        String cookie = con.getHeaderField("Set-Cookie");
        if(cookie != null) {
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

class GuessMachine {

    int guessCounter = 0;
    Integer lastLowerThan = 100;
    Integer lastHigherThan = 0;
    Integer currentGuess = 50;

    public String makeAGuess(Result towards) {
        if (guessCounter == 0 && towards == null) {
            guessCounter++;
            return String.valueOf(currentGuess);
        } else if (towards == Result.HIGHER) {
            if (guessCounter == 1) {
                currentGuess = 75;
                lastHigherThan = 50;
            } else if (guessCounter > 1) {
                lastHigherThan = currentGuess;
                currentGuess = (lastLowerThan + currentGuess) / 2;
            }
        } else if (towards == Result.LOWER) {
            if (guessCounter == 1) {
                currentGuess = 25;
                lastLowerThan = 50;
            } else if (guessCounter > 1) {
                lastLowerThan = currentGuess;
                currentGuess = (lastHigherThan + currentGuess) / 2;
            }
        } else if (towards == Result.CORRECT) {
            int finalNumberOfGuesses = guessCounter;
            reset();
            return String.valueOf(finalNumberOfGuesses);
        }
        guessCounter++;
        return String.valueOf(currentGuess);
    }

    private void reset() {
        currentGuess = 50;
        lastLowerThan = 100;
        lastHigherThan = 0;
        guessCounter = 0;
    }
}

