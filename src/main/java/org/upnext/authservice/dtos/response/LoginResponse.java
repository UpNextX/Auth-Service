package org.upnext.authservice.dtos.response;

import org.upnext.sharedlibrary.Dtos.UserDto;

public class LoginResponse {
    private String jwtToken;
    private UserDto user;
}
