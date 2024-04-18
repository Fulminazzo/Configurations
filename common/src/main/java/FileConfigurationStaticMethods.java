import it.fulminazzo.fulmicollection.objects.Refl;
import it.fulminazzo.fulmicollection.utils.ClassUtils;
import it.fulminazzo.yamlparser.configuration.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * An utility class with methods imported by {@link FileConfiguration}.
 * Should not be used.
 */
class FileConfigurationStaticMethods {

    /**
     * Creates the most appropriate configuration file from the given data.
     *
     * @param rawData the raw data
     * @return the file configuration
     */
    public static @NotNull FileConfiguration newConfiguration(final @NotNull String rawData) {
        return newConfiguration(new ByteArrayInputStream(rawData.getBytes()));
    }

    /**
     * Creates the most appropriate configuration file from the given file.
     *
     * @param file the file
     * @return the file configuration
     */
    public static @NotNull FileConfiguration newConfiguration(final @Nullable File file) {
        return instantiateNewConfiguration(file);
    }

    /**
     * Creates the most appropriate configuration file from the given stream.
     *
     * @param stream the stream
     * @return the file configuration
     */
    public static @NotNull FileConfiguration newConfiguration(final @Nullable InputStream stream) {
        return instantiateNewConfiguration(stream);
    }

    private static @NotNull FileConfiguration instantiateNewConfiguration(final Object @Nullable ... parameters) {
        Set<Class<? extends FileConfiguration>> classes = getConfigurationClasses();
        List<Exception> exceptions = new ArrayList<>();
        for (final Class<? extends FileConfiguration> clazz : classes)
            try {
                Refl<? extends FileConfiguration> refl = new Refl<>(clazz, parameters);
                return refl.getObject();
            } catch (Exception e) {
                if (e.getCause() instanceof FileNotFoundException)
                    throw new RuntimeException(e.getCause());
                exceptions.add(e);
            }
        exceptions.forEach(Throwable::printStackTrace);
        throw new RuntimeException(String.format("Could not create %s from %s", FileConfiguration.class.getSimpleName(), Arrays.toString(parameters)));
    }

    @SuppressWarnings("unchecked")
    private static @NotNull Set<Class<? extends FileConfiguration>> getConfigurationClasses() {
        final Set<Class<? extends FileConfiguration>> classes = new LinkedHashSet<>();
        for (final Class<?> clazz : ClassUtils.findClassesInPackage(FileConfiguration.class.getPackage().getName()))
            if (FileConfiguration.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers()))
                classes.add((Class<? extends FileConfiguration>) clazz);
        return classes;
    }
}