package com.brewery.app.order.mapper;

import com.brewery.app.order.dto.OrderDto;
import com.brewery.app.order.dto.OrderLineDto;
import com.brewery.app.order.repository.Order;
import com.brewery.app.order.repository.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Set;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface OrderMapper {

    @Mapping(target = "orderLineSet", ignore = true)
    Order fromOrderDto(OrderDto orderDto);

    OrderDto fromOrder(Order order);

    OrderLineDto fromOrderLine(OrderLine orderLine);

    OrderLine fromOrderLineDto(OrderLineDto orderLineDto);

    Set<OrderLine> fromOrderLineDto(Set<OrderLineDto> orderLineDto);

    Set<OrderLineDto> fromOrderLine(Set<OrderLine> orderLineDto);
}
