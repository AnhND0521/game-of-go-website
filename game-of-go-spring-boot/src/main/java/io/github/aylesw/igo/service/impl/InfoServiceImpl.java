package io.github.aylesw.igo.service.impl;

import io.github.aylesw.igo.dto.GameDto;
import io.github.aylesw.igo.dto.PlayerDto;
import io.github.aylesw.igo.dto.Statistics;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.exception.ResourceNotFoundException;
import io.github.aylesw.igo.repository.AccountRepository;
import io.github.aylesw.igo.repository.GameRepository;
import io.github.aylesw.igo.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InfoServiceImpl implements InfoService {
    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;
    private final ModelMapper mapper;

    @Override
    public List<PlayerDto> getPlayerRankings() {
        Sort sortByEloDesc = Sort.by(Sort.Order.desc("elo"));
        List<Account> rankings = accountRepository.findAll(sortByEloDesc);

        AtomicLong index = new AtomicLong(1);
        AtomicLong ranking = new AtomicLong(1);
        AtomicInteger prevElo = new AtomicInteger(-1);

        return rankings.stream().map(account -> {
            if (prevElo.get() >= 0 && account.getElo() < prevElo.get()) {
                ranking.set(index.get());
            }
            var playerDto = mapper.map(account, PlayerDto.class);
            playerDto.setRanking(ranking.get());
            index.getAndIncrement();
            prevElo.set(account.getElo());
            return playerDto;
        }).toList();
    }

    @Override
    public List<GameDto> getHistory(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new ResourceNotFoundException("account", "username", username);
        }

        return gameRepository.findGamesByAccountId(account.getId()).stream()
                .map(game -> {
                    GameDto dto = mapper.map(game, GameDto.class);
                    dto.setBlackPlayer(mapper.map(game.getBlackPlayer(), PlayerDto.class));
                    dto.setWhitePlayer(mapper.map(game.getWhitePlayer(), PlayerDto.class));
                    return dto;
                }).toList();
    }

    @Override
    public Statistics getStatistics(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new ResourceNotFoundException("account", "username", username);
        }

        int totalGames = gameRepository.countGamesByAccountId(account.getId());
        int wins = gameRepository.countWinsByAccountId(account.getId());
        int draws = gameRepository.countDrawsByAccountId(account.getId());
        int losses = totalGames - wins - draws;
        double winningRate = (double) wins / totalGames;

        var player = getPlayerRankings().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst();
        long ranking = player.isPresent() ? player.get().getRanking() : Long.MIN_VALUE;

        return Statistics.builder()
                .totalGames(totalGames)
                .wins(wins)
                .draws(draws)
                .losses(losses)
                .winningRate(winningRate)
                .elo(account.getElo())
                .rankType(account.getRankType())
                .ranking(ranking)
                .build();
    }
}
