package io.candidate.supportdesk.batch;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
@Component
public final class FingerprintBook {
  public String note(byte[] bytes,String documentId,Map<String,String> firstByDigest) throws Exception {
    String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    String first=firstByDigest.get(digest);
    if(first==null) firstByDigest.put(digest,documentId);
    return first;
  }
}
