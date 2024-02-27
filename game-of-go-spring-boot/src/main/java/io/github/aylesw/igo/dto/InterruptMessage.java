package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterruptMessage {
    private String username;
    private int color;
    private String requestType;
    private String reply;
}
