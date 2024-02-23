package io.github.aylesw.igo.controller;

import io.github.aylesw.igo.dto.AccountDto;
import io.github.aylesw.igo.dto.SimpleMessage;
import io.github.aylesw.igo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/accounts/register")
    public ResponseEntity<SimpleMessage> register(@RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(accountService.register(accountDto));
    }

    @PostMapping("/accounts/login")
    public ResponseEntity<SimpleMessage> login(@RequestBody AccountDto accountDto) {
        return ResponseEntity.ok(accountService.login(accountDto));
    }
}
