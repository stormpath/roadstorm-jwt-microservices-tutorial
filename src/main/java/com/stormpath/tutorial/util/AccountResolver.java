package com.stormpath.tutorial.util;

import com.stormpath.tutorial.model.AccountResponse;
import com.stormpath.tutorial.service.SecretService;

import javax.servlet.http.HttpServletRequest;

public interface AccountResolver {

    AccountResolver INSTANCE = new DefaultAccountResolver();
    String USERNAME_CLAIM = "userName";

    AccountResponse getAccount(HttpServletRequest req, SecretService secretService);
}
