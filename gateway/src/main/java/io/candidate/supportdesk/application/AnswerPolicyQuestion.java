package io.candidate.supportdesk.application;
import io.candidate.supportdesk.http.PublicModels.*;
import io.candidate.supportdesk.identity.CallerDirectory.Principal;
import io.candidate.supportdesk.integration.EngineModels.*;
import io.candidate.supportdesk.integration.TriageEngineGateway;
import org.springframework.stereotype.Service;
@Service
public final class AnswerPolicyQuestion {
  private final TriageEngineGateway engine;
  public AnswerPolicyQuestion(TriageEngineGateway engine){this.engine=engine;}
  public AnswerResponse run(Principal p,AnswerRequest r){
    return engine.ask(new QuestionCommand(new TrustedContext(p.tenant(),p.role(),r.asOf()),r.question()));
  }
}
