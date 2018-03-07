package im.toss.http.parser;

import java.util.Objects;

public class KvPair {

    private final String key;
    private final String value;

    public static KvPair parse(String input) {
        String[] pairs = input.split("=", 2);
        if (pairs.length >= 2) {
            return new KvPair(pairs[0].trim(), pairs[1].trim());
        }  else {
            return new KvPair("", input);
        }
    }

    KvPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KvPair{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KvPair kvPair = (KvPair) o;
        return Objects.equals(key, kvPair.key) && Objects.equals(value, kvPair.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, value);
    }
}
