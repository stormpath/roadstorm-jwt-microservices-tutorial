package com.stormpath.tutorial.util;

import com.stormpath.tutorial.model.Account;
import com.stormpath.tutorial.model.AccountResponse;
import com.stormpath.tutorial.service.SecretService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static com.stormpath.tutorial.model.BaseResponse.Status;

public class DefaultAccountResolver implements AccountResolver {

    private static final Logger log = LoggerFactory.getLogger(AccountResolver.class);
    private static final String BEARER_IDENTIFIER = "Bearer "; // space is important

    private Map<String, Account> accounts;

    public DefaultAccountResolver() {
        accounts = new HashMap<>();
        accounts.put("anna", new Account("Anna", "Apple", "anna"));
        accounts.put("betty", new Account("Betty", "Baker", "betty"));
        accounts.put("colin", new Account("Colin", "Cooper", "colin"));
    }

    public AccountResponse getAccount(HttpServletRequest req, SecretService secretService) {
        Assert.notNull(req);
        Assert.notNull(secretService);

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setStatus(Status.ERROR);

        // get JWT as Authorization header
        String authorization = req.getHeader("Authorization");
        Assert.notNull(authorization, "No Authorization Header on the request");

        // make sure it's bearer
        Assert.isTrue(
            authorization.startsWith(BEARER_IDENTIFIER),
            "Authorization header is not a Bearer token: " + authorization
        );

        String jwt = authorization.substring(BEARER_IDENTIFIER.length());

        // verify JWT - will throw JWT Exception if not valid
        Jws<Claims> jws = Jwts.parser()
            .setSigningKeyResolver(secretService.getSigningKeyResolver())
            .parseClaimsJws(jwt);

        // get userName - throw if missing
        String userName;
        if ((userName = (String)jws.getBody().get(USERNAME_CLAIM)) == null) {
            throw new MissingClaimException(
                jws.getHeader(),
                jws.getBody(),
                "Required claim: '" + USERNAME_CLAIM + "' missing on the JWT"
            );
        }

        // see if it exists
        if (accounts.get(userName) == null) {
            String msg = "Account with " + USERNAME_CLAIM + ": " + userName + ", not found";
            log.warn(msg);
            accountResponse.setMessage(msg);
            return accountResponse;
        }

        accountResponse.setMessage("Found Account");
        accountResponse.setStatus(Status.SUCCESS);
        accountResponse.setAccount(accounts.get(userName));

        return accountResponse;
    }
}
