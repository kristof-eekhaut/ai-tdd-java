package eekhaut.kristof.aitdd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class JavaCodeService {

    public record CompilationResult(
            boolean success,
            String logging,
            JavaClass javaClass,
            File javaFile,
            File classfile
    ) {}

    private record CompileOutput(
            boolean success,
            String logging
    ) {}

    public File writeJavaFile(JavaClass javaClass) {
        String content = javaClass.content()
                .orElseThrow(() -> new RuntimeException("Class has no content!"));

        File directory = javaClass.getJavaDir();
        File javaFile = javaClass.getJavaFile();

        createDirectory(directory);

        try {
            Files.write(javaFile.toPath(), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write generated code to file", e);
        }

        return javaFile;
    }

    public CompilationResult compile(JavaClass javaClass, File javaFile) {
        File directory = javaClass.getClassDir();
        File classFile = javaClass.getClassFile();

        classFile.delete();

        createDirectory(directory);
        CompileOutput compileOutput = compile(javaFile, javaClass);

        if (compileOutput.success() && !classFile.isFile()) {
            throw new RuntimeException("Compiled file not found!");
        }

        return new CompilationResult(compileOutput.success(), compileOutput.logging(), javaClass, javaFile, classFile);
    }

    private CompileOutput compile(File javaSourceFile, JavaClass javaClass) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Compiler not available");
        }

        boolean compilationSuccess = false;
        ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
        try (PrintWriter printWriter = new PrintWriter(logOutputStream)) {
            try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {

                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(javaClass.getClassBaseDir()));

                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(javaSourceFile));
                
                List<String> options = List.of("-proc:none", "-classpath", assembleCompilationClasspath());
                JavaCompiler.CompilationTask task = compiler.getTask(printWriter, fileManager, null, options, null, compilationUnits);

                compilationSuccess = task.call();
            } catch (IOException e) {
                throw new RuntimeException("Failed to configure file manager", e);
            }
        }

        return new CompileOutput(compilationSuccess, logOutputStream.toString());
    }

    private String assembleCompilationClasspath() {
        String mvnClasspath;
        try {
            mvnClasspath = new String(Files.readAllBytes(Paths.get(JavaClass.COMPILATION_DIR + "/classpath.txt")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to configure file manager", e);
        }
        return String.join(":", JavaClass.CODE_COMPILATION_DIR, JavaClass.TEST_COMPILATION_DIR, mvnClasspath);
    }

    private void createDirectory(File directory) {
        try {
            Path dirPath = directory.toPath();
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory", e);
        }
    }
}
