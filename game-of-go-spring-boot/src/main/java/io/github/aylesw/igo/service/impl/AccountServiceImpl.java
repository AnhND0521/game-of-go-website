package io.github.aylesw.igo.service.impl;

import io.github.aylesw.igo.dto.AccountDto;
import io.github.aylesw.igo.dto.AuthData;
import io.github.aylesw.igo.dto.SimpleMessage;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.repository.AccountRepository;
import io.github.aylesw.igo.service.AccountService;
import io.github.aylesw.igo.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final StatusService statusService;
    private static final int DEFAULT_ELO = 1000;
    private static final String DEFAULT_RANK_TYPE = "11K";

    @Override
    public SimpleMessage register(AccountDto accountDto) {
        if (accountRepository.existsByUsername(accountDto.getUsername())) {
            return new SimpleMessage("Username already used", "ERROR");
        }

        accountRepository.save(Account.builder()
                .username(accountDto.getUsername())
                .password(encodePassword(accountDto.getPassword()))
                .elo(DEFAULT_ELO)
                .rankType(DEFAULT_RANK_TYPE)
                .build());
        return new SimpleMessage("Account created successfully");
    }

    @Override
    public SimpleMessage login(AccountDto accountDto) {
        var sessionData = statusService.getSessionData(accountDto.getUsername());
        if (sessionData != null && !sessionData.getStatus().equals("Offline")) {
            return new SimpleMessage("Account is currently logged in", "ERROR");
        }

        Account account = accountRepository.findByUsername(accountDto.getUsername());
        if (account == null) {
            return new SimpleMessage("Account does not exist", "ERROR");
        }

        String password = encodePassword(accountDto.getPassword());
        if (!password.equals(account.getPassword())) {
            return new SimpleMessage("Password is not correct", "ERROR");
        }

        String accessId = UUID.randomUUID().toString();
        statusService.addOnlinePlayer(account, accessId);

        return new SimpleMessage("Logged in successfully", "OK", new AuthData(account.getUsername(), accessId));
    }

    private String encodePassword(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encoding password", e);
        }
    }
}
