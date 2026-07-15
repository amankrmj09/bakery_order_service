package com.blubugtech.bakery_order_service.exception;

 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.blubugtech.common.exception.ErrorResponseDto;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.blubugtech.common.exception.BaseExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderServiceException(OrderServiceException ex, WebRequest request) {
        logger.error("Order service error: {}", ex.getMessage());

        ErrorResponseDto error = new ErrorResponseDto(
            "ORDER_SERVICE_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(error);
    }

    
    

    

    

    // Error Response Class
    
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFoundException(OrderNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto("ORDER_NOT_FOUND", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProductNotFoundException(ProductNotFoundException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto("PRODUCT_NOT_FOUND", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    @ExceptionHandler(InvalidOrderStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidOrderStatusException(InvalidOrderStatusException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto("INVALID_ORDER_STATUS", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientStockException(InsufficientStockException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto("INSUFFICIENT_STOCK", ex.getMessage(), LocalDateTime.now(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    

}

