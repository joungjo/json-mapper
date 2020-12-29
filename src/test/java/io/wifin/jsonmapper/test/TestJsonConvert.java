package io.wifin.jsonmapper.test;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.Charsets;

import com.alibaba.fastjson.JSONObject;

import io.wifin.jsonmapper.JSONMapper;
import io.wifin.jsonmapper.JSONMapper.KeyMapper;

public class TestJsonConvert {
	public static void main(String[] args) {
		JSONMapper json = new JSONMapper();
		JSONObject target = json.convert(getSource(), getKeyMapper());
		System.out.println(target.toJSONString());

		// System.out.println("detail".split("\\.").length);
	}

	public static JSONObject getSource() {
		File file = new File("src/test/resources/data.json");
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			return JSONObject.parseObject(is, JSONObject.class);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static List<KeyMapper> getKeyMapper() {
		File file = new File("src/test/resources/keyMapper.json");
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			byte[] bytes = new byte[1024];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int read = 0;
			while ((read = is.read(bytes)) > 0) {
				os.write(bytes, 0, read);
			}
			String text = os.toString(Charsets.UTF_8.toString());
			return JSONObject.parseArray(text, KeyMapper.class);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static JSONObject getFileJSON(String fileName) {
		File file = new File(fileName);
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			return JSONObject.parseObject(is, JSONObject.class);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
