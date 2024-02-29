package io.github.aylesw.igo.service;

import io.github.aylesw.igo.dto.GameDto;
import io.github.aylesw.igo.dto.PlayerDto;
import io.github.aylesw.igo.dto.Statistics;

import java.util.List;

public interface InfoService {
    List<PlayerDto> getPlayerRankings();
    List<GameDto> getHistory(String username);
    Statistics getStatistics(String username);
}
