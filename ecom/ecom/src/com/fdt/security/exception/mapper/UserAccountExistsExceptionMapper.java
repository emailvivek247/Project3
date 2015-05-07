package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.UserAccountExistsException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles UserAccountExistsExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 440 error code.
 */
@Component("userAccountExistsExceptionMapper")
public class UserAccountExistsExceptionMapper implements ExceptionMapper<UserAccountExistsException> {

    @Override
    public Response toResponse(UserAccountExistsException userAccountExistsException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.USERACCOUNT_EXISTS_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + userAccountExistsException.getErrorCode() +
                     "\",\"description\":\""  + userAccountExistsException.getMessage() + "\"}");
        return builder.build();
    }
}
