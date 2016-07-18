package com.stormpath.tutorial.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse extends BaseResponse {
    private String exceptionType;
    private String jwt;
    private Jws<Claims> jws;

    public JwtResponse() {}

    public JwtResponse(String jwt) {
        this.jwt = jwt;
        setStatus(Status.SUCCESS);
    }

    public JwtResponse(Jws<Claims> jws) {
        this.jws = jws;
        setStatus(Status.SUCCESS);
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Jws<Claims> getJws() {
        return jws;
    }

    public void setJws(Jws<Claims> jws) {
        this.jws = jws;
    }
}
