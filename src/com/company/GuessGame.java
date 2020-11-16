package com.company;

import java.util.UUID;

public class GuessGame {

    private final int secret;
    private final UUID sessionId;
    private int numberOfAttempts = 0;
    private Result lastTip;

    public GuessGame(UUID sessionId) {
        int secret = ((int) (Math.random() * 100));
        if(secret == 0) {
            this.secret = 1;
        } else if (secret == 100) {
            this.secret = 99;
        } else {
            this.secret = secret;
        }
        this.sessionId = sessionId;
        System.out.println(secret);
    }

    public Result makeAGuess(int guess) {
        numberOfAttempts++;
        if(guess == secret) {
            return Result.CORRECT;
        } else if(guess > secret) {
            lastTip = Result.LOWER;
            return Result.LOWER;
        } else {
            lastTip = Result.HIGHER;
            return Result.HIGHER;
        }
    }

    public int getNumberOfAttempts() {
        return numberOfAttempts;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Result getLastTip() {
        return lastTip;
    }
}
