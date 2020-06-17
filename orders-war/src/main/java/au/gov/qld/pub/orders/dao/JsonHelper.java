package au.gov.qld.pub.orders.dao;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class JsonHelper {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper() {
            {
                this.registerModule(new ParameterNamesModule());
                this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                this.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                this.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
            }
        };
    }

    private JsonHelper() {

    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T deserialise(Class<T> clazz, String json) {
        if (isBlank(json)) {
            return null;
        }

        T fromJson = null;
        try {
            fromJson = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (clazz.isAssignableFrom(Map.class)) {
            return (T)new TreeMap((Map)fromJson);
        }
        
        return fromJson;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> String serialise(T obj) {
        try {
            if (obj instanceof Map) {
                    return objectMapper.writeValueAsString(new TreeMap((Map)obj));
            }
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
