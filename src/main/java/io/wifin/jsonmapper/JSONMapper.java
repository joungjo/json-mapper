package io.wifin.jsonmapper;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * JSONObject A mapping to B with the key mappers {@link KeyMapper }
 * 
 * @author zozoz
 *
 */
public class JSONMapper {
	public JSONObject convert(JSONObject sourceObject, List<KeyMapper> mappers) {
		if (mappers == null || mappers.isEmpty()) {
			return JSONObject.parseObject(sourceObject.toJSONString());
		}
		JSONObject targetObject = new JSONObject();
		convert(sourceObject, mappers, targetObject);
		return targetObject;
	}

	public void convert(JSONObject sourceObject, List<KeyMapper> mappers, JSONObject targetObject) {
		for (KeyMapper mapper : mappers) {
			String sourceKey = mapper.getSourceKey();
			Object value = sourceValue(sourceObject, sourceKey);
			if (value == null) {
				value = mapper.getDefaultValue();
			}
			if (value == null) {
				continue;
			}
			value = convertType(value, mapper);
			String targetField = mapper.getTargetKey();
			targetObject.put(targetField, value);
		}
	}

	/**
	 * get value from the source json object
	 * 
	 * @param sourceObject
	 *            source json object
	 * @param sourceKey
	 *            relative key from the parent object combine with '.'<br/>
	 *            for example if the sourceObject is just like this
	 *            'sourceObject': { 'a':{ 'b':'c' } }<br/>
	 *            and the sourceKey is 'a.b' then this method will return 'c'
	 * @return the value with the key
	 */
	private Object sourceValue(JSONObject sourceObject, String sourceKey) {
		String[] keys = sourceKey.split("\\.");
		Object object = sourceObject;
		for (int i = 0; i < keys.length - 1; i++) {
			object = getValue((JSONObject) object, keys[i]);
			if (object == null) {
				return null;
			}
		}
		return getValue((JSONObject) object, keys[keys.length - 1]);
	}

	private Object getValue(JSONObject source, String key) {
		if (source == null) {
			return null;
		}
		// if array
		if (key.contains("[") && key.endsWith("]")) {
			int startConditionIndex = key.indexOf("[");
			String keyName = key.substring(0, startConditionIndex);
			int endConditionIndex = key.indexOf("]");
			String condition = key.substring(startConditionIndex + 1, endConditionIndex);
			return getArrayValue(source, keyName, condition);
		}
		return source.get(key);
	}

	private Object getArrayValue(JSONObject source, String key, String condition) {
		JSONArray jsonArray = source.getJSONArray(key);
		if (jsonArray == null || jsonArray.isEmpty()) {
			return null;
		}
		if (StringUtils.isBlank(condition)) {
			return jsonArray.get(0);
		}
		if (StringUtils.isNumeric(condition)) {
			int index = Integer.parseInt(condition);
			return jsonArray.get(index);
		}
		if (condition.contains("=")) {
			String[] conditions = condition.split("&");
			return getArrayValue(jsonArray, conditions);
		}
		return source.get(key);
	}

	private Object getArrayValue(JSONArray jsonArray, String[] conditions) {
		for (Object object : jsonArray) {
			if (!(object instanceof JSONObject)) {
				continue;
			}
			if (checkConditions((JSONObject) object, conditions)) {
				return object;
			}
		}
		return null;
	}

