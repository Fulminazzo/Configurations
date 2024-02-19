#!/usr/bin/env python3
"""
This script allows reloading the FileConfiguration class from the official YAMLParser GitHub repository.
It is necessary to ensure an always up-to-date class with the modifications introduced by this project.
"""
import os
import re
import requests

STATIC_DEFAULT_METHODS = """/**
     * Load to map.
     *
     * @param stream the stream
     * @return the map
     */
    protected abstract Map<?, ?> load(@NotNull final InputStream stream);

    /**
     * Dump to stream.
     *
     * @param data   the data
     * @param writer the writer
     */
    protected abstract void dump(@NotNull final Map<?, ?> data, @NotNull final Writer writer);

    """

CLASS_COMMENT = "/**\n * A re-implementation of the <a href=\"https://www.github.com/Fulminazzo/YAMLParser\">YAMLParser</a> <b>FileConfiguration</b> class.\n * It provides two new methods to implement: {@link #load(InputStream)} and {@link #dump(Map, Writer)}.\n */"
FILE_DIR = "common/src/main/java/it/fulminazzo/yamlparser/configuration/"
FILE_PATH = FILE_DIR + "FileConfiguration.java"
FILE_URL = "https://raw.githubusercontent.com/Fulminazzo/YAMLParser/main/src/main/java/it/fulminazzo/yamlparser/configuration/FileConfiguration.java"
STATIC_METHODS_CLASS = "common/src/main/java/FileConfigurationStaticMethods.java"
STATIC_IMPORTS = ""
STATIC_METHODS = ""

def load_static_variables():
    global STATIC_METHODS_CLASS
    global STATIC_METHODS
    global STATIC_IMPORTS
    print(f"Reading file content of {STATIC_METHODS_CLASS}")
    with open(STATIC_METHODS_CLASS, "r") as file:
        data = file.readlines()
        print(f"Read {len(data)} bytes of data")
        reached_class = False
        for f in data:
            # Import of FileConfiguration should not be added to FileConfiguration.
            if "FileConfiguration;" in f: continue
            if "FileConfigurationStaticMethods" in f: reached_class = True
            else:
                if reached_class: STATIC_METHODS += f
                else: STATIC_IMPORTS += f

def replace_regex_start(regex: str, content: str, replacement: str) -> str:
    matcher = re.search(regex, content)
    if matcher:
        print(f"Found match for ${regex} at index {matcher.start()}")
        return content[0:matcher.start()] + replacement + content[matcher.start():]
    else:
        raise Exception(f"Could not find any string matching the regex \"{regex}\" in content")

def replace_regex_end(regex: str, content: str, replacement: str) -> str:
    matcher = re.search(regex, content)
    if matcher:
        print(f"Found match for ${regex} at index {matcher.end()}")
        return content[0:matcher.start()] + replacement + content[matcher.end():]
    else:
        raise Exception(f"Could not find any string matching the regex \"{regex}\" in content")

load_static_variables()

response = requests.get(FILE_URL)
print(f"Received response code {response.status_code} from server")
content = response.content.decode("UTF-8")
print(f"Content length: {len(content)}")

# Replace with abstract
content = content.replace("public final class FileConfiguration", "public abstract class FileConfiguration")
# Remove newYaml() calls
content = content.replace("newYaml().", "")
# Replace constructors to prevent external usage
content = content.replace("public FileConfiguration(", "protected FileConfiguration(")
# Remove problematic static method
content = content.replace("\n\n    /**\n     * From string file configuration.\n     *\n     * @param string the string\n     * @return the file configuration\n     */\n    public static @NotNull FileConfiguration fromString(@NotNull String string) {\n        return new FileConfiguration(new ByteArrayInputStream(string.getBytes()));\n    }", "")

print("Replacing comment")
content = replace_regex_end("\/\*\*\n( \*([^\n]+)?\n)* \*\/", content, CLASS_COMMENT)

print("Replacing imports")
STATIC_IMPORTS = "\n".join([s for s in STATIC_IMPORTS.split("\n") if not s in content]) + "\n"
content = replace_regex_start("import", content, STATIC_IMPORTS)

print("Parsing static methods")
content = replace_regex_start("\/\*\*\n     \* [^I]", content, STATIC_DEFAULT_METHODS)
content = replace_regex_end("}\n$", content, STATIC_METHODS)

print(f"Downloading to {FILE_PATH}")
os.makedirs(FILE_DIR, exist_ok=True)
with open(FILE_PATH, "wb") as file:
    file.write(content.encode("utf-8"))
print("Finished download")