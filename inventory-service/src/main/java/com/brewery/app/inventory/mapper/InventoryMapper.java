package com.brewery.app.inventory.mapper;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.inventory.repository.BeerInventory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {

    BeerInventory fromInventoryDTO(InventoryDTO inventoryDTO);

    BeerInventory fromInventoryDTO(InventoryDTO inventoryDTO, @MappingTarget BeerInventory beerInventory);

    InventoryDTO fromBeerInventory(BeerInventory beerInventory);

}
