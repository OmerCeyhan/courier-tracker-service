package com.migros.online.mapper;

import com.migros.online.dto.request.CourierLocationRequest;
import com.migros.online.dto.response.CourierLocationResponse;
import com.migros.online.entity.CourierLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourierLocationMapper {

    @Mapping(target = "message", ignore = true)
    CourierLocationResponse toResponse(CourierLocation location);

    List<CourierLocationResponse> toResponseList(List<CourierLocation> locations);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", source = "time")
    @Mapping(target = "createdAt", ignore = true)
    CourierLocation toEntity(CourierLocationRequest request);
}
