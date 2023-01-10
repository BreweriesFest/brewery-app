package com.brewery.app.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringConverter implements Converter<String, String> {

	@Override
	public String convert(String source) {
		return source != null ? source.trim() : null;
	}

}