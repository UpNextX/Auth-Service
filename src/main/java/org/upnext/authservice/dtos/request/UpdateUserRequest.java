package org.upnext.authservice.dtos.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    String name;
    String phoneNumber;
    String address;
}
