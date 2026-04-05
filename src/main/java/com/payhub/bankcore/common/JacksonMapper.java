package com.payhub.bankcore.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class JacksonMapper {

    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    private JacksonMapper() {
    }

    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", ex);
        }
    }

    public static String toPrettyJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize object to pretty JSON", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize JSON to " + targetType.getSimpleName(), ex);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        Objects.requireNonNull(typeReference, "typeReference must not be null");
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize JSON with type reference", ex);
        }
    }

    public static JsonNode readTree(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to read JSON tree", ex);
        }
    }

    public static <T> T convertValue(Object source, Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (source == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(source, targetType);
    }

    public static <T, R> Function<R, T> convertValue(Class<T> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        return source -> source == null ? null : OBJECT_MAPPER.convertValue(source, targetType);
    }

    public static <T, R> Function<R, T> convertValue(TypeReference<T> typeReference) {
        Objects.requireNonNull(typeReference, "typeReference must not be null");
        return source -> source == null ? null : OBJECT_MAPPER.convertValue(source, typeReference);
    }

    public static <R, T> T convertValue(R source, TypeReference<T> typeReference) {
        Objects.requireNonNull(typeReference, "typeReference must not be null");
        if (source == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(source, typeReference);
    }

    public static Map<String, Object> toMap(Object value) {
        if (value == null) {
            return new LinkedHashMap<>();
        }
        if (value instanceof String text) {
            if (StringUtils.isBlank(text)) {
                return new LinkedHashMap<>();
            }
            String trimmedText = text.trim();
            if (!(trimmedText.startsWith("{") || trimmedText.startsWith("["))) {
                return new LinkedHashMap<>();
            }
            Map<String, Object> jsonMap = fromJson(trimmedText, new TypeReference<>() {
            });
            return jsonMap == null ? new LinkedHashMap<>() : jsonMap;
        }
        return convertValue(value, new TypeReference<>() {
        });
    }

    public static JsonNode valueToTree(Object value) {
        if (value == null) {
            return null;
        }
        return OBJECT_MAPPER.valueToTree(value);
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
    }

    public static Map<String, Object> toMap(InputStream in) {
        if (in != null) {
            try {
                return JacksonMapper.objectMapper().readValue(in, new TypeReference<>() {
                });
            } catch (IOException ex) {
                throw new IllegalArgumentException("Failed to read JSON tree", ex);
            }
        }
        return null;
    }

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
}
