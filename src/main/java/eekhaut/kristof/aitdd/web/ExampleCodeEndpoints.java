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

    @PostMapping("/write-code")
    public ResponseEntity<String> writeCode() {
        JavaClass javaClass = new JavaClass(GENERATED_CODE_PACKAGE, GENERATED_CODE_FILE_NAME, false);
        return runFromCodeExample(false);
    }

    @PostMapping("/write-tests")
    public ResponseEntity<String> writeTests() {
        return runFromCodeExample(true);
    }

    private ResponseEntity<String> runFromCodeExample(boolean isTest) {
        JavaClass javaClass = new JavaClass(GENERATED_CODE_PACKAGE, GENERATED_CODE_FILE_NAME, false);
        JavaClass testClass = javaClass.getTestJavaClass();

        String output = AIJavaCoder.codeExample(AIJavaCoder.SystemPromptParams.builder()
                .isTest(isTest)
                .packageName(javaClass.packageName())
                .className(javaClass.className())
                .testClassName(testClass.className())
                .build());

        JavaClass outputClass = (isTest ? testClass : javaClass).addContent(output);
        File javaFile = javaCodeService.writeJavaFile(outputClass);
        javaCodeService.compile(outputClass, javaFile);

        return ResponseEntity.ok(output);
    }
}
