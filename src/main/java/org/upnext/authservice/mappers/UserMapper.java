package org.upnext.authservice.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.upnext.authservice.models.User;
import org.upnext.sharedlibrary.Dtos.UserDto;

@Mapper(componentModel="spring")
public interface UserMapper {

    @Mapping(target = "role", source = "roles")
    UserDto toUserDto(User user);
}
