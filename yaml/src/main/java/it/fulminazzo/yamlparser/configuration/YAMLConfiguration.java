package it.fulminazzo.yamlparser.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

/**
 * An implementation of {@link FileConfiguration} to support YAML files.
 */
public class YAMLConfiguration extends FileConfiguration {

    /**
     * Instantiates a new Yaml configuration.
     *
     * @param path the path
     */
    public YAMLConfiguration(final @NotNull String path) {
        super(path);
    }

    /**
     * Instantiates a new Yaml configuration.
     *
     * @param file the file
     */
    public YAMLConfiguration(final @NotNull File file) {
        super(file);
    }

    /**
     * Instantiates a new Yaml configuration.
     *
     * @param inputStream the input stream
     */
    public YAMLConfiguration(final @NotNull InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Instantiates a new Yaml configuration.
     *
     * @param file        the file
     * @param inputStream the input stream
     */
    public YAMLConfiguration(final @Nullable File file, final @NotNull InputStream inputStream) {
        super(file, inputStream);
    }

    @Override
    protected Map<?, ?> load(final @NotNull InputStream stream) {
        return newYaml().load(stream);
    }

    @Override
    protected void dump(final @NotNull Map<?, ?> data, final @NotNull Writer writer) {
        newYaml().dump(data, writer);
    }
}
