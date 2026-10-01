package io.candidate.supportdesk.http;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public final class PublicModels {
  private PublicModels() {}
  public record AnswerRequest(@NotBlank String question, @NotBlank @JsonProperty("as_of") String asOf) {}
  public record Citation(@JsonProperty("chunk_id") String chunkId, String quote) {}
  public record AnswerResponse(String status, String answer, List<Citation> citations) {}
  public record ManifestEntry(@NotBlank @JsonProperty("document_id") String documentId, @NotBlank String filename) {}
  public record BatchMetadata(@NotBlank @JsonProperty("batch_id") String batchId, @NotBlank @JsonProperty("as_of") String asOf,
                              @NotEmpty List<@Valid ManifestEntry> documents) {}
  public record Extracted(String benefit, Integer amount, String currency, String reference) {}
  public record Quote(String quote) {}
  public record ItemError(String code, String message) {}
  public record ItemResult(@JsonProperty("document_id") String documentId,
                           @JsonProperty("processing_status") String processingStatus,
                           Extracted extracted,
                           @JsonProperty("field_evidence") Map<String,Quote> fieldEvidence,
                           AnswerResponse policy,
                           @JsonProperty("review_required") boolean reviewRequired,
                           List<String> issues,
                           @JsonProperty("duplicate_of") String duplicateOf,
                           ItemError error) {}
  public record Summary(int total, int completed, int failed) {}
  public record BatchResponse(@JsonProperty("batch_id") String batchId, Summary summary, List<ItemResult> results) {}
}
