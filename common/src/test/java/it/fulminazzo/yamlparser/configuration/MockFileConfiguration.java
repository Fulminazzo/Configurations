package it.fulminazzo.yamlparser.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

public class MockFileConfiguration extends FileConfiguration {

    public MockFileConfiguration(@NotNull String path) {
        super(path);
    }

    public MockFileConfiguration(@NotNull File file) {
        super(file);
    }

    public MockFileConfiguration(InputStream inputStream) {
        super(inputStream);
    }

    public MockFileConfiguration(@Nullable File file, InputStream inputStream) {
        super(file, inputStream);
    }

    @Override
    protected Map<?, ?> load(@NotNull InputStream stream) {
        return newYaml().load(stream);
    }

    @Override
    protected void dump(@NotNull Map<?, ?> data, @NotNull Writer writer) {
        newYaml().dump(data, writer);
    }
}
