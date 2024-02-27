package io.github.aylesw.igo.service.impl;

import io.github.aylesw.igo.dto.PlayerDto;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.repository.AccountRepository;
import io.github.aylesw.igo.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InfoServiceImpl implements InfoService {
    private final AccountRepository accountRepository;
    private final ModelMapper mapper;

    @Override
    public List<PlayerDto> getPlayerRankings() {
        Sort sortByEloDesc = Sort.by(Sort.Order.desc("elo"));
        List<Account> rankings = accountRepository.findAll(sortByEloDesc);

        AtomicLong index = new AtomicLong(1);
        AtomicLong ranking = new AtomicLong(1);
        AtomicLong prevElo = new AtomicLong(-1);

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
}
