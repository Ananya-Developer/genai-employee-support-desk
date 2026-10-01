package io.candidate.supportdesk.http;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.candidate.supportdesk.application.*;
import io.candidate.supportdesk.batch.ManifestGate;
import io.candidate.supportdesk.http.PublicModels.*;
import io.candidate.supportdesk.identity.CallerDirectory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*; import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers=PublicApi.class)
class PublicApiTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json;
 @MockBean CallerDirectory callers; @MockBean DateGate dates; @MockBean ManifestGate manifests;
 @MockBean AnswerPolicyQuestion answer; @MockBean RunReimbursementBatch batches;
 @Test void answerContract() throws Exception{
   var principal=new CallerDirectory.Principal("Atlas","employee");
   when(callers.resolve("atlas-employee-01")).thenReturn(principal);
   when(answer.run(eq(principal),any())).thenReturn(new AnswerResponse("ANSWERED","The annual certification reimbursement limit for employees is INR 25000.",List.of(new Citation("atlas-cert-current","The annual certification reimbursement limit for employees is INR 25000."))));
   mvc.perform(post("/answer").header("X-Caller-Id","atlas-employee-01").contentType(MediaType.APPLICATION_JSON)
      .content("{\"question\":\"certification\",\"as_of\":\"2026-09-21\"}"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ANSWERED")).andExpect(jsonPath("$.citations[0].chunk_id").value("atlas-cert-current"));
 }
}
