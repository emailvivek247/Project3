package com.fdt.common.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

/**
 * @author valampally
 * 
 * This Class is a REST Exception Mapper that handles runtime exception, so that the error can be displayed in a nice format
 * with code and Description. If this class is not implemented the response will return the Web server's, 500 Internal Server
 * error Page.
 */
@Component("runtimeExceptionMapper")
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

	@Override
	public Response toResponse(RuntimeException runtimeException) {
		ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR);
		builder.type(MediaType.APPLICATION_JSON);
		builder.entity(runtimeException.getMessage());
		return builder.build();
	}
}
