package com.example.ExpenseTracker.exception;
import com.example.ExpenseTracker.dto.GlobalExceptionRes;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static java.time.LocalDateTime.now;


@RestControllerAdvice
@Slf4j
public class ControllerValidation {

     public ResponseEntity<GlobalExceptionRes<Map<String, String>>> buildErrorList(String field, String message){
        Map<String, String> listErrors = new HashMap<>();
        listErrors.put(field, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GlobalExceptionRes<>(400, listErrors, LocalDate.now()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> listErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            listErrors.put(error.getField(), error.getDefaultMessage());
        });
        GlobalExceptionRes<Map<String, String>> exceptionRes = new GlobalExceptionRes<>(400, listErrors, LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleConstraintValidationExceptions(ConstraintViolationException ex){
        Map<String, String> listErrors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            listErrors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        GlobalExceptionRes<Map<String, String>> exceptionRes = new GlobalExceptionRes<>(400, listErrors, LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        log.atWarn()
                .setMessage("Registration attempt failed: Email already exists")
                .addKeyValue("eventType", "DUPLICATE_EMAIL")
                .log();
        GlobalExceptionRes<Map<String, String>> exceptionRes = new GlobalExceptionRes<>(409, Map.of("email", ex.getMessage()), LocalDate.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionRes);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleImageSizeException(MaxUploadSizeExceededException ex){
        log.warn("File upload too large");
        GlobalExceptionRes<Map<String, String>> exceptionRes = new GlobalExceptionRes<>(413,Map.of("imageProfile", "The uploaded file is too large! Maximum allowed size is 5MB"), LocalDate.now());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(exceptionRes);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleBadCredentials(BadCredentialsException ex){
        log.atWarn()
                .setMessage("Authentication failed")
                .addKeyValue("eventType", "AUTHENTICATION_FAILED")
                .log();
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(401,"Invalid credentials", LocalDate.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionRes);
    }


    @ExceptionHandler(InvalidAccountUpdateException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleInvalidInputAccountUpdate(InvalidAccountUpdateException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(400,ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleReportNotFound(ReportNotFoundException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404,"Resource not found", LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleWrongFormat(HttpMessageNotReadableException ex){
        String formatErrorMessage = "Invalid data format, check your input fields";
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(400, formatErrorMessage, LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleMethodNotAllowed(Exception ex) {
        String errorMessage = "Check HTTP method (GET/POST/PUT/etc)";
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(405, errorMessage, LocalDate.now());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(exceptionRes);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleUserNotFoundException(UserNotFoundException ex){
        log.atWarn()
                .setMessage("User not found")
                .addKeyValue("eventType", "USER_NOT_FOUND")
                .log();
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404,"User not found", LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }

    @ExceptionHandler(CloudinaryException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleCloudinaryUpload(CloudinaryException ex){
        log.atError()
                .setMessage("Cloudinary upload failed")
                .addKeyValue("eventType", "CLOUDINARY_UPLOAD_FAILED")
                .setCause(ex)
                .log();
        return buildErrorList("imageProfile", ex.getMessage()); 
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleInvalidFileType(InvalidFileTypeException ex){
        log.atWarn()
                .setMessage("Invalid file upload attempt")
                .addKeyValue("eventType", "INVALID_FILE_TYPE")
                .log();
        return buildErrorList("imageProfile", ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleDatabaseExceptions(DataAccessException ex){
        log.atError()
                .setMessage("Database error occurred")
                .addKeyValue("eventType", "DATABASE_ERROR")
                .setCause(ex)
                .log();
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(500,"Something went wrong, internal server error", LocalDate.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionRes);
    }

    @ExceptionHandler(FileReadException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleFileReadException(FileReadException ex){
        log.atError()
                .setMessage("Failed to read uploaded file")
                .addKeyValue("eventType", "FILE_READ_ERROR")
                .setCause(ex)
                .log();
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(500, ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionRes);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalExceptionRes<String>> dbConstraintViolationException(DataIntegrityViolationException ex){
        log.atWarn()
                .setMessage("Database constraint violation")
                .addKeyValue("eventType", "DB_CONSTRAINT_VIOLATION")
                .log();
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(409,"Constraint violation occurred", LocalDate.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionRes);
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleExpenseNotFoundException(ExpenseNotFoundException ex) {
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404, "Expense not found", LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleCategoryNotFound(CategoryNotFoundException ex) {
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404, ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleResourceNotFoundException(ResourceNotFoundException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404,ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }
}

