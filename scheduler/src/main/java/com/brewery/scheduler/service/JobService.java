package com.brewery.scheduler.service;

import com.brewery.common.client.BeerClient;
import com.brewery.common.kafka.producer.ReactiveProducerService;
import com.brewery.model.event.CheckInventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.spring.annotations.Recurring;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.brewery.common.util.AppConstant.CUSTOMER_ID;
import static com.brewery.common.util.AppConstant.TENANT_ID;
import static com.brewery.common.util.Helper.uuid;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

	private final JobScheduler jobScheduler;

	private final BeerClient beerClient;

	private final ReactiveProducerService<String, CheckInventoryEvent> reactiveProducer;

	public void scheduleJob(String beerId, String tenantId, String customerId) {
		jobScheduler.enqueue(() -> createCheckInventoryEvent(beerId, tenantId, customerId));
	}

	@Recurring(id = "my-recurring-job", cron = "*/10 * * * *")
	@Job(name = "Check Beer Inventory")
	public void getAllBeer() {
		beerClient.getAllByTenant().contextWrite(
				__ -> __.putAllMap(Map.of(TENANT_ID, Optional.of("tenant"), CUSTOMER_ID, Optional.of("customer"))))
				.subscribe(__ -> scheduleJob(__.id(), "tenant", "customer"));
	}

	public void createCheckInventoryEvent(String beerId, String tenantId, String customerId) {
		reactiveProducer.send(new CheckInventoryEvent(uuid.get(), beerId), Map.of()).contextWrite(
				__ -> __.putAllMap(Map.of(TENANT_ID, Optional.of(tenantId), CUSTOMER_ID, Optional.of(customerId))))
				.subscribe();

	}

}
