package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RefundAlreadyExistsException extends RuntimeException {

    public RefundAlreadyExistsException(Long transactionId) {
        super("La transacción ya posee una devolución. Id: " + transactionId);
    }

}