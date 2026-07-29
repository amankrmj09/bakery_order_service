package com.blubugtech.bakery_order_service.exception;

import lombok.extern.slf4j.Slf4j;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.blubakery.common.core.exception.handler.ErrorResponse;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.blubakery.common.core.exception.handler.BaseExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(OrderServiceException.class)
    public ResponseEntity<ErrorResponse> handleOrderServiceException(OrderServiceException ex, WebRequest request) {
        log.error("Order service error: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            "ORDER_SERVICE_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(error);
    }

    
    

    

    

    // Error Response Class
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderStatusException(InvalidOrderStatusException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse("INVALID_ORDER_STATUS", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    

}

