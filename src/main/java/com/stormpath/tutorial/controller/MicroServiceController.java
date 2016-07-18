package com.stormpath.tutorial.controller;

import com.stormpath.tutorial.model.AccountResponse;
import com.stormpath.tutorial.model.JwtResponse;
import com.stormpath.tutorial.service.SecretService;
import com.stormpath.tutorial.util.AccountResolver;
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

    @RequestMapping("/auth-builder")
    public JwtResponse authBuilder(@RequestBody Map<String, Object> claims) {
        Assert.notNull(
            claims.get(AccountResolver.USERNAME_CLAIM),
            AccountResolver.USERNAME_CLAIM + " claim is required."
        );

        Date now = new Date();
        Date exp = new Date(System.currentTimeMillis() + (1000*60)); // 60 seconds

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
        return new JwtResponse(jwt);
    }

    @RequestMapping("/restricted")
    public AccountResponse restricted(HttpServletRequest req) {
        return AccountResolver.INSTANCE.getAccount(req, secretService);
    }
}
