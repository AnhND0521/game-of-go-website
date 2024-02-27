package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDto {
    private String id;
    private String username;
    private String email;
    private Integer elo;
    private String rankType;
    private Long ranking;
}
