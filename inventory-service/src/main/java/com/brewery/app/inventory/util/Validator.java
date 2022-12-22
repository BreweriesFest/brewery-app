package com.brewery.app.inventory.util;

import com.brewery.app.event.BrewBeerEvent;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.brewery.app.inventory.util.HelperClass.isBlankString;
import static com.brewery.app.inventory.util.ValidationResult.*;

public interface Validator extends Function<BrewBeerEvent, ValidationResult> {

    static Validator isBeerIdValid() {
        return brewBeerEvent -> isBlankString.test(brewBeerEvent.beerId()) ? BEER_ID_NOT_VALID : SUCCESS;
    }

    static Validator isQtyToBrewValid() {
        return brewBeerEvent -> Objects.nonNull(brewBeerEvent.qtyToBrew())
                && Integer.compare(brewBeerEvent.qtyToBrew(), 0) != -1 ? SUCCESS : QUANTITY_ON_HAND_NOT_VALID;

    }

    static Mono<ValidationResult> validateInventoryDTO(BrewBeerEvent brewBeerEvent) {
        return Mono.just(isBeerIdValid().and(isQtyToBrewValid()).apply(brewBeerEvent));
    }

    default Validator and(Validator validator) {
        return brewBeerEvent -> {
            ValidationResult result = this.apply(brewBeerEvent);
            return result.equals(SUCCESS) ? validator.apply(brewBeerEvent) : result;
        };
    }
}
