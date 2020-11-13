package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;

public class Server {

    public static TreeMap <String,GuessGame> gameList = new TreeMap<>();

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(8080);
            System.out.println("Server socket created...");
            while (true) {
                Socket s = ss.accept();
                BufferedReader request = new BufferedReader(new InputStreamReader(s.getInputStream()));

                HTTPHeader header = new HTTPHeader(request);
                String requestType = header.requestType;
                System.out.println(gameList);

                boolean containsClientId = gameList.containsKey(header.clientId) ;
                boolean isDocument = header.fetchDest.equals("document");

                if(containsClientId && isDocument) {
                    System.out.println("CONTAINS KEY!!!!!!!!!!!!!!!!!!!!!!");
                    GuessGame game = gameList.get(header.clientId);
                    if (requestType.equals("GET")) {
                        System.out.println("GET REQUESTTTTTTTTTTTT");
                        sendWelcomeResponse(s,game.getClientID().toString());
                    } else if (requestType.equals("POST")) {
                        System.out.println("POST REQUESTTTTTTTTTT");
                        int answer = Integer.parseInt(header.getAnswer());
                        Result result = game.makeAGuess(answer);
                        switch (result){
                            case CORRECT -> {
                                sendResultResponse(s,game,Result.CORRECT);
                                gameList.remove(game.getClientID().toString());
                            }
                            case LOWER -> sendResultResponse(s,game,Result.LOWER);
                            case HIGHER -> sendResultResponse(s,game,Result.HIGHER);
                        }
                    }
                } else if (!containsClientId && isDocument) {
                    GuessGame game = new GuessGame(UUID.randomUUID());
                    String clientId = game.getClientID().toString();
                    gameList.put(clientId, game);
                    sendWelcomeResponse(s, clientId);
                }
                s.shutdownOutput();
                s.close();
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
            System.out.println(e.toString());
        }
    }

    private static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

/**
 * Responses
 */


    private static void sendWelcomeResponse(Socket s, String clientId) throws IOException {
        PrintStream response = new PrintStream(s.getOutputStream());
        response.println("HTTP/1.1 200 OK");
        response.println("Set-Cookie: clientId="+ clientId +"\n");
        response.println(ResponseText.getWelcomeText(clientId));
    }

    private static void sendResultResponse(Socket s, GuessGame game, Result result) throws IOException {
        String clientId = game.getClientID().toString();
        String numberOfAttempts = String.valueOf(game.getNumberOfAttempts());
        PrintStream response = new PrintStream(s.getOutputStream());
        response.println("HTTP/1.1 200 OK");
        response.println("Set-Cookie: clientId="+ clientId +"\n");
        switch (result) {
            case HIGHER -> {
                String textHigher = ResponseText.getIncorrectResultText(Result.HIGHER, numberOfAttempts);
                response.println(textHigher);
            }
            case LOWER -> {
                String textLower = ResponseText.getIncorrectResultText(Result.LOWER, numberOfAttempts);
                response.println(textLower);
            }
            case CORRECT -> {
                String textCorrect = ResponseText.getCorrectResultText(numberOfAttempts);
                response.println(textCorrect);
            }
        }
    }
}

/**
 * Header Class
 */

class HTTPHeader {

    String header;
    String body;
    String clientId = "-1";
    String requestType;
    String fetchDest;

    public HTTPHeader(BufferedReader request) throws IOException {

        String line;
        StringBuilder sb1 = new StringBuilder();

        String firstLine = request.readLine();
        StringTokenizer stringTokenizer = new StringTokenizer(firstLine, " ?");
        this.requestType = stringTokenizer.nextToken();

        while ((line = request.readLine()) != null && !line.isEmpty()) {
            sb1.append(line).append("\n");
        }
        this.header = sb1.toString();

        String[] headerSplit = header.split("\n");
        for (String s : headerSplit) {
            if (s.contains("Content-Length")){
                int contentLength = Integer.parseInt(s.replaceAll("[^0-9]", ""));
                int i;
                StringBuilder sb2 = new StringBuilder();
                for (int j = 0; j < contentLength; j++) {
                    i = request.read();
                    char c = (char) i;
                    sb2.append(c);
                }
                this.body = sb2.toString();
            } else if (s.contains("Cookie")) {
                String[] split = s.split("=");
                this.clientId = split[1];
            } else if (s.contains("Sec-Fetch-Dest")) {
                String[] split = s.split(": ");
                this.fetchDest = split[1];
            }
        }
    }

    public String getAnswer() {
        if(this.body != null) {
            String[] lineSplit = this.body.split("=");
            return lineSplit[1];
        }
        return "";
    }
}

/**
 * Static Text Classes
 */

class ResponseText {
    public static String getWelcomeText(String clientId) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Guess Game!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <h1>Welcome to the Guess Game "+ clientId +"!</h1>\n" +
                "    <p>I am thinking of a number between 1 and 100.</p>\n" +
                "    <form action=\"\" method=\"post\">\n" +
                "        <label for=\"fname\">What is your guess?</label>\n" +
                "        <input type=\"text\" id=\"fname\" name=\"answer\"><br><br>\n" +
                "        <input type=\"submit\" value=\"Submit\">\n" +
                "    </form>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
    public static String getIncorrectResultText(Result higherOrLower, String numberOfGuesses) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Guess Game!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <h1>Sorry, your guess is WRONG!!</h1>\n" +
                "    <p> You have made "+ numberOfGuesses+  "guess(es)." +
                "    <p>You should guess " + higherOrLower + "</p>\n" +
                "    <form action=\"\" method=\"post\">\n" +
                "        <label for=\"fname\">What is your guess?</label>\n" +
                "        <input type=\"text\" id=\"fname\" name=\"answer\"><br><br>\n" +
                "        <input type=\"submit\" value=\"Submit\">\n" +
                "    </form>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }

    public static String getCorrectResultText(String numberOfGuesses) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Guess Game!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <h1>CORRECT Guess!!!!!!!!</h1>\n" +
                "    <p>You have made " + numberOfGuesses + " guess(es).</p>\n" +
                "    <form action=\"\" method=\"post\">\n" +
                "        <label for=\"fname\">Wanna play again?</label>\n" +
                "        <input type=\"submit\" value=\"New Game\">\n" +
                "    </form>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
}

enum Result {
    HIGHER,
    LOWER,
    CORRECT
}