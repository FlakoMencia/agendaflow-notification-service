package com.flakomencia.agendaflow.notification.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public record NotificationVariables(Map<String, String> values) {

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "^[A-Za-z][A-Za-z0-9]*(?:[._-][A-Za-z0-9]+)*$");

    public NotificationVariables {
        Objects.requireNonNull(values, "values must not be null");
        if (values.size() > NotificationLimits.MAX_VARIABLES) {
            throw new IllegalArgumentException("too many notification variables");
        }

        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            if (key == null
                    || key.length() > NotificationLimits.MAX_VARIABLE_KEY_LENGTH
                    || !KEY_PATTERN.matcher(key).matches()) {
                throw new IllegalArgumentException("invalid notification variable key");
            }
            if (value == null || value.length() > NotificationLimits.MAX_VARIABLE_VALUE_LENGTH) {
                throw new IllegalArgumentException("invalid notification variable value");
            }
            copy.put(key, value);
        });
        values = Collections.unmodifiableMap(copy);
    }

    public static NotificationVariables empty() {
        return new NotificationVariables(Map.of());
    }

    public String get(String key) {
        return values.get(key);
    }

    public boolean contains(String key) {
        return values.containsKey(key);
    }
}
