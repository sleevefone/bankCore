package com.payhub.bankcore.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;

public final class JacksonUtils {

    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    private JacksonUtils() {
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize object to JSON", ex);
        }
    }

    public static String toPrettyJson(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize object to pretty JSON", ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> targetType) {
        try {
            return OBJECT_MAPPER.readValue(json, targetType);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize JSON to " + targetType.getSimpleName(), ex);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to deserialize JSON with type reference", ex);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to read JSON tree", ex);
        }
    }

    public static <T> T convertValue(Object source, Class<T> targetType) {
        return OBJECT_MAPPER.convertValue(source, targetType);
    }

    public static <T> T convertValue(Object source, TypeReference<T> typeReference) {
        return OBJECT_MAPPER.convertValue(source, typeReference);
    }

    public static Map<String, Object> toMap(Object value) {
        return convertValue(value, new TypeReference<>() {
        });
    }

    public static JsonNode valueToTree(Object value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    public static ObjectMapper objectMapper() {
        return OBJECT_MAPPER;
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
