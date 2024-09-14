package eekhaut.kristof.aitdd.web;

import eekhaut.kristof.aitdd.service.AIJavaCoder;
import eekhaut.kristof.aitdd.service.JavaCodeCompiler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GenerateCodeEndpoint {

    private final File GENERATED_CODE_DIR = new File("tmp/eekhaut/kristof/aitdd/generated");
    private final String GENERATED_CODE_FILE_NAME = "GeneratedCode";

    private final AIJavaCoder aiJavaCoder;
    private final JavaCodeCompiler javaCodeCompiler;

    @PostMapping("/generate-code")
    public ResponseEntity<String> generateCode(@RequestBody GenerateCodeRequest request) {
        String output = aiJavaCoder.generateCode(request.description());
        javaCodeCompiler.writeAndCompile(GENERATED_CODE_DIR, GENERATED_CODE_FILE_NAME, output);
        return ResponseEntity.ok(output);
    }

    public record GenerateCodeRequest(
            String description
    ) {}
}
