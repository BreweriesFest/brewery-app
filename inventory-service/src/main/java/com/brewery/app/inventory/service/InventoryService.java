package com.brewery.app.inventory.service;

import com.brewery.app.inventory.config.BadRequestException;
import com.brewery.app.inventory.domain.InventoryDTO;
import com.brewery.app.inventory.domain.QBeerInventory;
import com.brewery.app.inventory.mapper.InventoryMapper;
import com.brewery.app.inventory.repository.BeerInventoryRepository;
import com.brewery.app.inventory.util.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.brewery.app.inventory.util.Validator.validateInventoryDTO;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final BeerInventoryRepository beerInventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final TransactionalOperator transactionalOperator;
    private final ReactiveMongoOperations reactiveMongoOperations;

    public Mono<InventoryDTO> saveInventory(InventoryDTO inventoryDTO) {
        // reactiveMongoOperations.upsert();

        var validation = validateInventoryDTO(inventoryDTO).flatMap(validationResult -> {
            if (validationResult != ValidationResult.SUCCESS)
                return Mono.error(new BadRequestException(validationResult.name()));
            return Mono.empty();
        });

        var persist = Mono.deferContextual(ctx -> {
            QBeerInventory inventory = QBeerInventory.beerInventory;
            return beerInventoryRepository
                    .findOne(inventory.beerId.eq(inventoryDTO.beerId()).and(inventory.upc.eq(inventoryDTO.upc()))
                            .and(inventory.tenantId.eq((String) ctx.get("tenantId"))));
        }).map(beerInventory -> inventoryMapper.fromInventoryDTO(inventoryDTO, beerInventory))
                .switchIfEmpty(Mono.just(inventoryMapper.fromInventoryDTO(inventoryDTO)))
                .flatMap(beerInventory -> beerInventoryRepository.save(beerInventory))
                .map(inventoryMapper::fromBeerInventory);

        return validation.then(persist).doOnError(exc -> log.error("exception", exc))
                // .onErrorResume(throwable -> Mono.error(new BadRequestException(throwable.getMessage())))
                // .as(transactionalOperator::transactional)
                .subscribeOn(Schedulers.boundedElastic());
    }
}