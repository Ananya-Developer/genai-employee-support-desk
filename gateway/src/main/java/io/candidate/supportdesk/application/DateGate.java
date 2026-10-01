package io.candidate.supportdesk.application;
import io.candidate.supportdesk.identity.CallerDirectory.RequestProblem;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
@Component
public final class DateGate {
  private static final DateTimeFormatter STRICT=DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
  public void validate(String value){
    try { LocalDate.parse(value,STRICT); }
    catch(Exception e){ throw new RequestProblem("INVALID_AS_OF",400,"as_of must be a real date in YYYY-MM-DD format."); }
  }
}
