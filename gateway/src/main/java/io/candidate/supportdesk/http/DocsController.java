package io.candidate.supportdesk.http;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class DocsController {
  @GetMapping("/docs")
  public String docs() {
    return "forward:/docs-enhanced.html";
  }
}
