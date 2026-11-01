package org.example.banksimulator.exceptions;

public class IncorrectlyTransferException extends RuntimeException{
    public IncorrectlyTransferException(String message) {
        super(message);
    }
}
