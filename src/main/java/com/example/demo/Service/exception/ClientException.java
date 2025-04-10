package com.example.demo.Service.exception;

public class ClientException extends RuntimeException {
    public ClientException(Exception e) {
        super(e);
    }
    public ClientException(String message) {
        super(message);
    }
}
