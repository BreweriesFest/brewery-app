package com.brewery.app.inventory.mapper;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.event.BrewBeerEvent;
import com.brewery.app.inventory.repository.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {

    InventoryDTO fromInventory(Inventory inventory);

    @Mapping(source = "qtyToBrew", target = "quantityOnHand")
    Inventory fromBrewBeerEvent(BrewBeerEvent brewBeerEvent);

}
