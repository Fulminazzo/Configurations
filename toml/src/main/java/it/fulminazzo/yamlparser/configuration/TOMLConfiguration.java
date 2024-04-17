package it.fulminazzo.yamlparser.configuration;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

public class TOMLConfiguration extends FileConfiguration {
    
    public TOMLConfiguration(@NotNull String path) {
        super(path);
    }

    public TOMLConfiguration(@NotNull File file) {
        super(file);
    }

    public TOMLConfiguration(InputStream inputStream) {
        super(inputStream);
    }

    public TOMLConfiguration(@Nullable File file, InputStream inputStream) {
        super(file, inputStream);
    }

    @Override
    protected Map<?, ?> load(@NotNull InputStream stream) {
        try {
            Toml toml = new Toml();
            if (stream.available() > 0) toml.read(stream);
            return toml.toMap();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void dump(@NotNull Map<?, ?> data, @NotNull Writer writer) {
        try {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(data, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
