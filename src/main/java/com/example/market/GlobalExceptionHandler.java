package com.example.market;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

//    @ExceptionHandler({MissingServletRequestParameterException.class})
//    public Mono<String> handleMissingParam(MissingServletRequestParameterException ex, Model model) {
//        logger.error("Missing parameter: {}", ex.getParameterName());
//        String errorMessage = "Отсутствует параметр: " + ex.getParameterName();
//        return Mono.just("redirect:/main/items?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
//    }

    @ExceptionHandler({WebExchangeBindException.class})
    public Mono<String> handleBindException(WebExchangeBindException ex, Model model) {
        logger.error("Binding error: {}", ex.getMessage());
        String errorMessage = "Ошибка в параметрах запроса: " + ex.getMessage();
        return Mono.just("redirect:/main/items?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }

    @ExceptionHandler({Exception.class})
    public Mono<String> handleException(Exception ex, Model model) {
        logger.error("Global exception caught: ", ex);
        String errorMessage = "Ошибка: " + ex.getMessage();
        return Mono.just("redirect:/main/items?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }
}