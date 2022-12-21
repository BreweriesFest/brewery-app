package com.brewery.app.inventory.mapper;

import com.brewery.app.domain.InventoryDTO;
import com.brewery.app.inventory.repository.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {

    Inventory fromInventoryDTO(InventoryDTO inventoryDTO);

    Inventory fromInventoryDTO(InventoryDTO inventoryDTO, @MappingTarget Inventory inventory);

    InventoryDTO fromInventory(Inventory inventory);

}
