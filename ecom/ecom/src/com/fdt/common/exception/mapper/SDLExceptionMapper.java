package com.fdt.common.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.SDLException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles SDLBusinessExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 443 error code.
 */
@Component("sdlExceptionMapper")
public class SDLExceptionMapper implements ExceptionMapper<SDLException> {

    @Override
    public Response toResponse(SDLException sdlException) {
        ResponseBuilder builder = Response.status(465);
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""        + sdlException.getErrorCode() +
                     "\",\"description\":\""      + sdlException.getMessage() + "\"}");
        return builder.build();
    }
}
