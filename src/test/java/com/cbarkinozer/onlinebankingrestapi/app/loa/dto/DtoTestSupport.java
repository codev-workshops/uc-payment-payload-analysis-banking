package com.cbarkinozer.onlinebankingrestapi.app.loa.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class DtoTestSupport {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private DtoTestSupport() {
    }

    static void assertEqualContract(Object first, Object second) {
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    static void assertNotEqualBothWays(Object first, Object second) {
        assertNotEquals(first, second);
        assertNotEquals(second, first);
    }

    static <T> T jsonRoundTrip(T value, Class<T> type) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(value), type);
    }
}
