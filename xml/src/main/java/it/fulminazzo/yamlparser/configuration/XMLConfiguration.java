package it.fulminazzo.yamlparser.configuration;

import it.fulminazzo.fulmicollection.utils.ReflectionUtils;
import it.fulminazzo.fulmicollection.utils.SerializeUtils;
import it.fulminazzo.tagparser.markup.INodeObject;
import it.fulminazzo.tagparser.nodes.ContainerNode;
import it.fulminazzo.tagparser.nodes.Node;
import it.fulminazzo.tagparser.nodes.exceptions.EmptyNodeException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

/**
 * An implementation of {@link FileConfiguration} to support XML files.
 */
@SuppressWarnings("unchecked")
@Getter
class XMLConfiguration extends FileConfiguration implements INodeObject {
    /**
     * This value will be used when saving lists.
     * Since XML cannot accept tags of the format &lt;\d+&gt;, this will be prepended to the tag name.
     */
    private static final String PADDING = "pre";
    private static final String COLLECTION_ID = "collection";
    private static final String VALUE_CLASS = "value-class";
    private Node rootNode;

    /**
     * Instantiates a new Xml configuration.
     *
     * @param path the path
     */
    public XMLConfiguration(@NotNull String path) {
        super(path);
    }

    /**
     * Instantiates a new Xml configuration.
     *
     * @param file the file
     */
    public XMLConfiguration(@NotNull File file) {
        super(file);
    }

    /**
     * Instantiates a new Xml configuration.
     *
     * @param inputStream the input stream
     */
    public XMLConfiguration(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Instantiates a new Xml configuration.
     *
     * @param file        the file
     * @param inputStream the input stream
     */
    public XMLConfiguration(@Nullable File file, InputStream inputStream) {
        super(file, inputStream);
    }

    @Override
    protected Map<?, ?> load(@NotNull InputStream stream) {
        try {
            this.rootNode = Node.newNode(stream);
        } catch (EmptyNodeException e) {
            return new HashMap<>();
        }
        if (this.rootNode instanceof ContainerNode) {
            Node child = ((ContainerNode) this.rootNode).getChild();
            if (child != null && this.rootNode.getTagName().equals("root"))
                this.rootNode = child;
        }
        Map<?, ?> output = toMap(this.rootNode, true);
        if (output.isEmpty()) return output;

        parseMap((Map<Object, Object>) output);

        return output;
    }

    @Override
    protected void dump(@NotNull Map<?, ?> data, @NotNull Writer writer) {
        try {
            ContainerNode converted = convertToXML("root", data);
            writer.write(converted.toHTML());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the given map to a format acceptable by {@link FileConfiguration}.
     * Specifically, it removes every key prepended with {@link #PADDING} and uses
     * {@link #tryListConversion(Map, Object, Map)} to convert collections.
     *
     * @param map the map
     */
    static void parseMap(Map<Object, Object> map) {
        for (Object key : new ArrayList<>(map.keySet())) {
            if (key == null) continue;
            Object val = map.get(key);
            if (key.toString().matches(PADDING + "\\d+")) {
                map.remove(key);
                map.put(key.toString().substring(PADDING.length()), val);
            }
            if (val instanceof Map) {
                Map<Object, Object> valmap = (Map<Object, Object>) val;
                if (tryListConversion(map, key, valmap)) continue;
                parseMap(valmap);
            }
        }
    }

    /**
     * Analyzes the given value map.
     * If it has a key named "collection" and the others matching the format {@link #PADDING}&lt;number&gt;,
     * it will be interpreted as a list.
     * This means that every value of the map will be added to a list, and finally that list will replace the value map in the original map.
     *
     * @param originalMap    the original map
     * @param key    the key
     * @param valueMap the value map
     * @return the boolean
     */
    static boolean tryListConversion(Map<Object, Object> originalMap, Object key, Map<Object, Object> valueMap) {
        if (valueMap.containsKey(VALUE_CLASS)) {
            if (valueMap.keySet().stream()
                    .filter(k -> !k.equals(VALUE_CLASS))
                    .allMatch(k -> k.toString().matches(PADDING + "\\d+"))) {
                for (Object k : new ArrayList<>(valueMap.keySet()))
                    if (!k.equals(VALUE_CLASS)) {
                        int num = Integer.parseInt(k.toString().substring(PADDING.length()));
                        Object val = valueMap.get(k);
                        if (val instanceof Map) {
                            Map<Object, Object> m = (Map<Object, Object>) val;
                            if (!tryListConversion(valueMap, k, m)) parseMap(m);
                            val = valueMap.get(k);
                        }
                        valueMap.remove(k);
                        valueMap.put(num + "", val);
                    }
                return true;
            }
        } else if (valueMap.containsKey(COLLECTION_ID)) {
            if (valueMap.keySet().stream()
                    .filter(k -> !k.equals(COLLECTION_ID))
                    .allMatch(k -> k.toString().matches(PADDING + "\\d+"))) {
                List<Object> list = new LinkedList<>();
                for (Object k : valueMap.keySet())
                    if (!k.equals(COLLECTION_ID)) {
                        int num = Integer.parseInt(k.toString().substring(PADDING.length()));
                        while (list.size() < num) list.add(null);
                        Object val = valueMap.get(k);
                        if (val instanceof Map) parseMap((Map<Object, Object>) val);
                        list.add(val);
                    }
                originalMap.put(key, list);
                return true;
            }
        }
        return false;
    }

    private ContainerNode convertToXML(String key, Object data) {
        if (key.matches("\\d*")) key = PADDING + key;
        ContainerNode node = new ContainerNode(key);
        if (data instanceof IConfiguration) data = ((IConfiguration) data).toMap();
        if (data instanceof Iterable) {
            int i = 0;
            for (Object object : (Iterable<?>) data) {
                node.addChild(convertToXML(PADDING + i, object));
                i++;
            }
            node.addChild(new Node("collection"));
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            for (Object k : map.keySet())
                node.addChild(convertToXML(k.toString(), map.get(k)));
        } else if (data != null) {
            if (!ReflectionUtils.isPrimitiveOrWrapper(data.getClass()))
                try {
                    String tmp = SerializeUtils.serializeToBase64(data);
                    if (tmp != null) data = tmp;
                } catch (Exception ignored) {}
            node.setText(data.toString());
        }
        return node;
    }

    @Override
    public String toHTML() {
        return this.rootNode == null ? "" : this.rootNode.toHTML();
    }
}
