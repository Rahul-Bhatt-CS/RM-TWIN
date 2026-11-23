package com.transformertwin.api;
import com.transformertwin.api.dto.ApiDtos.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.NoSuchElementException;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(NoSuchElementException.class) ResponseEntity<ErrorResponse> missing(Exception e){return response(HttpStatus.NOT_FOUND,"NOT_FOUND",e.getMessage());}
 @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class,ConstraintViolationException.class}) ResponseEntity<ErrorResponse> bad(Exception e){String msg=e instanceof IllegalArgumentException?e.getMessage():"Request validation failed.";return response(HttpStatus.BAD_REQUEST,"BAD_REQUEST",msg);}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ErrorResponse> conflict(Exception e){return response(HttpStatus.CONFLICT,"CONFLICT","A record with the supplied unique identifier already exists or violates a data constraint.");}
 @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> generic(Exception e){return response(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL_ERROR","The request could not be completed.");}
 private ResponseEntity<ErrorResponse> response(HttpStatus s,String code,String msg){return ResponseEntity.status(s).body(new ErrorResponse(code,msg,Instant.now()));}
}
