package io.github.aylesw.igo.service.impl;

import io.github.aylesw.igo.dto.GameSetupInfo;
import io.github.aylesw.igo.dto.InviteMessage;
import io.github.aylesw.igo.dto.PlayerDto;
import io.github.aylesw.igo.entity.Account;
import io.github.aylesw.igo.game.GameConfig;
import io.github.aylesw.igo.game.GameInstance;
import io.github.aylesw.igo.game.SimpleGameInstance;
import io.github.aylesw.igo.repository.AccountRepository;
import io.github.aylesw.igo.service.GameService;
import io.github.aylesw.igo.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final AccountRepository accountRepository;
    private final StatusService statusService;
    private final ModelMapper mapper;

    @Override
    public GameSetupInfo setupGame(InviteMessage message) {
        Account player1 = accountRepository.findByUsername(message.getSourcePlayer());
        Account player2 = accountRepository.findByUsername(message.getTargetPlayer());
        Account blackPlayer, whitePlayer;

        Random random = new Random();
        int randomResult = random.nextInt(2);
        if (randomResult == 0) {
            blackPlayer = player1;
            whitePlayer = player2;
        } else {
            blackPlayer = player2;
            whitePlayer = player1;
        }

        GameInstance game = new SimpleGameInstance(blackPlayer, whitePlayer, message.getGameConfig());
        statusService.getSessionData(player1.getUsername()).setGame(game);
        statusService.getSessionData(player2.getUsername()).setGame(game);

        return GameSetupInfo.builder()
                .blackPlayer(mapper.map(blackPlayer, PlayerDto.class))
                .whitePlayer(mapper.map(whitePlayer, PlayerDto.class))
                .gameConfig(message.getGameConfig())
                .build();
    }
}
