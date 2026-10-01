package io.candidate.supportdesk.integration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.candidate.supportdesk.http.PublicModels.*;
import java.util.List;
import java.util.Map;
public final class EngineModels {
  private EngineModels(){}
  public record TrustedContext(String tenant,String role,@JsonProperty("as_of") String asOf){}
  public record QuestionCommand(TrustedContext context,String question){}
  public record DocumentCommand(TrustedContext context,@JsonProperty("document_id") String documentId,String filename,@JsonProperty("payload_b64") String payloadB64){}
  public record DocumentFinding(Extracted extracted,@JsonProperty("field_evidence") Map<String,Quote> fieldEvidence,AnswerResponse policy,List<String> issues){}
}
