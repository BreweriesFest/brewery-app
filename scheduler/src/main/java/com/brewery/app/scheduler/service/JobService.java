package com.brewery.app.scheduler.service;

import com.brewery.app.client.BeerClient;
import com.brewery.app.event.CheckInventoryEvent;
import com.brewery.app.kafka.producer.ReactiveProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.spring.annotations.Recurring;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static com.brewery.app.util.AppConstant.CUSTOMER_ID;
import static com.brewery.app.util.AppConstant.TENANT_ID;
import static com.brewery.app.util.Helper.uuid;

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
        beerClient.getAllByTenant()
                .contextWrite(
                        __ -> __.putAllMap(Map.of(TENANT_ID, Optional.of("txt"), CUSTOMER_ID, Optional.of("test"))))
                .subscribe(__ -> scheduleJob(__.id(), "txt", "test"));
    }

    public void createCheckInventoryEvent(String beerId, String tenantId, String customerId) {
        reactiveProducer.send(new CheckInventoryEvent(uuid.get(), beerId), Map.of()).contextWrite(
                __ -> __.putAllMap(Map.of(TENANT_ID, Optional.of(tenantId), CUSTOMER_ID, Optional.of(customerId))))
                .subscribe();

    }
}
