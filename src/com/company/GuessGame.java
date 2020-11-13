package com.company;

import java.util.UUID;

public class GuessGame {

    private final int secret = (int) (Math.random() * 100);
    private final UUID clientID;
    private int numberOfAttempts = 0;

    public GuessGame(UUID clientID) {
        this.clientID = clientID;
        System.out.println(secret);
    }

    public Result makeAGuess(int guess) {
        numberOfAttempts++;
        if(guess == secret) {
            return Result.CORRECT;
        } else if(guess > secret) {
            return Result.LOWER;
        } else {
            return Result.HIGHER;
        }
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public UUID getClientID() {
        return clientID;
    }
}
