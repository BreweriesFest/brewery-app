package com.brewery.app.order.resource;

import com.brewery.app.model.OrderDto;
import com.brewery.app.model.OrderLineDto;
import com.brewery.app.order.service.OrderService;
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
        return orderService.orderLine(orders);
    }
}
