package com.example.hospital_manager.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;

public class ErrorResponseWriter {

    public static void writeError(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String safeMessage = message != null ? message.replace("\"", "\\\"").replace("\n", " ") : "";
        String safePath = path != null ? path : "";

        String json = "{"
            + "\"timestamp\":\"" + LocalDateTime.now() + "\","
            + "\"status\":" + status.value() + ","
            + "\"error\":\"" + status.getReasonPhrase() + "\","
            + "\"message\":\"" + safeMessage + "\","
            + "\"path\":\"" + safePath + "\""
            + "}";

        response.getWriter().write(json);
    }
}
