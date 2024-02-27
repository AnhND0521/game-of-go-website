package io.github.aylesw.igo.service;

import io.github.aylesw.igo.dto.PlayerDto;

import java.util.List;

public interface InfoService {
    List<PlayerDto> getPlayerRankings();
}
