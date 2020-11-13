package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.UUID;

public class Server {

    public static TreeMap <String,GuessGame> gameList = new TreeMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(8080);
            while (true) {
                try {
                    Socket s = ss.accept();
                    BufferedReader request = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    HTTPHeader header = new HTTPHeader(request);
                    String requestType = header.requestType;
                    System.out.println(header.header);
                    System.out.println(header.body);

                    boolean containsClientId = gameList.containsKey(header.sessionid);
                    boolean isTextRequest = header.acceptType.equals("text");

                    if(containsClientId && isTextRequest) {
                        GuessGame game = gameList.get(header.sessionid);
                        if(requestType.equals("GET")) {
                            if(game.getNumberOfAttempts() == 0) {
                                sendWelcomeResponse(s,game.getSessionId().toString());
                            } else {
                                sendResultResponse(s, game, game.getLastTip());
                            }
                        } else if (requestType.equals("POST")) {
                            int answer = Integer.parseInt(header.getAnswer());
                            Result result = game.makeAGuess(answer);
                            switch (result) {
                                case LOWER -> sendResultResponse(s, game, Result.LOWER);
                                case HIGHER -> sendResultResponse(s, game, Result.HIGHER);
                                case CORRECT -> {
                                    sendResultResponse(s, game, Result.CORRECT);
                                    gameList.remove(game.getSessionId().toString());
                                }
                            }
                        }
                    } else if (!containsClientId && isTextRequest) {
                        GuessGame game = new GuessGame(UUID.randomUUID());
                        String sessionId = game.getSessionId().toString();
                        gameList.put(sessionId, game);
                        sendWelcomeResponse(s, sessionId);
                    }
                    s.shutdownOutput();
                    s.close();
                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

/**
 * Responses
 */
    private static void sendWelcomeResponse(Socket s, String clientId) throws IOException {
        PrintStream response = new PrintStream(s.getOutputStream());
        response.println("HTTP/1.1 200 OK");
        response.println("Set-Cookie: sessionid="+ clientId +"\n");
        response.println(ResponseText.getWelcomeText(clientId));
    }

    private static void sendResultResponse(Socket s, GuessGame game, Result result) throws IOException {
        String numberOfAttempts = String.valueOf(game.getNumberOfAttempts());
        PrintStream response = new PrintStream(s.getOutputStream());
        response.println("HTTP/1.1 200 OK\n");
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
    String sessionid = "-1";
    String requestType;
    String acceptType;

    public HTTPHeader(BufferedReader request) throws IOException {

        String line;
        StringBuilder sb1 = new StringBuilder();

        String firstLine = request.readLine();
        System.out.println(firstLine);
        StringTokenizer stringTokenizer = new StringTokenizer(firstLine, " ");
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
                String[] split = s.split("sessionid=");
                this.sessionid = split[1];
            } else if (s.contains("Accept:")) {
                String[] split = s.split(": ");
                String operation = split[1];
                String[] type = operation.split("/");
                this.acceptType = type[0];
            }
        }
    }

    public String getAnswer() {
        if(this.body != null && !this.body.equals("answer=")) {
            String[] lineSplit = this.body.split("=");
            return lineSplit[1];
        }
        return "0";
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
                "    <h1>Welcome to the Guess Game Dear "+ clientId +"!</h1>\n" +
                "    <p>I am thinking of a number between 1 and 100.</p>\n" +
                "    <form action=\"\" method=\"post\">\n" +
                "        <label for=\"fname\">What is your guess?</label>\n" +
                "        <input type=\"number\" id=\"fname\" name=\"answer\"><br><br>\n" +
                "        <input type=\"submit\" value=\"Submit\">\n" +
                "    </form>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
    public static String getIncorrectResultText(Result higherOrLower, String numberOfGuesses) {

        String plural = "";
        if(!numberOfGuesses.equals("1"))
            plural = "es";
            return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Guess Game!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <h1>Sorry, your guess is WRONG!!</h1>\n" +
                "    <p> You have made "+ numberOfGuesses+  " guess"+ plural + "." +
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