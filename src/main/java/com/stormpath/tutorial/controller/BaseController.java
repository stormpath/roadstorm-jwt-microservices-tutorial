package com.stormpath.tutorial.controller;

import com.stormpath.tutorial.exception.UnauthorizedException;
import com.stormpath.tutorial.model.JWTResponse;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BaseController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        SignatureException.class, MalformedJwtException.class, JwtException.class, IllegalArgumentException.class
    })
    public JWTResponse badRequest(Exception e) {
        return processException(e);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public JWTResponse unauthorized(Exception e) {
        return processException(e);
    }

    private JWTResponse processException(Exception e) {
        JWTResponse response = new JWTResponse();
        response.setStatus(JWTResponse.Status.ERROR);
        response.setMessage(e.getMessage());
        response.setExceptionType(e.getClass().getName());

        return response;
    }
}
