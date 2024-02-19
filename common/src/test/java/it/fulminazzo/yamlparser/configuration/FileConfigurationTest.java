package it.fulminazzo.yamlparser.configuration;

import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.yamlparser.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class FileConfigurationTest {
    private static final String FILE_NAME = "build/resources/test/config.yml";

    @BeforeEach
    void setUp() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) FileUtils.createNewFile(file);
    }

    private static Object[][] getConstructorObjects() {
        return new Object[][]{
                new Object[]{new Object[]{""}},
                new Object[]{new Object[]{new File(FILE_NAME)}},
                new Object[]{new Object[]{new ByteArrayInputStream("".getBytes())}}
        };
    }

    @ParameterizedTest
    @MethodSource("getConstructorObjects")
    void testGetConfiguration(Object... constructorParameters) {
        Refl<Class<FileConfiguration>> refl = new Refl<>(FileConfiguration.class);
        FileConfiguration configuration = refl.invokeMethod("newConfiguration", constructorParameters);
        assertInstanceOf(MockFileConfiguration.class, configuration);
    }

    @Test
    void testUnsuccessfulCreation() {
        assertThrowsExactly(RuntimeException.class, () -> {
            try {
                Refl<Class<FileConfiguration>> refl = new Refl<>(FileConfiguration.class);
                refl.invokeMethod("instantiateNewConfiguration", new Class[]{Object[].class}, (Object) new Object[]{10});
            } catch (Exception e) {
                throw unwrapThrowable(e);
            }
        });
    }

    @Test
    void testFileNotFound() {
        assertThrowsExactly(FileNotFoundException.class, () -> {
            try {
                FileConfiguration.newConfiguration(new File("data"));
            } catch (Exception e) {
                throw unwrapThrowable(e);
            }
        });
    }

    private Throwable unwrapThrowable(Throwable throwable) {
        while (throwable.getCause() != null) throwable = throwable.getCause();
        return throwable;
    }
}