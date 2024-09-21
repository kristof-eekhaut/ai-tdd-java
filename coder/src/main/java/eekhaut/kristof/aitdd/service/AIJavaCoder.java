package eekhaut.kristof.aitdd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AIJavaCoder {

    private static final SystemPromptTemplate SYSTEM_PROMPT_TEMPLATE_CODE = new SystemPromptTemplate(
            """
            You are an expert Java programmer.
            Your task is to create the code for a Java program that solves the problem described by the user.
            
            # Rules
            Make sure to follow all the rules listed below when writing the code:
            * Respond only with the code. Do not provide additional explanation or text. Do not add formatting or markdown.
            * The main class of your program has to be named "{class_name}".
            * The package name has to be "{package_name}".
            * Make sure to add all import statements at the top of the file right below the package statement.
            * All code has to be provided in the "{class_name}" class and has to be part of the same file.
            * Don't write any tests. Only the production code.
            
            # Tests
            The user can provide a set of unit tests in the description. Your implementation must make all these
            tests pass.
            
            # Example
            Below an example of a valid response is given within the <example> tags:
            <example>{example_code}</example>
            """);

    private static final SystemPromptTemplate SYSTEM_PROMPT_TEMPLATE_TESTS = new SystemPromptTemplate(
            """
            You are an expert Java programmer.
            Your task is to create the tests for a Java program that solves the problem described by the user.
            
            # Rules
            Make sure to follow all the rules listed below when writing the code for the tests:
            * Respond only with the code for the tests. Do not provide additional explanation or text. Do not add formatting or markdown.
            * The main class of the program is named "{class_name}". You do not have to write this class. Assume that it already exists.
            * The test class that have to write has to be named "{test_class_name}".
            * The package name has to be "{package_name}".
            * Make sure to add all import statements at the top of the file right below the package statement.
            * All code has to be provided in the "{test_class_name}" class and has to be part of the same file.
            * Use JUnit 5 and AssertJ libraries for tests.
            * Do not write the production class code. Only write the tests.
            
            # Example
            Below an example of a valid response is given within the <example> tags:
            <example>{example_code}</example>
            """);

    private static final String EXAMPLE_CODE =
            """
            package <<package_name>>;
            
            import java.util.List;
            
            public class <<class_name>> {
            
                public static Integer sum(List<Integer> numbers) {
                    return numbers.stream()
                            .reduce(Integer::sum)
                            .orElse(0);
                }
            }
            """;

    private static final String EXAMPLE_TESTS =
            """
            package <<package_name>>;
            
            import org.junit.jupiter.api.Test;
            
            import java.util.Collections;
            import java.util.List;
            
            import static org.assertj.core.api.Assertions.assertThat;
            
            class <<test_class_name>> {
            
                @Test
                void whenListContainsOneNumber_thenResultIsThatNumber() {
                    List<Integer> numbers = List.of(7);
                    assertThat(<<class_name>>.sum(numbers)).isEqualTo(7);
                }
            
                @Test
                void whenListContainsTwoNumbers_thenNumbersAreAdded() {
                    List<Integer> numbers = List.of(3, 8);
                    assertThat(<<class_name>>.sum(numbers)).isEqualTo(11);
                }
            
                @Test
                void whenListContainsManyNumbers_thenNumbersAreAdded() {
                    List<Integer> numbers = List.of(3, 8, -6, 9);
                    assertThat(<<class_name>>.sum(numbers)).isEqualTo(14);
                }
            
                @Test
                void whenListIsEmpty_theResultIs0() {
                    List<Integer> numbers = Collections.emptyList();
                    assertThat(<<class_name>>.sum(numbers)).isEqualTo(0);
                }
            }
            """;

    private static final PromptTemplate USER_PROMPT_CODE = new PromptTemplate(
            """
            {description}
            
            # Tests
            Below a class with tests is provided within the <tests> tags. Your implementation must make all these tests pass:
            <tests>{tests}</tests>
            """);

    private static final PromptTemplate USER_PROMPT_TESTS = new PromptTemplate("{description}");


    private final ChatClient aiClient;

    AIJavaCoder(ChatClient.Builder chatClientBuilder) {
        this.aiClient = chatClientBuilder.build();
    }

    public JavaClass generateTests(String packageName, String className, String description) {
        JavaClass javaClass = new JavaClass(packageName, className, false);
        JavaClass testClass = javaClass.getTestJavaClass();
        return generateCode(testClass, javaClass, description, true);
    }

    public JavaClass generateImplementation(String packageName, String className, String description, JavaClass testClass) {
        JavaClass javaClass = new JavaClass(packageName, className, false);
        return generateCode(testClass, javaClass, description, false);
    }

    private JavaClass generateCode(JavaClass testClass, JavaClass javaClass, String description, boolean isTest) {
        var systemMessage = systemPrompt(testClass, javaClass, isTest);
        var userMessage = (isTest ? USER_PROMPT_TESTS : USER_PROMPT_CODE).createMessage(Map.of(
                "description", description,
                "tests", testClass.content().orElse("")
        ));
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        log.info("Prompt:\n {}", prompt.getInstructions());

        ChatClient.CallPromptResponseSpec response = aiClient.prompt(prompt).call();
        String output = response.content();

        log.info("Output:\n {}", output);

        return (isTest ? testClass : javaClass).addContent(output);
    }

    private Message systemPrompt(JavaClass testClass, JavaClass javaClass,boolean isTest) {
        SystemPromptTemplate systemPromptTemplate = isTest ?
                SYSTEM_PROMPT_TEMPLATE_TESTS : SYSTEM_PROMPT_TEMPLATE_CODE;
        String codeExample = codeExample(testClass, javaClass, isTest);
        return systemPromptTemplate.createMessage(Map.of(
                "example_code", codeExample,
                "package_name", javaClass.packageName(),
                "class_name", javaClass.className(),
                "test_class_name", testClass.className()
        ));
    }

    public static String codeExample(JavaClass testClass, JavaClass javaClass, boolean isTest) {
        String template = isTest ? EXAMPLE_TESTS : EXAMPLE_CODE;
        return template
                .replaceAll("<<package_name>>", javaClass.packageName())
                .replaceAll("<<class_name>>", javaClass.className())
                .replaceAll("<<test_class_name>>", testClass.className());
    }
}
