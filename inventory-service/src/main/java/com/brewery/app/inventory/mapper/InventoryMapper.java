package com.brewery.app.inventory.mapper;

import com.brewery.app.inventory.domain.BeerInventory;
import com.brewery.app.inventory.domain.InventoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {

    BeerInventory fromInventoryDTO(InventoryDTO inventoryDTO);

    BeerInventory fromInventoryDTO(InventoryDTO inventoryDTO, @MappingTarget BeerInventory beerInventory);

    InventoryDTO fromBeerInventory(BeerInventory beerInventory);

}
