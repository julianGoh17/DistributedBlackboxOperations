package io.julian.server.components;

import io.julian.server.api.DistributedAlgorithm;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoader {
    private static final Logger log = LogManager.getLogger(ClassLoader.class.getName());

    public static final String JAR_FILE_NOT_FOUND_ERROR = "Could not find JAR file at path '%s'";

    public <T extends DistributedAlgorithm> T loadJar(final String jarFilePath, final String packageName, final Controller controller, final Vertx vertx) throws Exception {
        log.traceEntry(() -> jarFilePath, () -> packageName, () -> controller);
        File jarFile = getJarFile(jarFilePath);
        URLClassLoader child = new URLClassLoader(
            new URL[]{jarFile.toURI().toURL()},
            this.getClass().getClassLoader()
        );
        Class classToLoad = Class.forName(packageName, true, child);
        T algorithm = (T) classToLoad.getConstructor(Controller.class, Vertx.class).newInstance(controller, vertx);
        return log.traceExit(algorithm);
    }

    private File getJarFile(final String jarFilePath) throws FileNotFoundException {
        log.traceEntry(() -> jarFilePath);
        File file = new File(jarFilePath);
        if (!file.exists()) {
            FileNotFoundException exception = new FileNotFoundException(String.format(JAR_FILE_NOT_FOUND_ERROR, jarFilePath));
            log.error(exception);
            throw exception;
        }
        return log.traceExit(file);
    }
}

