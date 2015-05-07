package com.fdt.security.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.common.exception.rs.HttpStatusCodes;
import com.fdt.security.exception.UserNotActiveException;

/**
 * @author valampally
 * 
 * This Class is a REST Exception Mapper that handles UserNotActiveException, so that the error can be displayed in a nice 
 * format with code and Description. If this class is not implemented the response will return the custom 433 error code.
 */
@Component("userNotActiveExceptionMapper")
public class UserNotActiveExceptionMapper implements ExceptionMapper<UserNotActiveException> {

	@Override
	public Response toResponse(UserNotActiveException userNotActiveException) {
		ResponseBuilder builder = Response.status(HttpStatusCodes.USER_NOT_ACTIVE_EXCEPTION.value());
		builder.type(MediaType.APPLICATION_JSON);
		builder.entity("{\"errorCode\":\""    + userNotActiveException.getErrorCode() + 
					 "\",\"description\":\""  + userNotActiveException.getMessage() + "\"}");
		return builder.build();
	}
}
