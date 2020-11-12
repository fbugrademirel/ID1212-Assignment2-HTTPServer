package com.company;

import java.util.UUID;

public class GuessGame {

    private final int secret = (int) (Math.random() * 100);
    private int numberOfAttempts = 0;
    private UUID clientID;

    public GuessGame(UUID clientID) {
        this.clientID = clientID;
    }

    public String makeAGuess(int guess) {
        numberOfAttempts++;
        if(guess == secret) {
            return "Correct!";
        } else if(guess > secret) {
            return "Lower!";
        } else {
            return "Higher!";
        }
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public UUID getClientID() {
        return clientID;
    }
}
