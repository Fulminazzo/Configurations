The following project is a reimplementation of [YAMLParser](https://github.com/Fulminazzo/YAMLParser)
which supports many new data formats.

As of right now, the supported formats are:
- `YAML`
- `JSON`

Each one of them can be retrieved singularly using its name, or by loading the whole project to retrieve all of them simultaneously.

## Importing the whole project
**Maven**:
```xml
<repository>
    <id>Fulminazzo repository</id>
    <url>https://repo.fulminazzo.it/releases</url>
</repository>
```
```xml
<dependency>
    <groupId>it.fulminazzo</groupId>
    <artifactId>Configurations</artifactId>
    <version>{VERSION}</version>
</dependency>
```

**Gradle**:
```groovy
repositories {
    maven { url = 'https://repo.fulminazzo.it/releases' }
}
```
```groovy
dependencies {
    implementation 'it.fulminazzo:Configurations:{VERSION}'
}
```

## Importing just one data format (YAML)
**Maven**:
```xml
<repository>
    <id>Fulminazzo repository</id>
    <url>https://repo.fulminazzo.it/releases</url>
</repository>
```
```xml
<dependency>
    <groupId>it.fulminazzo.Configurations</groupId>
    <artifactId>yaml</artifactId>
    <version>{VERSION}</version>
</dependency>
```

**Gradle**:
```groovy
repositories {
    maven { url = 'https://repo.fulminazzo.it/releases' }
}
```
```groovy
dependencies {
    implementation 'it.fulminazzo.Configurations:yaml:{VERSION}'
}
```