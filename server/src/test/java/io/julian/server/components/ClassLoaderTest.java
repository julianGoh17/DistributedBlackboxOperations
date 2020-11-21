package io.julian.server.components;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

public class ClassLoaderTest {
    private static final String CORRECT_JAR_PATH = String.format("%s/../test/target/test-1.0-SNAPSHOT-jar-with-dependencies.jar", System.getProperty("user.dir"));
    private static final String PACKAGE_NAME = "io.julian.Main";

    @Test
    public void TestClassLoaderThrowsExceptionWhenCannotFindTestJar() {
        ClassLoader loader = new ClassLoader();
        String incorrectFilePath = String.format("%s-random", CORRECT_JAR_PATH);
        Controller controller = new Controller();
        try {
            loader.loadJar(incorrectFilePath, PACKAGE_NAME, controller);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(String.format(ClassLoader.JAR_FILE_NOT_FOUND_ERROR, incorrectFilePath), e.getMessage());
            Assert.assertEquals(FileNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void TestClassLoaderCanLoadJar() throws Exception {
        ClassLoader loader = new ClassLoader();
        Controller controller = new Controller();
        Assert.assertNotNull(loader.loadJar(CORRECT_JAR_PATH, PACKAGE_NAME, controller));
    }
}
