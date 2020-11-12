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

    public static TreeMap <String,GuessGame> gameList;

    public static void main(String[] args) {

        try {
            ServerSocket ss = new ServerSocket(8080);
            System.out.println("Server socket created...");

            while (true) {
                Socket s = ss.accept();
                BufferedReader request = new BufferedReader(new InputStreamReader(s.getInputStream()));

                HTTPHeader header = new HTTPHeader(request);
                String requestType = header.requestType;

                if(gameList.containsKey(header.clientId)) {
                    GuessGame game = gameList.get(header.clientId);
                    if (requestType.equals("GET")) {
//                        s.shutdownInput();
                        sendWelcomeResponse(s);
                    } else if (requestType.equals("POST")) {
                        int answer = Integer.parseInt(header.getAnswer());
                        String result = game.makeAGuess(answer);
//                        s.shutdownInput();
                        PrintStream response = new PrintStream(s.getOutputStream());
                        response.println("HTTP/1.1 200 OK\n");
                    }
                } else {
//                    s.shutdownInput();
                    GuessGame game = new GuessGame(UUID.randomUUID());
                    gameList.put(game.getClientID().toString(), game);
                    sendWelcomeResponse(s);
                }

                s.shutdownOutput();
                s.close();
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    private static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    private static void sendWelcomeResponse(Socket s) throws IOException {
        PrintStream response = new PrintStream(s.getOutputStream());
        response.println("HTTP/1.1 200 OK");
        UUID uuid = UUID.randomUUID();
        response.println("Set-Cookie: clientId="+ uuid +"\n");
        response.println(readFile("/Users/farukbugrademirel/IdeaProjects/Assignment-2/src/com/company/WelcomeResponse.html"));
    }

    private static void sendResultResponse(String result) {

    }
}

class HTTPHeader {

    String header;
    String body;
    String clientId;
    String requestType;

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
                String[] split = s.split(": ");
                this.clientId = split[1];
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

class WelcomeResponse {
    public static String getText() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Guess Game!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <h1>Welcome to the Guess Game!</h1>\n" +
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
}

class IncorrectResponse {

    enum Level {
        HIGHER,
        LOWER
    }

    public static String getText(Level higherOrLower, String numberOfGuesses) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Guess Game!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <h1>Sorry you guess is Wrong!!</h1>\n" +
                "    <p>You should guess" + higherOrLower + "</p>\n" +
                "    <form action=\"\" method=\"post\">\n" +
                "        <label for=\"fname\">What is your guess?</label>\n" +
                "        <input type=\"text\" id=\"fname\" name=\"answer\"><br><br>\n" +
                "        <input type=\"submit\" value=\"Submit\">\n" +
                "    </form>\n" +
                "\n" +
                "</body>\n" +
                "</html>";
    }
}