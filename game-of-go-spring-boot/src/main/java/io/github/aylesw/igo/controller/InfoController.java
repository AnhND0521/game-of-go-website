package io.github.aylesw.igo.controller;

import io.github.aylesw.igo.dto.PlayerDto;
import io.github.aylesw.igo.service.GameService;
import io.github.aylesw.igo.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InfoController {
    private final InfoService infoService;

    @GetMapping("/rankings")
    public ResponseEntity<List<PlayerDto>> getPlayerRankings() {
        return ResponseEntity.ok(infoService.getPlayerRankings());
    }


}
