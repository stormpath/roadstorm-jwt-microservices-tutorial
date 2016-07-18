package com.stormpath.tutorial.util;

import com.stormpath.tutorial.model.AccountResponse;
import com.stormpath.tutorial.service.SecretService;

import javax.servlet.http.HttpServletRequest;

public interface AccountResolver {

    public static final AccountResolver INSTANCE = new DefaultAccountResolver();

    public AccountResponse getAccount(HttpServletRequest req, SecretService secretService);
}
