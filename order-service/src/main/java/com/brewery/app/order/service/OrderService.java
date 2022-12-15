package com.brewery.app.order.service;

import com.brewery.app.order.dto.OrderDto;
import com.brewery.app.order.mapper.OrderMapper;
import com.brewery.app.order.repository.OrderLine;
import com.brewery.app.order.repository.OrderLineRepository;
import com.brewery.app.order.repository.OrderRepository;
import com.brewery.app.order.repository.QOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderMapper orderMapper;

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;

    // BeerOrderPagedList listOrders(UUID customerId, Pageable pageable);
    //
    public Mono<OrderDto> placeOrder(OrderDto orderDto) {

        return Mono.just(orderDto).map(__ -> orderDto.orderLineSet()).map(orderMapper::fromOrderLineDto).map(__ -> {
            for (OrderLine ___ : __) {
                var res = orderMapper.fromOrderDto(orderDto);
                res.setId(UUID.randomUUID().toString());
                ___.setOrder(res);
            }
            return __;
        }).map(__ -> orderLineRepository.saveAll(__)).flatMap(__ -> __.collectList()).map(__ -> {
            var res = __.stream().findAny().map(___ -> ___.getOrder()).orElse(orderMapper.fromOrderDto(orderDto));
            res.setOrderLineSet(__.stream().collect(Collectors.toSet()));
            return res;
        }).flatMap(orderRepository::save).map(orderMapper::fromOrder);
    }

    public Flux<OrderLine> findOrderLineByOrderId(String orderId) {
        QOrder qOrder = QOrder.order;
        return orderRepository.findAll(qOrder.id.eq(orderId)).map(order -> {
            var res = order.getOrderLineSet();
            return res;
        }).flatMap(o -> Flux.fromIterable(o));
        // return res.map(orderLines -> orderMapper.fromOrderLine(orderLines))
        // .flatMap(orderLineDtos -> Flux.fromIterable(orderLineDtos));
    }
    //
    // BeerOrderDto getOrderById(UUID customerId, UUID orderId);
    //
    // void pickupOrder(UUID customerId, UUID orderId);
}
