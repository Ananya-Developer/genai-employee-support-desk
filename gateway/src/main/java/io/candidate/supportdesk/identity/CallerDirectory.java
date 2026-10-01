package io.candidate.supportdesk.identity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.Map;

@Component
public final class CallerDirectory {
  public record Principal(String tenant, String role) {}
  private final Map<String,Principal> entries;
  public CallerDirectory(ObjectMapper mapper) throws Exception {
    try(InputStream in=new ClassPathResource("caller-directory.json").getInputStream()) {
      entries=Map.copyOf(mapper.readValue(in,new TypeReference<Map<String,Principal>>(){}));
    }
  }
  public Principal resolve(String callerId) {
    if(callerId==null || callerId.isBlank()) throw new RequestProblem("MISSING_CALLER",400,"X-Caller-Id is required.");
    Principal p=entries.get(callerId);
    if(p==null) throw new RequestProblem("UNKNOWN_CALLER",401,"Caller is not recognized.");
    return p;
  }
  public static final class RequestProblem extends RuntimeException {
    public final String code; public final int status;
    public RequestProblem(String code,int status,String message){super(message);this.code=code;this.status=status;}
  }
}
