package eekhaut.kristof.aitdd.web;

import eekhaut.kristof.aitdd.service.AIJavaCoder;
import eekhaut.kristof.aitdd.service.JavaClass;
import eekhaut.kristof.aitdd.service.JavaCodeService;
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

    private static final String GENERATED_CODE_PACKAGE = "eekhaut.kristof.aitdd.generated";
    private static final String GENERATED_CODE_CLASS_NAME = "GeneratedCode";

    private final AIJavaCoder aiJavaCoder;
    private final JavaCodeService javaCodeService;

    @PostMapping("/generate-code")
    public ResponseEntity<String> generateCode(@RequestBody GenerateCodeRequest request) {
        JavaClass generatedClass = aiJavaCoder.generateCode(GENERATED_CODE_PACKAGE,
                GENERATED_CODE_CLASS_NAME, request.description(), request.tests());

        File javaFile = javaCodeService.writeJavaFile(generatedClass);
        javaCodeService.compile(generatedClass, javaFile);

        return ResponseEntity.ok(generatedClass.content().orElse(null));
    }

    public record GenerateCodeRequest(
            String description,
            boolean tests
    ) {}
}
