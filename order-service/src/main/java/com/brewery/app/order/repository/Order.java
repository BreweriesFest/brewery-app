package com.brewery.app.order.repository;

import com.brewery.app.audit.Auditable;
import com.brewery.app.model.OrderStatus;
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
