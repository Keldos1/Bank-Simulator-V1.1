package org.example.banksimulator.exceptions;

public class IncorrectCurrencyException extends RuntimeException{
    public IncorrectCurrencyException(String message) {
        super(message);
    }
}
