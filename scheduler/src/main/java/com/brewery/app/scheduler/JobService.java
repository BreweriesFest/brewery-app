package com.brewery.app.scheduler;

import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobScheduler jobScheduler;

    public void getAllBeer() {
        jobScheduler.schedule(ZonedDateTime.now(), x -> this.checkBeerInventory("", ""));
    }

    public void checkBeerInventory(String beerId, String tenantId) {

    }
}
