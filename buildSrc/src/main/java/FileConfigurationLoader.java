import it.fulminazzo.yamlparser.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileConfigurationLoader {
    private static final String STATIC_DEFAULT_METHODS = "/**\n" +
            "     * Load to map.\n" +
            "     *\n" +
            "     * @param stream the stream\n" +
            "     * @return the map\n" +
            "     */\n" +
            "    protected abstract Map<?, ?> load(@NotNull final InputStream stream);\n" +
            "\n" +
            "    /**\n" +
            "     * Dump to stream.\n" +
            "     *\n" +
            "     * @param data   the data\n" +
            "     * @param writer the writer\n" +
            "     */\n" +
            "    protected abstract void dump(@NotNull final Map<?, ?> data, @NotNull final Writer writer);\n" +
            "\n" +
            "    ";
    private static final String CLASS_COMMENT = "/**\n * A re-implementation of the <a href=\"https://www.github.com/Fulminazzo/YAMLParser\">YAMLParser</a> <b>FileConfiguration</b> class.\n * It provides two new methods to implement: {@link #load(InputStream)} and {@link #dump(Map, Writer)}.\n */";
    private static final String FILE_DIR = "common/src/main/java/it/fulminazzo/yamlparser/configuration/";
    private static final String FILE_PATH = FILE_DIR + "FileConfiguration.java";
    private static final String FILE_URL = "https://raw.githubusercontent.com/Fulminazzo/YAMLParser/main/src/main/java/it/fulminazzo/yamlparser/configuration/FileConfiguration.java";
    private static final String STATIC_METHODS_CLASS = "common/src/main/java/FileConfigurationStaticMethods.java";
    private static String STATIC_IMPORTS;
    private static String STATIC_METHODS;

    private static void loadStaticVariables() {
        System.out.println("Reading file content of " + STATIC_METHODS_CLASS);
        final String[] read = readFileContent(STATIC_METHODS_CLASS).split("\n");
        System.out.printf("Read %s lines of data%n", read.length);
        boolean reachedClass = false;
        STATIC_IMPORTS = "";
        STATIC_METHODS = "";
        for (String r : read) {
            r += "\n";
            if (!r.contains("FileConfiguration;"))
                if (r.contains("FileConfigurationStaticMethods")) reachedClass = true;
                else if (reachedClass) STATIC_METHODS += r;
                else STATIC_IMPORTS += r;
        }
    }

    private static String replaceRegexStart(final String regex, final String content, final String replacement) {
        Matcher matcher = Pattern.compile(regex).matcher(content);
        if (matcher.find()) {
            final int index = matcher.start();
            System.out.printf("Found match for \"%s\" at index %s%n", regex, index);
            return content.substring(0, matcher.start()) + replacement + content.substring(index);
        } else throw new RuntimeException(String.format("Could not find any string matching the regex \"%s\" in content", regex));
    }

    private static String replaceRegexEnd(final String regex, final String content, final String replacement) {
        Matcher matcher = Pattern.compile(regex).matcher(content);
        if (matcher.find()) {
            final int index = matcher.end();
            System.out.printf("Found match for \"%s\" at index %s%n", regex, index);
            return content.substring(0, matcher.start()) + replacement + content.substring(index);
        } else throw new RuntimeException(String.format("Could not find any string matching the regex \"%s\" in content", regex));
    }

    public static void loadFileConfiguration() throws IOException {
        final File outputFile = new File(getParentFile(), FILE_PATH);
        if (outputFile.exists()) FileUtils.deleteFile(outputFile);
        loadStaticVariables();

        String content = readFileContentWeb(FILE_URL);

        // Replace with abstract
        content = content.replace("public final class FileConfiguration", "public abstract class FileConfiguration");
        // Remove newYaml() calls
        content = content.replace("newYaml().", "");
        // Replace constructors to prevent external usage
        content = content.replace("public FileConfiguration(", "protected FileConfiguration(");
        // Remove problematic static method
        content = content.replace("\n\n    /**\n     * From string file configuration.\n     *\n     * @param string the string\n     * @return the file configuration\n     */\n    public static @NotNull FileConfiguration fromString(@NotNull String string) {\n        return new FileConfiguration(new ByteArrayInputStream(string.getBytes()));\n    }", "");

        System.out.println("Replacing comment");
        content = replaceRegexEnd("\\/\\*\\*\n( \\*([^\n]+)?\n)* \\*\\/", content, CLASS_COMMENT);

        System.out.println("Replacing imports");
        final String[] tmp = STATIC_IMPORTS.split("\n");
        STATIC_IMPORTS = "";
        for (final String s : tmp)
            if (!content.contains(s)) STATIC_IMPORTS += s + "\n";
        content = replaceRegexStart("import", content, STATIC_IMPORTS);

        System.out.println("Parsing static methods");
        content = replaceRegexStart("\\/\\*\\*\n     \\* [^I]", content, STATIC_DEFAULT_METHODS);
        content = replaceRegexEnd("}\n*$", content, STATIC_METHODS);

        System.out.println("Downloading to " + FILE_PATH);
        FileUtils.createNewFile(outputFile);
        FileUtils.writeToFile(outputFile, content);
        System.out.println("Finished download");
    }

    private static String readFileContentWeb(final String link) {
        try {
            final URL url = new URL(link);
            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            final int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.printf("Received response code %s from server%n", responseCode);
                final BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                final StringBuilder content = new StringBuilder();
                while ((inputLine = input.readLine()) != null) content.append(inputLine).append("\n");
                input.close();

                System.out.println("Content length: " + content.length());
                return content.toString();
            } else throw new Exception("Received response code: " + responseCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String readFileContent(final String fileName) {
        try {
            File file = new File(getParentFile(), fileName);
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getParentFile() {
        return new File("").getAbsoluteFile();
    }
}