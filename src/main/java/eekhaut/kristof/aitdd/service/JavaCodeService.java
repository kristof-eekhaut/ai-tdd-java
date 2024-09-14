package eekhaut.kristof.aitdd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
public class JavaCodeService {

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

    public File compile(JavaClass javaClass, File javaFile) {
        File directory = javaClass.getClassDir();
        File classFile = javaClass.getClassFile();

        classFile.delete();

        createDirectory(directory);
        compile(javaFile, javaClass);

        log.info("Compilation successful!");

        if (!classFile.isFile()) {
            throw new RuntimeException("Compiled file not found!");
        }

        return classFile;
    }

    private JavaCompiler compile(File javaSourceFile, JavaClass javaClass) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("Compiler not available");
        }

        try(StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(javaClass.getClassBaseDir()));

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(List.of(javaSourceFile));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);

            if (!task.call()) {
                throw new RuntimeException("Compilation failed.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to configure file manager", e);
        }

        return compiler;
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
