package io.candidate.supportdesk.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.candidate.supportdesk.application.*;
import io.candidate.supportdesk.batch.ManifestGate;
import io.candidate.supportdesk.http.PublicModels.*;
import io.candidate.supportdesk.identity.CallerDirectory;
import io.candidate.supportdesk.identity.CallerDirectory.RequestProblem;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public final class PublicApi {
  private final CallerDirectory callers;
  private final DateGate dates;
  private final ManifestGate manifests;
  private final AnswerPolicyQuestion answer;
  private final RunReimbursementBatch batches;
  private final ObjectMapper objectMapper;
  private final Validator validator;

  public PublicApi(
      CallerDirectory callers,
      DateGate dates,
      ManifestGate manifests,
      AnswerPolicyQuestion answer,
      RunReimbursementBatch batches,
      ObjectMapper objectMapper,
      Validator validator) {
    this.callers = callers;
    this.dates = dates;
    this.manifests = manifests;
    this.answer = answer;
    this.batches = batches;
    this.objectMapper = objectMapper;
    this.validator = validator;
  }

  @Operation(summary = "Answer a policy question with eligible cited evidence")
  @PostMapping(
      value = "/answer",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public AnswerResponse answer(
      @RequestHeader("X-Caller-Id") String callerId,
      @Valid @RequestBody AnswerRequest body) {
    var principal = callers.resolve(callerId);
    dates.validate(body.asOf());
    return answer.run(principal, body);
  }

  @Operation(
      summary = "Triage a synchronous batch of TXT/PDF reimbursement submissions",
      description = "Send metadata as JSON text in the multipart 'metadata' field and upload one or more files in the 'files' field.")
  @PostMapping(
      value = "/batches",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public BatchResponse batches(
      @RequestHeader("X-Caller-Id") String callerId,
      @RequestPart("metadata") String metadataJson,
      @RequestPart("files") List<MultipartFile> files) throws Exception {

    var principal = callers.resolve(callerId);
    var metadata = parseAndValidateMetadata(metadataJson);

    dates.validate(metadata.asOf());
    manifests.verify(metadata, files);
    return batches.run(principal, metadata, files);
  }

  private BatchMetadata parseAndValidateMetadata(String metadataJson) {
    final BatchMetadata metadata;
    try {
      metadata = objectMapper.readValue(metadataJson, BatchMetadata.class);
    } catch (JsonProcessingException e) {
      throw new RequestProblem(
          "INVALID_BATCH_METADATA",
          400,
          "The metadata multipart field must contain valid JSON.");
    }

    var violations = validator.validate(metadata);
    if (!violations.isEmpty()) {
      throw new RequestProblem(
          "INVALID_BATCH_METADATA",
          400,
          "Batch metadata validation failed.");
    }
    return metadata;
  }
}
