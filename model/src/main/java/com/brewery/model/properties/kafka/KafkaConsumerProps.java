package com.brewery.model.properties.kafka;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KafkaConsumerProps extends KafkaProps {

	private String consumerGroup;

}
