package com.fdt.paymentgateway.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.springframework.stereotype.Component;

import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;

/**
 * @author valampally
 *
 * This Class is a REST Exception Mapper that handles paymentGatewaySystemException, so that the error can be displayed in a nice
 * format with code and Description. If this class is not implemented the response will return the custom 443 error code.
 */
@Component("paymentGatewaySystemExceptionMapper")
public class PaymentGatewaySystemExceptionMapper implements ExceptionMapper<PaymentGatewaySystemException> {

    @Override
    public Response toResponse(PaymentGatewaySystemException paymentGatewaySystemException) {
        ResponseBuilder builder = Response.status(443);
        builder.type(MediaType.APPLICATION_JSON);
        builder.entity("{\"code\":\"" + paymentGatewaySystemException.getErrorCode()
                        + "\",\"description\":\"" + paymentGatewaySystemException.getDescription() + "\"}");
        return builder.build();
    }
}
