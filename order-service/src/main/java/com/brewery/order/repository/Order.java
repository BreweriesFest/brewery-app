package com.brewery.order.repository;

import com.brewery.model.audit.Auditable;
import com.brewery.model.dto.OrderStatus;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "order")
public class Order extends Auditable {

	@Setter
	private OrderStatus status;

	@Setter
	private Collection<String> orderLineId;

}
