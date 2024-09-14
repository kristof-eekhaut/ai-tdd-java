package eekhaut.kristof.aitdd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class JavaCodeCompiler {

    public File writeAndCompile(File directory, String javaClassName, String content) {
        File classFile = new File(directory, javaClassName + ".class");
        classFile.delete();

        File javaFile = writeJavaFile(directory, javaClassName, content);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // Check if the compiler is available
        if (compiler == null) {
            throw new RuntimeException("Compiler not available");
        }

        int result = compiler.run(null, null, null, javaFile.getPath());
        if (result != 0) {
            throw new RuntimeException("Compilation failed.");
        }
        log.info("Compilation successful!");

        if (!classFile.isFile()) {
            throw new RuntimeException("Compiled file not found!");
        }

        return classFile;
    }

    private File writeJavaFile(File directory, String javaClassName, String content) {
        File javaFile = new File(directory, javaClassName + ".java");
        try {
            Path dirPath = directory.toPath();
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Files.write(javaFile.toPath(), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write generated code to file", e);
        }

        return javaFile;
    }
}
