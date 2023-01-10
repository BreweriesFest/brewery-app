package com.brewery.app.order.mapper;

import com.brewery.app.model.OrderDto;
import com.brewery.app.model.OrderLineDto;
import com.brewery.app.order.repository.Order;
import com.brewery.app.order.repository.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collection;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderMapper {

	Order fromOrderDto(OrderDto orderDto);

	Collection<Order> fromOrderDto(Collection<OrderDto> orderDto);

	OrderDto fromOrder(Order order);

	default OrderDto fromOrderLine(OrderDto orderDto, Collection<OrderLine> orderLine) {

		return orderDto.fromOrderLine(fromOrderLine(orderLine));
	}

	OrderLineDto fromOrderLine(OrderLine orderLine);

	OrderLine fromOrderLineDto(OrderLineDto orderLineDto);

	Collection<OrderLine> fromOrderLineDto(Collection<OrderLineDto> orderLineDto);

	Collection<OrderLineDto> fromOrderLine(Collection<OrderLine> orderLineDto);

}
