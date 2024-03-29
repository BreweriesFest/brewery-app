package com.brewery.order.service;

import com.brewery.common.client.BeerClient;
import com.brewery.model.dto.BeerDto;
import com.brewery.model.dto.OrderDto;
import com.brewery.model.dto.OrderLineDto;
import com.brewery.order.mapper.OrderMapper;
import com.brewery.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.brewery.common.util.Helper.collectionAsStream;
import static com.brewery.model.dto.OrderStatus.NEW;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final OrderMapper orderMapper;

	private final OrderRepository orderRepository;

	private final OrderLineRepository orderLineRepository;

	private final BeerClient beerClient;

	public Mono<OrderDto> placeOrder(Collection<OrderLineDto> orderLineDtos) {
		var orderLineMono = Mono.just(orderLineDtos)
			.map(orderMapper::fromOrderLineDto)
			.map(orderLineRepository::saveAll);
		return orderLineMono.flatMap(__ -> __.collectList())
			.map(__ -> collectionAsStream(__).map(OrderLine::getId).collect(Collectors.toList()))
			.flatMap(__ -> orderRepository.save(Order.builder().orderLineId(__).status(NEW).build()))
			.map(orderMapper::fromOrder)
			.subscribeOn(Schedulers.boundedElastic());
	}

	public Flux<OrderDto> findOrderById(List<String> orderId) {
		QOrder qOrder = QOrder.order;
		return orderRepository.findAll(qOrder.id.in(orderId)).map(__ -> orderMapper.fromOrder(__));
	}

	public Mono<Map<OrderDto, List<OrderLineDto>>> orderLine(List<OrderDto> orders) {
		QOrderLine orderLine = QOrderLine.orderLine;

		var orderLines = Flux.fromIterable(orders)
			.map(OrderDto::orderLineId)
			.flatMap(__ -> orderLineRepository.findAll(orderLine.id.in(__)));
		return orderLines.collectList()
			.map(__ -> collectionAsStream(orders).collect(Collectors.toMap(Function.identity(),
					o -> collectionAsStream(__).filter(___ -> o.orderLineId().contains(___.getId()))
						.map(orderMapper::fromOrderLine)
						.collect(Collectors.toList()))));
	}

	public Mono<Map<OrderLineDto, BeerDto>> beer(List<OrderLineDto> orderLines) {

		var beerCollection = Flux.fromIterable(orderLines)
			.map(OrderLineDto::beerId)
			.collectList()
			.map(beerClient::getBeerById)
			.flatMapMany(Flux::concat)
			.collectList();

		return beerCollection.map(__ -> collectionAsStream(orderLines).collect(Collectors.toMap(Function.identity(),
				o -> collectionAsStream(__).filter(___ -> o.beerId().equals(___.id()))
					.findFirst()
					.orElse(new BeerDto(o.beerId(), null, null, null, null, null)))));

	}

}
