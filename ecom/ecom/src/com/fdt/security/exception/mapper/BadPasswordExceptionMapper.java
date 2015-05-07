package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.BadPasswordException;

/**
 * @author valampally
 * 
 * This Class is a REST Exception Mapper that handles BadPasswordException, so that the error can be displayed in a nice 
 * format with code and Description. If this class is not implemented the response will return the custom 434 error code.
 */
@Component("badPasswordExceptionMapper")
public class BadPasswordExceptionMapper implements ExceptionMapper<BadPasswordException> {

	@Override
	public Response toResponse(BadPasswordException badPasswordException) {
		ResponseBuilder builder = Response.status(HttpStatusCodes.BAD_PASSWORD_EXCEPTION.value());
		builder.type(MediaType.APPLICATION_JSON);
		builder.entity("{\"errorCode\":\""    + badPasswordException.getErrorCode() + 
					 "\",\"description\":\""  + badPasswordException.getMessage() + "\"}");
		return builder.build();
	}
}
