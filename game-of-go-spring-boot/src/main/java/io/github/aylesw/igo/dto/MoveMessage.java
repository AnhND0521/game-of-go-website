package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class MoveMessage {
    private String username;
    private Integer color;
    private String move;
}
