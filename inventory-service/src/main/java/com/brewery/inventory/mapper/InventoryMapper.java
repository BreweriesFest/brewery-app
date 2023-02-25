package com.brewery.inventory.mapper;

import com.brewery.inventory.repository.Inventory;
import com.brewery.model.domain.InventoryDTO;
import com.brewery.model.event.BrewBeerEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {

	InventoryDTO fromInventory(Inventory inventory);

	@Mapping(source = "qtyToBrew", target = "qtyOnHand")
	Inventory fromBrewBeerEvent(BrewBeerEvent brewBeerEvent);

}
