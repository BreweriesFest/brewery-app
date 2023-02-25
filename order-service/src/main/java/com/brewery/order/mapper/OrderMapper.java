package com.brewery.order.mapper;

import com.brewery.model.dto.OrderDto;
import com.brewery.model.dto.OrderLineDto;
import com.brewery.order.repository.Order;
import com.brewery.order.repository.OrderLine;
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
