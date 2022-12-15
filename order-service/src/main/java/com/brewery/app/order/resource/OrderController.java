package com.brewery.app.order.resource;

import com.brewery.app.order.dto.OrderDto;
import com.brewery.app.order.repository.OrderLine;
import com.brewery.app.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @MutationMapping
    Mono<OrderDto> placeOrder(@Argument OrderDto orderDto) {
        return orderService.placeOrder(orderDto);
    }

    @QueryMapping
    Flux<OrderLine> listOrderLine(@Argument String orderId) {
        return orderService.findOrderLineByOrderId(orderId);
    }
}
