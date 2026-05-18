package com.example.ExpenseTracker.exception;
import com.example.ExpenseTracker.dto.GlobalExceptionRes;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static java.time.LocalDateTime.now;


@RestControllerAdvice
public class ControllerValidation {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalExceptionRes<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> listErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            listErrors.put(error.getField(), error.getDefaultMessage());
        });
        GlobalExceptionRes<Map<String, String>> exceptionRes = new GlobalExceptionRes<>(400, listErrors, LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleBadCredentials(BadCredentialsException ex){
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
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404,"User not found", LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }

    @ExceptionHandler(CloudinaryException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleCloudinaryUpload(CloudinaryException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(400, ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleInvalidFileType(InvalidFileTypeException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(400, ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionRes);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleDatabaseExceptions(DataAccessException ex){
        ex.printStackTrace();
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(500,"Something went wrong, internal server error", LocalDate.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionRes);
    }

    @ExceptionHandler(FileReadException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleFileReadException(FileReadException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(500, ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionRes);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalExceptionRes<String>> dbConstraintViolationException(DataIntegrityViolationException ex){
        System.err.println("DB ERROR: " + ex.getMostSpecificCause().getMessage());
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(409,"Constraint violation occurred", LocalDate.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionRes);
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleExpenseNotFoundException(ExpenseNotFoundException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(404,"Expense not found", LocalDate.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionRes);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<GlobalExceptionRes<String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex){
        GlobalExceptionRes<String> exceptionRes = new GlobalExceptionRes<>(409, ex.getMessage(), LocalDate.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionRes);
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

