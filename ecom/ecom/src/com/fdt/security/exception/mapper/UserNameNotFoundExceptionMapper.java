package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.UserNameNotFoundException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles runtime exception, so that the error can be displayed in a nice format
 * with code and Description. If this class is not implemented the response will return the custom 438 error code.
 */
@Component("userNameNotFoundExceptionMapper")
public class UserNameNotFoundExceptionMapper implements ExceptionMapper<UserNameNotFoundException> {

    @Override
    public Response toResponse(UserNameNotFoundException userNameNotFoundException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.USERNAME_NOTFOUND_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + userNameNotFoundException.getErrorCode() +
                     "\",\"description\":\""  + userNameNotFoundException.getMessage() + "\"}");
        return builder.build();
    }
}
