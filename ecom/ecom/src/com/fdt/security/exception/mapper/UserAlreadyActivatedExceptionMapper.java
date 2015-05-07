package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.UserAlreadyActivatedException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles UserAlreadyActivatedExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 439 error code.
 */
@Component("userAlreadyActivatedExceptionMapper")
public class UserAlreadyActivatedExceptionMapper implements ExceptionMapper<UserAlreadyActivatedException> {

    @Override
    public Response toResponse(UserAlreadyActivatedException userAlreadyActivatedException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.USER_ALREADY_ACTIVATED_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + userAlreadyActivatedException.getErrorCode() +
                     "\",\"description\":\""  + userAlreadyActivatedException.getMessage() + "\"}");
        return builder.build();
    }
}
