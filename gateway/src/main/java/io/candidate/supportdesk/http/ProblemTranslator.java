package io.candidate.supportdesk.http;
import io.candidate.supportdesk.identity.CallerDirectory.RequestProblem;
import io.candidate.supportdesk.integration.TriageEngineGateway.EngineFailure;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public final class ProblemTranslator {
  @ExceptionHandler(RequestProblem.class) ResponseEntity<ProblemDetail> request(RequestProblem e){return problem(e.status,e.code,e.getMessage());}
  @ExceptionHandler(EngineFailure.class) ResponseEntity<ProblemDetail> engine(EngineFailure e){return problem(e.status,e.code,e.getMessage());}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ProblemDetail> validation(){return problem(400,"INVALID_REQUEST","Request validation failed.");}
  private ResponseEntity<ProblemDetail> problem(int status,String code,String message){
    ProblemDetail p=ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),message); p.setProperty("code",code); return ResponseEntity.status(status).body(p);
  }
}
