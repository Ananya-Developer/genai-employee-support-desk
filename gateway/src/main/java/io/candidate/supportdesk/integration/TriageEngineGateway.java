package io.candidate.supportdesk.integration;
import io.candidate.supportdesk.http.PublicModels.AnswerResponse;
import io.candidate.supportdesk.integration.EngineModels.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.time.Duration;

@Component
public final class TriageEngineGateway {
  private final RestTemplate http; private final String baseUrl; private final String key;
  public TriageEngineGateway(RestTemplateBuilder builder,
      @Value("${engine.base-url}") String baseUrl,@Value("${engine.key}") String key,
      @Value("${engine.connect-timeout-ms}") long connectMs,@Value("${engine.read-timeout-ms}") long readMs){
    this.http=builder.setConnectTimeout(Duration.ofMillis(connectMs)).setReadTimeout(Duration.ofMillis(readMs)).build();
    this.baseUrl=baseUrl; this.key=key;
  }
  private HttpEntity<Object> entity(Object body){
    HttpHeaders h=new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON); h.set("X-Engine-Key",key); return new HttpEntity<>(body,h);
  }
  public AnswerResponse ask(QuestionCommand cmd){ return post("/engine/question",cmd,AnswerResponse.class); }
  public DocumentFinding inspect(DocumentCommand cmd){ return post("/engine/document",cmd,DocumentFinding.class); }
  private <T>T post(String path,Object body,Class<T> type){
    try { return http.exchange(baseUrl+path,HttpMethod.POST,entity(body),type).getBody(); }
    catch(ResourceAccessException e){ throw new EngineFailure("ENGINE_UNAVAILABLE",503,"Document intelligence service is unavailable."); }
    catch(HttpStatusCodeException e){ throw new EngineFailure("ENGINE_FAILURE",502,"Document intelligence service returned a technical failure."); }
  }
  public static final class EngineFailure extends RuntimeException{
    public final String code; public final int status;
    public EngineFailure(String code,int status,String msg){super(msg);this.code=code;this.status=status;}
  }
}
