package io.github.aylesw.igo.service;

import io.github.aylesw.igo.dto.AccountDto;
import io.github.aylesw.igo.dto.SimpleMessage;

public interface AccountService {
    SimpleMessage register(AccountDto accountDto);
    SimpleMessage login(AccountDto accountDto);
}
