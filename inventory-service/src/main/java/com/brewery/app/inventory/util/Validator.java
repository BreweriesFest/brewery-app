package com.brewery.app.inventory.util;

import com.brewery.app.domain.InventoryDTO;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.brewery.app.inventory.util.HelperClass.isBlankString;
import static com.brewery.app.inventory.util.ValidationResult.*;

public interface Validator extends Function<InventoryDTO, ValidationResult> {

    static Validator isBeerIdValid() {
        return inventoryDTO -> isBlankString.test(inventoryDTO.beerId()) ? BEER_ID_NOT_VALID : SUCCESS;
    }

    static Validator isQuantityOnHandValid() {
        return inventoryDTO -> Objects.nonNull(inventoryDTO.quantityOnHand())
                && Integer.compare(inventoryDTO.quantityOnHand(), 0) != -1 ? SUCCESS : QUANTITY_ON_HAND_NOT_VALID;

    }

    static Mono<ValidationResult> validateInventoryDTO(InventoryDTO inventoryDTO) {
        return Mono.just(isBeerIdValid().and(isQuantityOnHandValid()).apply(inventoryDTO));
    }

    default Validator and(Validator validator) {
        return inventoryDTO -> {
            ValidationResult result = this.apply(inventoryDTO);
            return result.equals(SUCCESS) ? validator.apply(inventoryDTO) : result;
        };
    }
}
