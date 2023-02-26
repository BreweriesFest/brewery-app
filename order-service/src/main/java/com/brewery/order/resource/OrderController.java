package com.brewery.order.resource;

import com.brewery.model.dto.BeerDto;
import com.brewery.model.dto.OrderDto;
import com.brewery.model.dto.OrderLineDto;
import com.brewery.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {

	private final OrderService orderService;

	@MutationMapping
	public Mono<OrderDto> placeOrder(@Argument Collection<OrderLineDto> orderLines) {
		return orderService.placeOrder(orderLines);
	}

	@QueryMapping
	public Flux<OrderDto> findOrder(@Argument List<String> orderId) {
		return orderService.findOrderById(orderId);
	}

	@BatchMapping(typeName = "OrderDtoOut")
	public Mono<Map<OrderDto, List<OrderLineDto>>> orderLine(List<OrderDto> orders) {
		return orderService.orderLine(orders).doOnError(ex -> log.error("", ex));
	}

	@BatchMapping(typeName = "OrderLineDtoOut")
	public Mono<Map<OrderLineDto, BeerDto>> beer(List<OrderLineDto> orders) {
		return orderService.beer(orders);
	}

}
