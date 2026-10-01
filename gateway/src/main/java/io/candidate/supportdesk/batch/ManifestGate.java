package io.candidate.supportdesk.batch;
import io.candidate.supportdesk.http.PublicModels.BatchMetadata;
import io.candidate.supportdesk.identity.CallerDirectory.RequestProblem;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
@Component
public final class ManifestGate {
  public void verify(BatchMetadata metadata, List<MultipartFile> files){
    Set<String> ids=new HashSet<>(), names=new HashSet<>();
    for(var d:metadata.documents()){
      if(!ids.add(d.documentId())) throw new RequestProblem("DUPLICATE_DOCUMENT_ID",400,"document_id values must be unique.");
      if(!names.add(d.filename())) throw new RequestProblem("DUPLICATE_MANIFEST_FILENAME",400,"manifest filenames must be unique.");
    }
    List<String> uploaded=files.stream().map(MultipartFile::getOriginalFilename).toList();
    if(uploaded.size()!=new HashSet<>(uploaded).size()) throw new RequestProblem("DUPLICATE_FILE_PART",400,"uploaded filenames must be unique.");
    if(!names.equals(new HashSet<>(uploaded))) throw new RequestProblem("FILE_MANIFEST_MISMATCH",400,"uploaded files must exactly match the manifest.");
  }
}
