package com.brewery.app.properties.kafka;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class KafkaProps {

	private List<String> bootstrapServers = new ArrayList(Collections.singletonList("localhost:9092"));

	private String topic;

}
