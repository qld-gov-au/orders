package au.gov.qld.pub.orders.dao;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.TreeMap;

import org.boon.json.JsonFactory;
import org.boon.json.ObjectMapper;

public class JsonHelper {
	private JsonHelper() {
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T deserialise(Class<T> clazz, String json) {
		if (isBlank(json)) {
			return null;
		}
		
		ObjectMapper mapper =  JsonFactory.create();
		T fromJson = mapper.fromJson(json, clazz);
		if (clazz.isAssignableFrom(Map.class)) {
			return (T)new TreeMap((Map)fromJson);
		}
		
		return fromJson;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> String serialise(T obj) {
		ObjectMapper mapper =  JsonFactory.create();
		if (obj instanceof Map) {
			return mapper.toJson(new TreeMap((Map)obj));
		}
		return mapper.toJson(obj);
	}
}
