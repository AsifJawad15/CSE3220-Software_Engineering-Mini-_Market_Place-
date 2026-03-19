package com.asif.minimarketplace.common.exception;
import com.asif.minimarketplace.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.stream.Collectors;
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/");
    }
    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFound(NotFoundException ex, HttpServletRequest request, Model model) {
        log.warn("Not found: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }
    @ExceptionHandler(com.asif.minimarketplace.common.exception.AccessDeniedException.class)
    public Object handleCustomAccessDenied(
            com.asif.minimarketplace.common.exception.AccessDeniedException ex,
            HttpServletRequest request, Model model) {
        log.warn("Access denied: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/403";
    }
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleSpringAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        log.warn("Spring Security access denied: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied."));
        }
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "You do not have permission to access this resource.");
        return "error/403";
    }
    @ExceptionHandler(InsufficientStockException.class)
    public Object handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request, Model model) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/stock";
    }
    @ExceptionHandler(ValidationException.class)
    public Object handleValidation(ValidationException ex, HttpServletRequest request, Model model) {
        log.warn("Validation error: {}", ex.getMessage());
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleBindValidation(Exception ex, HttpServletRequest request, Model model) {
        String errors;
        if (ex instanceof MethodArgumentNotValidException manve) {
            errors = manve.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else {
            errors = ex.getMessage();
        }
        log.warn("Bind validation error: {}", errors);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errors));
        }
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", errors);
        return "error/400";
    }
    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
        }
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        return "error/500";
    }
}