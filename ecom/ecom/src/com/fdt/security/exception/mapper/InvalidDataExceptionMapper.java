package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.InvalidDataException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles InvalidDataExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 435 error code.
 */
@Component("invalidDataExceptionMapper")
public class InvalidDataExceptionMapper implements ExceptionMapper<InvalidDataException> {

    @Override
    public Response toResponse(InvalidDataException invalidDataException) {
        ResponseBuilder builder = Response.status(HttpStatusCodes.INVALID_DATA_EXCEPTION.value());
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""    + invalidDataException.getErrorCode() +
                     "\",\"description\":\""  + invalidDataException.getMessage() + "\"}");
        return builder.build();
    }
}
