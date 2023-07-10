package com.example.common;


import org.springframework.stereotype.Component;

//业务异常

public class CustomException extends RuntimeException{
    public  CustomException(String message){
        super(message);
    }
}
