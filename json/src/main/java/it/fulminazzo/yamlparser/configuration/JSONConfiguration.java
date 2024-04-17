package it.fulminazzo.yamlparser.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Map;

/**
 * An implementation of {@link FileConfiguration} to support JSON files.
 */
public class JSONConfiguration extends FileConfiguration {

    /**
     * Instantiates a new Json configuration.
     *
     * @param path the path
     */
    public JSONConfiguration(final @NotNull String path) {
        super(path);
    }

    /**
     * Instantiates a new Json configuration.
     *
     * @param file the file
     */
    public JSONConfiguration(final @NotNull File file) {
        super(file);
    }

    /**
     * Instantiates a new Json configuration.
     *
     * @param inputStream the input stream
     */
    public JSONConfiguration(final @NotNull InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Instantiates a new Json configuration.
     *
     * @param file        the file
     * @param inputStream the input stream
     */
    public JSONConfiguration(final @Nullable File file, final @NotNull InputStream inputStream) {
        super(file, inputStream);
    }

    @Override
    protected Map<?, ?> load(final @NotNull InputStream stream) {
        return newJson().fromJson(new InputStreamReader(stream), Map.class);
    }

    @Override
    protected void dump(final @NotNull Map<?, ?> data, final @NotNull Writer writer) {
        newJson().toJson(data, writer);
    }

    /**
     * New json gson.
     *
     * @return the gson
     */
    public static Gson newJson() {
        return new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
    }
}
