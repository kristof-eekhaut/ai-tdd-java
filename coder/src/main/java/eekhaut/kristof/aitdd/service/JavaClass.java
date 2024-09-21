package eekhaut.kristof.aitdd.service;

import java.io.File;
import java.util.Optional;

public record JavaClass(
        String packageName,
        String className,
        boolean isTestClass,
        Optional<String> content
) {
    public static final String GENERATION_DIR = "generated-code";
    public static final String CODE_BASE_DIR = GENERATION_DIR + "/src/main/java";
    public static final String TEST_DIR = GENERATION_DIR + "/src/test/java";
    public static final String COMPILATION_DIR = GENERATION_DIR + "/target";
    public static final String CODE_COMPILATION_DIR = COMPILATION_DIR + "/classes";
    public static final String TEST_COMPILATION_DIR = COMPILATION_DIR + "/test-classes";
    public static final String TEST_SUFFIX = "Test";

    public JavaClass(String packageName, String className, boolean isTestClass, String content) {
        this(packageName, className, isTestClass, Optional.ofNullable(content));
    }

    public JavaClass(String packageName, String className, String content) {
        this(packageName, className, false, content);
    }

    public JavaClass(String packageName, String className, boolean isTestClass) {
        this(packageName, className, isTestClass, Optional.empty());
    }

    public JavaClass(String packageName, String className) {
        this(packageName, className, false);
    }

    public JavaClass getTestJavaClass() {
        if (isTestClass) {
            throw new RuntimeException("This is already a test class");
        }
        return new JavaClass(packageName, className + TEST_SUFFIX, true);
    }

    public JavaClass addContent(String content) {
        return new JavaClass(this.packageName, this.className, this.isTestClass, content);
    }

    public File getJavaBaseDir() {
        return new File(isTestClass ? TEST_DIR : CODE_BASE_DIR);
    }

    public File getJavaDir() {
        return new File(getJavaBaseDir(), packageToDir(packageName));
    }

    public File getJavaFile() {
        return new File(getJavaDir(), className + ".java");
    }

    public File getClassBaseDir() {
        return new File(isTestClass ? TEST_COMPILATION_DIR : CODE_COMPILATION_DIR);
    }

    public File getClassDir() {
        return new File(getClassBaseDir(), packageToDir(packageName));
    }

    public File getClassFile() {
        return new File(getClassDir(), className + ".class");
    }

    private String packageToDir(String packageName) {
        return packageName.replace(".", File.separator);
    }
}
