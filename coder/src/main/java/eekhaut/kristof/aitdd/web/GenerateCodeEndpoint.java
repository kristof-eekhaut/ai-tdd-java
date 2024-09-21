package eekhaut.kristof.aitdd.web;

import eekhaut.kristof.aitdd.service.AIJavaCoder;
import eekhaut.kristof.aitdd.service.JavaClass;
import eekhaut.kristof.aitdd.service.JavaCodeService;
import eekhaut.kristof.aitdd.service.JavaCodeService.CompilationResult;
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
        JavaClass generatedTests = generateTests(request.description());
        JavaClass generatedClass = generateImplementation(request.description(), generatedTests);

        CompilationResult compilationCode = writeAndCompile(generatedClass);
        log.info("Compile implementation: \n{}", compilationCode.logging());

        CompilationResult compilationTests = writeAndCompile(generatedTests);
        log.info("Compile tests: \n{}", compilationTests.logging());

        return ResponseEntity.ok("OK");
    }

    private CompilationResult writeAndCompile(JavaClass generatedClass) {
        File javaFile = javaCodeService.writeJavaFile(generatedClass);
        return javaCodeService.compile(generatedClass, javaFile);
    }

    private JavaClass generateTests(String description) {
        return aiJavaCoder.generateTests(GENERATED_CODE_PACKAGE, GENERATED_CODE_CLASS_NAME, description);
    }

    private JavaClass generateImplementation(String description, JavaClass testClass) {
        return aiJavaCoder.generateImplementation(GENERATED_CODE_PACKAGE, GENERATED_CODE_CLASS_NAME, description, testClass);
    }

    public record GenerateCodeRequest(
            String description,
            boolean tests
    ) {}
}
