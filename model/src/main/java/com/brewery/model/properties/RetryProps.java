package com.brewery.model.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RetryProps {

	@Value("${maxAttempts:${app.default.retry.maxAttempts}}")
	private int maxAttempts;

}
