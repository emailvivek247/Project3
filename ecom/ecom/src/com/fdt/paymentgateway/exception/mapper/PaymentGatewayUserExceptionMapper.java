package com.fdt.paymentgateway.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.paymentgateway.exception.PaymentGatewayUserException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles PaymentGatewayUserException, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 442 error code.
 */
@Component("paymentGatewayUserExceptionMapper")
public class PaymentGatewayUserExceptionMapper implements ExceptionMapper<PaymentGatewayUserException> {

    @Override
    public Response toResponse(PaymentGatewayUserException paymentGatewayUserException) {
        ResponseBuilder builder = Response.status(442);
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"errorCode\":\""         + paymentGatewayUserException.getErrorCode()
                    + "\",\"description\":\""      + paymentGatewayUserException.getDescription() + "\"}");
        return builder.build();
    }
}
