package com.brewery.beer.mapper;

import com.brewery.beer.repository.Beer;
import com.brewery.model.dto.BeerDto;
import org.mapstruct.*;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface BeerMapper {

	@Mapping(target = "name", qualifiedByName = "toUpperCase")
	@Mapping(target = "upc", qualifiedByName = "toUpperCase")
	Beer fromBeerDto(BeerDto beerDto);

	@Named("toUpperCase")
	default String toUpperCase(String src) {
		return src != null ? src.toUpperCase() : null;
	}

	@Mapping(target = "name", qualifiedByName = "toUpperCase")
	Beer fromBeerDto(BeerDto beerDto, @MappingTarget Beer beer);

	BeerDto fromBeer(Beer beer);

}
