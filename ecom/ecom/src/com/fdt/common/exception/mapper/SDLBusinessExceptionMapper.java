package com.fdt.common.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.SDLBusinessException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles SDLBusinessExceptionMapper, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 443 error code.
 */
@Component("sdlBusinessExceptionMapper")
public class SDLBusinessExceptionMapper implements ExceptionMapper<SDLBusinessException> {

    @Override
    public Response toResponse(SDLBusinessException sdlBusinessException) {
        ResponseBuilder builder = Response.status(464);
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""        + sdlBusinessException.getErrorCode() +
        		     "\",\"businessMessage\":\""  + sdlBusinessException.getBusinessMessage() +
                     "\",\"description\":\""      + sdlBusinessException.getMessage() + "\"}");
        return builder.build();
    }
}
