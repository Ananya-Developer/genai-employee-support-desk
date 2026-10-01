package io.candidate.supportdesk.http;
import io.candidate.supportdesk.application.*;
import io.candidate.supportdesk.batch.ManifestGate;
import io.candidate.supportdesk.http.PublicModels.*;
import io.candidate.supportdesk.identity.CallerDirectory;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
public final class PublicApi {
  private final CallerDirectory callers; private final DateGate dates; private final ManifestGate manifests;
  private final AnswerPolicyQuestion answer; private final RunReimbursementBatch batches;
  public PublicApi(CallerDirectory c,DateGate d,ManifestGate m,AnswerPolicyQuestion a,RunReimbursementBatch b){callers=c;dates=d;manifests=m;answer=a;batches=b;}

  @Operation(summary="Answer a policy question with eligible cited evidence")
  @PostMapping(value="/answer",consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
  public AnswerResponse answer(@RequestHeader("X-Caller-Id") String callerId,@Valid @RequestBody AnswerRequest body){
    var principal=callers.resolve(callerId); dates.validate(body.asOf()); return answer.run(principal,body);
  }

  @Operation(summary="Triage a synchronous batch of TXT/PDF reimbursement submissions")
  @PostMapping(value="/batches",consumes=MediaType.MULTIPART_FORM_DATA_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
  public BatchResponse batches(@RequestHeader("X-Caller-Id") String callerId,
      @Valid @RequestPart("metadata") BatchMetadata metadata,@RequestPart("files") List<MultipartFile> files) throws Exception {
    var principal=callers.resolve(callerId); dates.validate(metadata.asOf()); manifests.verify(metadata,files); return batches.run(principal,metadata,files);
  }
}
