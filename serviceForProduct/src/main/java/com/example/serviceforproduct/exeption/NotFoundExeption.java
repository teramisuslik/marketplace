package com.example.serviceforproduct.exeption;

public class NotFoundExeption extends RuntimeException{

    public NotFoundExeption(String message){
        super(message);
    }
}
