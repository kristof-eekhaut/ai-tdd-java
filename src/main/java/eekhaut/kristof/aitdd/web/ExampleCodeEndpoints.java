package eekhaut.kristof.aitdd.web;

import eekhaut.kristof.aitdd.service.AIJavaCoder;
import eekhaut.kristof.aitdd.service.JavaClass;
import eekhaut.kristof.aitdd.service.JavaCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ExampleCodeEndpoints {

    private static final String GENERATED_CODE_PACKAGE = "eekhaut.kristof.aitdd.generated";
    private static final String GENERATED_CODE_FILE_NAME = "GeneratedCode";

    private final JavaCodeService javaCodeService;

    @PostMapping("/example")
    public ResponseEntity<String> example() {
        JavaClass javaClass = new JavaClass(GENERATED_CODE_PACKAGE, GENERATED_CODE_FILE_NAME, false);
        JavaClass testClass = javaClass.getTestJavaClass();

        JavaClass generatedTests = generateTests(testClass, javaClass);
        JavaClass generatedClass = generateImplementation(testClass, javaClass);

        JavaCodeService.CompilationResult compilationCode = writeAndCompile(generatedClass);
        log.info("Compile implementation: \n{}", compilationCode.logging());

        JavaCodeService.CompilationResult compilationTests = writeAndCompile(generatedTests);
        log.info("Compile tests: \n{}", compilationTests.logging());

        return ResponseEntity.ok("OK");
    }

    private JavaCodeService.CompilationResult writeAndCompile(JavaClass generatedClass) {
        File javaFile = javaCodeService.writeJavaFile(generatedClass);
        return javaCodeService.compile(generatedClass, javaFile);
    }

    private JavaClass generateTests(JavaClass testClass, JavaClass javaClass) {
        String output = AIJavaCoder.codeExample(testClass, javaClass, true);
        return testClass.addContent(output);
    }

    private JavaClass generateImplementation(JavaClass testClass, JavaClass javaClass) {
        String output = AIJavaCoder.codeExample(testClass, javaClass, false);
        return javaClass.addContent(output);
    }
}
