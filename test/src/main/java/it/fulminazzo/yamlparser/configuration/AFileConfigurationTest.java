package it.fulminazzo.yamlparser.configuration;

import it.fulminazzo.yamlparser.configuration.exceptions.CannotBeNullException;
import it.fulminazzo.yamlparser.logging.LogMessage;
import it.fulminazzo.yamlparser.parsers.CallableYAMLParser;
import it.fulminazzo.yamlparser.parsers.exceptions.EmptyArrayException;
import it.fulminazzo.yamlparser.utils.FileUtils;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AFileConfigurationTest {
    protected final String filePath;
    protected final String fileExtension;
    protected FileConfiguration configuration;

    public AFileConfigurationTest(String fileExtension) {
        this.filePath = "build/resources/test/config." + fileExtension;
        this.fileExtension = fileExtension;
    }

    @BeforeEach
    void setUp() throws IOException {
        File file = new File(filePath);
        if (file.exists()) FileUtils.deleteFile(file);
        reloadConfiguration();
    }

    protected void reloadConfiguration() throws IOException {
        if (configuration != null) configuration.save();
        File file = new File(filePath);
        if (!file.exists()) FileUtils.createNewFile(file);
        configuration = FileConfiguration.newConfiguration(file);
    }

    private static Object[] getTestValues() {
        return new Object[]{
                "Hello",
                'w',
                10, 10.5f, 10.5d,
                (short) 10, (long) 10,
                (byte) 2, true,
                new Date(),
                UUID.randomUUID(),
                new HashSet<>(Collections.singletonList("Hello world")),
                new ArrayList<>(Collections.singletonList("Hello world")),
                new ArrayList<>(Collections.singletonList("Hello world")),
                new TextMessage("Hello world"),
                Arrays.asList(new TextMessage("Hello"), new TextMessage("world"))
        };
    }

    @ParameterizedTest
    @MethodSource("getTestValues")
    @Order(5)
    void testWriteAndReadEveryType(Object expected) throws IOException {
        String path = expected.getClass().getSimpleName().toLowerCase();
        if (expected.getClass().isArray()) path += "-array";
        // Add possibility for an inner object.
        if (new Random().nextInt(10) >= 5) path = "objects." + path;
        configuration.set(path, expected);
        reloadConfiguration();
        Object readObject;
        try {
            Method getObject = IConfiguration.class.getMethod("get" + expected.getClass().getSimpleName(), String.class);
            readObject = getObject.invoke(configuration, path);
        } catch (Exception e) {
            readObject = configuration.get(path, expected.getClass());
        }
        assertEquals(expected, readObject);
    }

    @ParameterizedTest
    @MethodSource("getTestValues")
    @Order(6)
    void testIsEveryType(Object object) {
        String path = object.getClass().getSimpleName().toLowerCase();
        if (object.getClass().isArray()) path += "-array";
        if (!configuration.contains(path)) path = "objects." + path;
        if (!configuration.contains(path)) throw new IllegalArgumentException("Could not find path " + path);
        boolean readObject;
        try {
            Method isObject = IConfiguration.class.getMethod("is" + object.getClass().getSimpleName(), String.class);
            readObject = (boolean) isObject.invoke(configuration, path);
        } catch (Exception e) {
            readObject = configuration.is(path, object.getClass());
        }
        assertTrue(readObject);
    }

    @Test
    void testGettingNullWithCheckNonNullEnabled() {
        configuration.setNonNull(true);
        assertThrowsExactly(CannotBeNullException.class, () -> configuration.getStringList("non-existing-string"));
    }

    @Test
    void testGettingNullWithCheckNonNullDisabled() {
        assertNull(configuration.getString("non-existing-string"));
    }

    @ParameterizedTest
    @MethodSource("getTestValues")
    @Order(5)
    void testWriteAndReadEveryListType(Object expected) throws IOException {
        String path = expected.getClass().getSimpleName().toLowerCase();
        if (expected.getClass().isArray()) path += "-array";
        path = "lists." + path;
        configuration.set(path, Collections.singletonList(expected));
        reloadConfiguration();
        Object readObject;
        try {
            Method getObject = IConfiguration.class.getMethod("get" + expected.getClass().getSimpleName() + "List", String.class);
            readObject = getObject.invoke(configuration, path);
        } catch (Exception e) {
            readObject = configuration.getList(path, expected.getClass());
        }
        assertEquals(new ArrayList<>(Collections.singletonList(expected)), readObject);
    }

    @Test
    @Order(6)
    void testIsList() {
        ConfigurationSection listSection = configuration.getConfigurationSection("lists");
        if (listSection == null) throw new IllegalArgumentException("Section \"lists\" cannot be null!");
        for (String key : listSection.getKeys()) assertTrue(listSection.isList(key));
    }

    @Test
    @Order(6)
    void testWriteAndReadEnum() throws IOException {
        LogMessage logMessage = LogMessage.UNEXPECTED_CLASS;
        configuration.set("enum", logMessage);
        reloadConfiguration();
        assertEquals(logMessage, configuration.getEnum("enum", LogMessage.class));
    }

    @Test
    @Order(6)
    void testWriteAndReadEnumList() throws IOException {
        List<LogMessage> list = new ArrayList<>(Collections.singletonList(LogMessage.YAML_ERROR));
        configuration.set("lists.enum", list);
        reloadConfiguration();
        assertEquals(list, configuration.getEnumList("lists.enum", LogMessage.class));
    }

    @Test
    @Order(7)
    void testIsEnum() {
        String path = "enum";
        if (!configuration.contains(path)) path = "objects." + path;
        if (!configuration.contains(path)) throw new IllegalArgumentException("Could not find path " + path);
        assertTrue(configuration.isEnum(path, LogMessage.class));
    }

    @Test
    @Order(5)
    void testWriteAndReadArray() throws IOException {
        String[] array = new String[]{"Welcome", "Friend"};
        configuration.set("string-array", array);
        reloadConfiguration();
        assertArrayEquals(array, configuration.get("string-array", String[].class));
    }

    @Test
    @Order(5)
    void testWriteAndReadEmptyArray() throws IOException {
        String[] array = new String[0];
        configuration.set("string-array", array);
        reloadConfiguration();
        assertThrowsExactly(EmptyArrayException.class, () ->
                configuration.get("string-array", String[].class));
    }

    @Test
    @Order(5)
    void testWriteAndReadCallableParser() throws IOException {
        User user = new User(UUID.randomUUID(), "Alex", new Date());
        CallableYAMLParser<User> userYAMLParser = new CallableYAMLParser<>(User.class,
                c -> new User(null, null, null));
        FileConfiguration.addParsers(userYAMLParser);
        configuration.set("user", user);
        reloadConfiguration();
        assertEquals(user, configuration.get("user", User.class));
    }

    @Test
    @Order(10)
    void testNewConfigurationFromNotExistingFile() {
        assertThrowsExactly(RuntimeException.class, () -> FileConfiguration.newConfiguration(new File("not/existing/file." + fileExtension)));
    }

    @Test
    @Order(10)
    void testNewConfigurationFromInputStream() throws IOException {
        reloadConfiguration();
        File file = new File(filePath);
        InputStream inputStream = Files.newInputStream(file.toPath());
        assertEquals(configuration, FileConfiguration.newConfiguration(inputStream));
    }

    @Test
    @Order(10)
    void testNewConfigurationFromString() throws IOException {
        reloadConfiguration();
        String contents = FileUtils.readFileToString(new File(filePath));
        assertNotNull(contents);
        assertEquals(configuration, FileConfiguration.newConfiguration(contents));
    }

    @Test
    void testDottedValues() throws IOException {
        final File parent = new File(filePath).getParentFile();
        final File file = new File(parent, "test-dot." + fileExtension);
        final String fileContents = FileUtils.readFileToString(file) + "\n";
        FileConfiguration config = FileConfiguration.newConfiguration(file);
        assertEquals(1, config.getInteger("dotted\\.value"));
        config.save();
        assertEquals(fileContents.replaceAll("[\n ]", ""), FileUtils.readFileToString(file).replaceAll("[\n ]", ""));
    }

    @Test
    void listShouldBeParsed() {
        ConfigurationSection s1;
        SimpleConfiguration c = new SimpleConfiguration();

        final List<ConfigurationSection> expected = new LinkedList<>();
        s1 = new ConfigurationSection(c, "0");
        s1.set("id", "spigotmc-repo");
        s1.set("url", "https://hub.spigotmc.org/nexus/content/repositories/snapshots/");
        expected.add(s1);
        s1 = new ConfigurationSection(c, "1");
        s1.set("id", "sonatype");
        s1.set("url", "https://oss.sonatype.org/content/groups/public/");
        expected.add(s1);
        s1 = new ConfigurationSection(c, "2");
        s1.set("id", "paper-repo");
        s1.set("url", "https://repo.papermc.io/repository/maven-public/");
        expected.add(s1);

        final FileConfiguration actual = FileConfiguration.newConfiguration(new File("build/resources/test/list." + fileExtension));
        assertEquals(expected, actual.getObjectList("repositories"));
    }

    @Getter
    static class User {
        private final UUID uuid;
        private final String name;
        private final Date registrationDate;

        public User(UUID uuid, String name, Date registrationDate) {
            this.uuid = uuid;
            this.name = name;
            this.registrationDate = registrationDate;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof User)
                return Objects.equals(uuid, ((User) o).getUuid()) &&
                        Objects.equals(name, ((User) o).getName()) &&
                        Objects.equals(registrationDate, ((User) o).getRegistrationDate());
            return super.equals(o);
        }
    }

    protected static class TextMessage implements Serializable {
        private final String message;

        public TextMessage(String message) {
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TextMessage)
                return message.equals(((TextMessage) o).message);
            return super.equals(o);
        }

        @Override
        public String toString() {
            return String.format("%s {\"%s\"}", getClass().getSimpleName(), message);
        }
    }
}