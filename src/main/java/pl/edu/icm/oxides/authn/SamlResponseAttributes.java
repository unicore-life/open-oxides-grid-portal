package pl.edu.icm.oxides.authn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SamlResponseAttributes {
    private Map<String, List<String>> attributes = new ConcurrentHashMap<>();

    public void put(String name, String value) {
        if (attributes.get(name) == null) {
            attributes.put(name, Collections.synchronizedList(new ArrayList()));
        }
        attributes.get(name).add(value);
    }

    public void put(String name, List<String> values) {
        if (attributes.get(name) == null) {
            attributes.put(name, Collections.synchronizedList(new ArrayList()));
        }
        attributes.get(name).addAll(values);
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void merge(SamlResponseAttributes other) {
        for (Map.Entry<String, List<String>> entry : other.getAttributes().entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean containsKey(String name) {
        return attributes.containsKey(name);
    }
}
