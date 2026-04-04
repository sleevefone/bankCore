package com.payhub.bankcore.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JacksonUtilsTest {

    @Test
    void shouldSerializeAndDeserializeJavaTimeObject() {
        SamplePayload payload = new SamplePayload(
                "ORD-1001",
                new BigDecimal("88.50"),
                LocalDateTime.of(2026, 4, 4, 14, 30, 0),
                List.of("PAY_IN", "AUDIT")
        );

        String json = JacksonUtils.toJson(payload);
        SamplePayload converted = JacksonUtils.fromJson(json, SamplePayload.class);

        assertTrue(json.contains("\"createdAt\":\"2026-04-04T14:30:00\""));
        assertEquals(payload, converted);
    }

    @Test
    void shouldConvertObjectToMapAndBack() {
        SamplePayload payload = new SamplePayload(
                "ORD-2001",
                new BigDecimal("19.99"),
                LocalDateTime.of(2026, 4, 4, 15, 0, 0),
                List.of("INTEREST")
        );

        Map<String, Object> valueMap = JacksonUtils.toMap(payload);
        SamplePayload converted = JacksonUtils.convertValue(valueMap, SamplePayload.class);

        assertEquals("ORD-2001", valueMap.get("orderId"));
        assertEquals(payload, converted);
    }

    @Test
    void shouldDeserializeGenericCollection() {
        String json = """
                [
                  {"orderId":"ORD-1","amount":10.00,"createdAt":"2026-04-04T16:00:00","tags":["A"]},
                  {"orderId":"ORD-2","amount":20.00,"createdAt":"2026-04-04T17:00:00","tags":["B","C"]}
                ]
                """;

        List<SamplePayload> payloads = JacksonUtils.fromJson(json, new TypeReference<>() {
        });

        assertEquals(2, payloads.size());
        assertEquals("ORD-2", payloads.get(1).orderId());
        assertEquals(List.of("B", "C"), payloads.get(1).tags());
    }

    @Test
    void shouldReadTreeWithoutBinding() {
        JsonNode node = JacksonUtils.readTree("""
                {"requestId":"REQ-1","detail":{"customerNo":"CUST-1"}}
                """);

        assertEquals("REQ-1", node.get("requestId").asText());
        assertEquals("CUST-1", node.get("detail").get("customerNo").asText());
    }

    @Test
    void shouldIgnoreUnknownFieldsWhenDeserializing() {
        String json = """
                {"orderId":"ORD-3001","amount":30.00,"createdAt":"2026-04-04T18:00:00","tags":["X"],"extra":"ignored"}
                """;

        SamplePayload payload = JacksonUtils.fromJson(json, SamplePayload.class);

        assertEquals("ORD-3001", payload.orderId());
        assertFalse(payload.tags().isEmpty());
    }

    @Test
    void shouldThrowReadableExceptionOnInvalidJson() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> JacksonUtils.fromJson("{invalid-json}", SamplePayload.class)
        );

        assertTrue(exception.getMessage().contains("Failed to deserialize JSON"));
    }

    record SamplePayload(
            String orderId,
            BigDecimal amount,
            LocalDateTime createdAt,
            List<String> tags
    ) {
    }
}
