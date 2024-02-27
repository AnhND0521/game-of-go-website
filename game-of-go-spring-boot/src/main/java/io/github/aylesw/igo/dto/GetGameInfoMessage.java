package io.github.aylesw.igo.dto;

import lombok.Data;

@Data
public class GetGameInfoMessage {
    private String username;
    private String gameId;
}
