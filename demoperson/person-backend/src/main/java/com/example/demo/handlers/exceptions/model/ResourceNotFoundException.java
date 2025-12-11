package com.example.demo.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class ResourceNotFoundException extends CustomException {
    private static final String MESSAGE = "Resource not found!";
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String resource) {
        super(MESSAGE, STATUS, resource, new ArrayList<>());
    }
}