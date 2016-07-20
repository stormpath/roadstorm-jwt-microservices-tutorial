package com.stormpath.tutorial.controller;

import com.stormpath.tutorial.model.AccountResponse;
import com.stormpath.tutorial.model.JWTResponse;
import com.stormpath.tutorial.service.AccountService;
import com.stormpath.tutorial.service.SecretService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@RestController
public class MicroServiceController extends BaseController {

    @Autowired
    SecretService secretService;

    @Autowired
    AccountService accountService;

    @RequestMapping("/auth-builder")
    public JWTResponse authBuilder(@RequestBody Map<String, Object> claims) {
        Assert.notNull(
            claims.get(AccountService.USERNAME_CLAIM),
            AccountService.USERNAME_CLAIM + " claim is required."
        );

        Date now = new Date();
        Date exp = new Date(now.getTime() + (1000*60)); // 60 seconds

        String jwt =  Jwts.builder()
            .setHeaderParam("kid", secretService.getMyPublicCreds().getKid())
            .setClaims(claims)
            .setIssuedAt(now)
            .setNotBefore(now)
            .setExpiration(exp)
            .signWith(
                SignatureAlgorithm.RS256,
                secretService.getMyPrivateKey()
            )
            .compact();
        return new JWTResponse(jwt);
    }

    @RequestMapping("/restricted")
    public AccountResponse restricted(HttpServletRequest req) {
        return accountService.getAccount(req);
    }
}