	private boolean checkConditions(JSONObject object, String[] conditions) {
		for (String condition : conditions) {
			String[] pair = condition.split("=");
			if (pair.length < 2) {
				return false;
			}
			String value = pair[1];
			String key = pair[0].trim();
			if (key.isEmpty()) {
				return false;
			}
			String stringValue = object.getString(key);
			if (!value.equalsIgnoreCase(stringValue)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param value
	 *            source value
	 * @param mapper
	 * @return targer value
	 */
	private Object convertType(Object value, KeyMapper mapper) {
		List<KeyMapper> subMappers = mapper.getSubMappers();
		if (subMappers == null || subMappers.isEmpty()) {
			return value;
		}
		String fieldType = mapper.getType();

		switch (fieldType.trim().toLowerCase()) {
		case "byte":
		case "short":
		case "int":
		case "integer":
		case "long":
			return Integer.parseInt(value.toString());
		case "double":
		case "float":
			return Double.parseDouble(value.toString());
		case "boolean":
		case "bool":
			return Boolean.parseBoolean(value.toString());
		case "array":
			JSONArray jsonArray = new JSONArray();
			if (value instanceof JSONArray) {
				JSONArray array = (JSONArray) value;
				for (Object object : array) {
					if (object instanceof JSONObject) {
						jsonArray.add(convert((JSONObject) object, subMappers));
					} else {
						jsonArray.add(object);
					}
				}
			} else if (value instanceof JSONObject) {
				jsonArray.add(convert((JSONObject) value, subMappers));
			} else {
				jsonArray.add(value);
			}
			return jsonArray;
		case "json":
		case "object":
			if (value instanceof JSONObject) {
				return convert((JSONObject) value, subMappers);
			}
		default:
			break;
		}
		return value;
	}

	/**
	 * sample [ <br/>
	 * { <br/>
	 * "sourceKey": "OPEN_ACCT_INFO", <br/>
	 * "targetKey": "custID", <br/>
	 * "type": "string" <br/>
	 * }, <br/>
	 * { <br/>
	 * "sourceKey": "AGENT_INFO.USER_FNAME", <br/>
	 * "targetKey": "custName", <br/>
	 * "type": "string" <br/>
	 * }, <br/>
	 * { <br/>
	 * "sourceKey": "SURVEY_INFO.surveyResults[].INVEST_LMT_NAME", <br/>
	 * "targetKey": "custInvestmentTimeName", <br/>
	 * "type": "string" <br/>
	 * }, <br/>
	 * { <br/>
	 * "sourceKey": "MATERIAL_INFO.MATERIAL_FILE[IMG_CLS=071]", <br/>
	 * "targetKey": "fileList", <br/>
	 * "type": "array", <br/>
	 * "subMappers":[ <br/>
	 * { <br/>
	 * "sourceKey": "type", <br/>
	 * "targetKey": "type", <br/>
	 * "type": "string", <br/>
	 * "defaultValue":"1" <br/>
	 * }, <br/>
	 * { <br/>
	 * "sourceKey": "materialList[0].fileName", <br/>
	 * "targetKey": "fileBase64", <br/>
	 * "type": "string" <br/>
	 * } <br/>
	 * ] <br/>
	 * } <br/>
	 * ]
	 * 
	 * @author zozzoo
	 *
	 */
	@SuppressWarnings("serial")
	public static class KeyMapper implements Serializable {
		private String sourceKey;
		private String targetKey;
		private String type;
		private KeyMapper parent;
		private List<KeyMapper> subMappers;
		private Object value;
		private Object defaultValue;
		private boolean isObject;

		public boolean isObject() {
			return isObject;
		}

		public void setObject(boolean isObject) {
			this.isObject = isObject;
		}

		public KeyMapper getParent() {
			return parent;
		}

		public void setParent(KeyMapper parent) {
			this.parent = parent;
		}

		public List<KeyMapper> getSubMappers() {
			return subMappers;
		}

		public void setSubMappers(List<KeyMapper> subMappers) {
			this.subMappers = subMappers;
		}

		public String getSourceKey() {
			return sourceKey;
		}

		public void setSourceKey(String sourceKey) {
			this.sourceKey = sourceKey;
		}

		public String getTargetKey() {
			return targetKey;
		}

		public void setTargetKey(String targetKey) {
			this.targetKey = targetKey;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(Object defaultValue) {
			this.defaultValue = defaultValue;
		}

	}
}
