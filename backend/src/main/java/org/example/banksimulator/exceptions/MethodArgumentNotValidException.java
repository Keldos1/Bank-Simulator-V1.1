package org.example.banksimulator.exceptions;

public class MethodArgumentNotValidException extends RuntimeException {
    public MethodArgumentNotValidException(String message) {

        super(message);
    }
}
