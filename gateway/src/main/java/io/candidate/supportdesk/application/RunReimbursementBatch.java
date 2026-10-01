package io.candidate.supportdesk.application;
import io.candidate.supportdesk.batch.FingerprintBook;
import io.candidate.supportdesk.http.PublicModels.*;
import io.candidate.supportdesk.identity.CallerDirectory.Principal;
import io.candidate.supportdesk.integration.EngineModels.*;
import io.candidate.supportdesk.integration.TriageEngineGateway;
import io.candidate.supportdesk.integration.TriageEngineGateway.EngineFailure;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*; import java.util.Base64;

@Service
public final class RunReimbursementBatch {
  private static final Logger log=LoggerFactory.getLogger(RunReimbursementBatch.class);
  private final TriageEngineGateway engine; private final FingerprintBook fingerprints;
  public RunReimbursementBatch(TriageEngineGateway engine,FingerprintBook fingerprints){this.engine=engine;this.fingerprints=fingerprints;}
  public BatchResponse run(Principal p,BatchMetadata meta,List<MultipartFile> files) throws Exception {
    Map<String,MultipartFile> byName=new HashMap<>(); files.forEach(f->byName.put(f.getOriginalFilename(),f));
    Map<String,String> firstByDigest=new HashMap<>(); List<ItemResult> out=new ArrayList<>(); int ok=0,failed=0;
    log.info("batch.begin id={} items={}",meta.batchId(),meta.documents().size());
    for(var entry:meta.documents()){
      try{
        byte[] bytes=byName.get(entry.filename()).getBytes();
        if(bytes.length==0) throw new ItemProblem("EMPTY_OR_UNREADABLE","The uploaded file is empty or unreadable.");
        String duplicate=fingerprints.note(bytes,entry.documentId(),firstByDigest);
        var cmd=new DocumentCommand(new TrustedContext(p.tenant(),p.role(),meta.asOf()),entry.documentId(),entry.filename(),Base64.getEncoder().encodeToString(bytes));
        var f=engine.inspect(cmd);
        out.add(new ItemResult(entry.documentId(),"COMPLETED",f.extracted(),f.fieldEvidence(),f.policy(),true,f.issues(),duplicate,null)); ok++;
        log.info("batch.item id={} document={} status=COMPLETED duplicate_of={}",meta.batchId(),entry.documentId(),duplicate);
      } catch(ItemProblem e){ out.add(failed(entry.documentId(),e.code,e.getMessage())); failed++; }
        catch(EngineFailure e){ out.add(failed(entry.documentId(),e.code,e.getMessage())); failed++; }
        catch(Exception e){ out.add(failed(entry.documentId(),"ITEM_PROCESSING_ERROR","The item could not be processed.")); failed++; }
    }
    log.info("batch.end id={} completed={} failed={}",meta.batchId(),ok,failed);
    return new BatchResponse(meta.batchId(),new Summary(out.size(),ok,failed),out);
  }
  private static ItemResult failed(String id,String code,String message){
    return new ItemResult(id,"FAILED",null,Map.of(),null,true,List.of("HUMAN_REVIEW_REQUIRED"),null,new ItemError(code,message));
  }
  private static final class ItemProblem extends RuntimeException{ final String code; ItemProblem(String c,String m){super(m);code=c;} }
}
