package com.brewery.model.properties.kafka;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class KafkaProps {

	private List<String> bootstrapServers;

	private String topic;

}
