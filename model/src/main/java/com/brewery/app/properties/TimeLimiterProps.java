package com.brewery.app.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TimeLimiterProps {

	@Value("${timeoutDuration:${app.default.time-limiter.timeoutDuration}}")
	private long timeoutDuration;

}
