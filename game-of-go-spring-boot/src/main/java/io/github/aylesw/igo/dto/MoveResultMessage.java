package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveResultMessage {
    private Integer color;
    private String move;
    private List<String> captured;
}
