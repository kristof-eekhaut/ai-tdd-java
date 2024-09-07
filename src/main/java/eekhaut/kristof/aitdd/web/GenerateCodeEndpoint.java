package eekhaut.kristof.aitdd.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class GenerateCodeEndpoint {

    private static final SystemPromptTemplate SYSTEM_PROMPT_TEMPLATE = new SystemPromptTemplate(
            """
            You are an export Java programmer.
            Your task is to create the code for a Java program that solves the problem described by the user.
            
            Respond only with the code. Do not provide additional explanation or text. Do not add formatting.
            
            The main class of your program has to be named "GeneratedCode".
            The package name has to be "eekhaut.kristof.aitdd.generated".
            Make sure to add all import statements at the top of the file.
            All code has to be provided in the "GeneratedCode" class and has to be part of the same file.
            
            Below an example of a valid response is given within the <example> tags:
            
            <example>{example_code}</example>
            """);

    private static final String EXAMPLE_CODE = """
            package eekhaut.kristof.aitdd.generated;
            
            import java.util.List;
            
            public class GeneratedCode {
            
                public static Integer sum(List<Integer> numbers) {
                    return numbers.stream()
                            .reduce(Integer::sum)
                            .orElse(0);
                }
            }
            """;

    private static final PromptTemplate USER_PROMPT_TEMPLATE = new PromptTemplate("{query}");

    private final ChatClient aiClient;

    GenerateCodeEndpoint(ChatClient.Builder chatClientBuilder) {
        this.aiClient = chatClientBuilder.build();
    }

    @PostMapping("/generate-code")
    public ResponseEntity<String> generateCode(@RequestBody GenerateCodeRequest request) {
        var systemMessage = SYSTEM_PROMPT_TEMPLATE.createMessage(Map.of("example_code", EXAMPLE_CODE));
        var userMessage = USER_PROMPT_TEMPLATE.createMessage(Map.of("query", request.description()));
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        log.info("Prompt:\n {}", prompt.getInstructions());

        ChatClient.CallPromptResponseSpec response = aiClient.prompt(prompt).call();
        String output = response.content();

        log.info("Output:\n {}", output);

        return ResponseEntity.ok(output);
    }

    public record GenerateCodeRequest(
            String description
    ) {}
}
