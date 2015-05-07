package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.UserNameAlreadyExistsException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles UserNameAlreadyExistsException, so that the error can be displayed in
 * a nice format with code and Description. If this class is not implemented the response will return the custom 432 error
 * code.
 */
@Component("userNameAlreadyExistsExceptionMapper")
public class UserNameAlreadyExistsExceptionMapper implements ExceptionMapper<UserNameAlreadyExistsException> {

    @Override
    public Response toResponse(UserNameAlreadyExistsException userNameAlreadyExistsException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.USERNAME_ALREADY_EXISTS_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + userNameAlreadyExistsException.getErrorCode() +
                     "\",\"description\":\""  + userNameAlreadyExistsException.getMessage() + "\"}");
        return builder.build();
    }
}
