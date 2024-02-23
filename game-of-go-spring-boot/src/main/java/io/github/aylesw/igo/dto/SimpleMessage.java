package io.github.aylesw.igo.dto;

import lombok.Data;

@Data
public class SimpleMessage {
    private String message;
    private String status;
    private Object data;

    public SimpleMessage(String message, String status, Object data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public SimpleMessage(String message, String status) {
        this(message, status, null);
    }

    public SimpleMessage(String message) {
        this(message, "OK");
    }
}
