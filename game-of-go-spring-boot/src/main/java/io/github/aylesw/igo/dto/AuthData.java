package io.github.aylesw.igo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthData {
    private String username;
    private String accessId;
}
