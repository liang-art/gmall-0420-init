package com.atguigu.gmall.common.exception;

public class UserExcption extends RuntimeException{
    public UserExcption() {
        super();
    }

    public UserExcption(String message) {
        super(message);
    }
}
