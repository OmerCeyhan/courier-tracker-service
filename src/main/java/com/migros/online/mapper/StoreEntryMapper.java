package com.migros.online.mapper;

import com.migros.online.dto.response.StoreEntryResponse;
import com.migros.online.entity.StoreEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StoreEntryMapper {

    @Mapping(target = "storeName", source = "store.name")
    StoreEntryResponse toResponse(StoreEntry entry);

    List<StoreEntryResponse> toResponseList(List<StoreEntry> entries);
}
