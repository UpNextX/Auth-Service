package org.upnext.authservice.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.upnext.authservice.dtos.request.RegisterRequest;
import org.upnext.authservice.models.User;
import org.upnext.sharedlibrary.Dtos.UserDto;

@Mapper(componentModel="spring")
public interface UserMapper {

    @Mapping(target = "role", source = "roles")
    UserDto toUserDto(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isConfirmed", ignore = true)
    User toUserFromRegisterRequest(RegisterRequest registerRequest);
}
