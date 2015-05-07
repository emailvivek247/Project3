package com.fdt.common.exception.rs;

import java.io.IOException;

import org.springframework.web.client.UnknownHttpStatusCodeException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.ecom.exception.AccessUnAuthorizedException;
import com.fdt.paymentgateway.exception.PaymentGatewaySystemException;
import com.fdt.paymentgateway.exception.PaymentGatewayUserException;
import com.fdt.security.exception.BadPasswordException;
import com.fdt.security.exception.DeleteUserException;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.InvalidDataException;
import com.fdt.security.exception.MaxUsersExceededException;
import com.fdt.security.exception.MaximumNumberOfAlertsReachedException;
import com.fdt.security.exception.UserAlreadyActivatedException;
import com.fdt.security.exception.UserNameAlreadyExistsException;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.security.exception.UserNotActiveException;

public class ExceptionHandler {

    public void handleUnknownHttpStatusCodeException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws UserNameNotFoundException,
            UserAlreadyActivatedException, UserNameAlreadyExistsException, UserNotActiveException,
            PaymentGatewayUserException, PaymentGatewaySystemException, BadPasswordException, DuplicateAlertException,
            InvalidDataException, SDLBusinessException,  
            MaxUsersExceededException, DeleteUserException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.USERNAME_NOTFOUND_EXCEPTION.value()) {
                UserNameNotFoundException userNameNotFoundException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserNameNotFoundException.class);
                throw userNameNotFoundException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() ==
                    HttpStatusCodes.USER_ALREADY_ACTIVATED_EXCEPTION.value()) {
                UserAlreadyActivatedException userAlreadyActivatedException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserAlreadyActivatedException.class);
                throw userAlreadyActivatedException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() ==
                    HttpStatusCodes.USERNAME_ALREADY_EXISTS_EXCEPTION.value()) {
                UserNameAlreadyExistsException userNameAlreadyExistsException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserNameAlreadyExistsException.class);
                throw userNameAlreadyExistsException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.USER_NOT_ACTIVE_EXCEPTION.value()) {
                UserNotActiveException userNotActiveException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserNotActiveException.class);
                throw userNotActiveException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.BAD_PASSWORD_EXCEPTION.value()) {
                BadPasswordException badPasswordException = jacksonObjectMapper.readValue(unknownHttpStatusCodeExcp
                    .getResponseBodyAsString(), BadPasswordException.class);
                throw badPasswordException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.MAX_USERS_EXCEEDED_EXCEPTION.value()) {
            	MaxUsersExceededException maxUsersExceededException = jacksonObjectMapper.readValue(unknownHttpStatusCodeExcp
                        .getResponseBodyAsString(), MaxUsersExceededException.class);
                    throw maxUsersExceededException;
            }  else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.DUPLICATE_ALERT_EXCEPTION.value()) {
                DuplicateAlertException duplicateAlertException = jacksonObjectMapper.readValue(unknownHttpStatusCodeExcp
                    .getResponseBodyAsString(), DuplicateAlertException.class);
                throw duplicateAlertException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.INVALID_DATA_EXCEPTION.value()) {
                InvalidDataException invalidDataException = jacksonObjectMapper.readValue(unknownHttpStatusCodeExcp
                    .getResponseBodyAsString(), InvalidDataException.class);
                throw invalidDataException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() ==
                    HttpStatusCodes.PAYPAL_GATEWAY_USER_EXCEPTION.value()) {
                PaymentGatewayUserException paymentGatewayUserException = jacksonObjectMapper.readValue(
                    unknownHttpStatusCodeExcp.getResponseBodyAsString(), PaymentGatewayUserException.class);
                throw paymentGatewayUserException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() ==
                    HttpStatusCodes.PAYPAL_GATEWAY_SYSTEM_EXCEPTION.value()) {
                PaymentGatewaySystemException paymentGatewaySystemException = jacksonObjectMapper.readValue(
                    unknownHttpStatusCodeExcp.getResponseBodyAsString(), PaymentGatewaySystemException.class);
                throw paymentGatewaySystemException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.SDL_BUSINESS_EXCEPTION.value()) {
                SDLBusinessException sdlBusinessException = jacksonObjectMapper.readValue(unknownHttpStatusCodeExcp
                    .getResponseBodyAsString(), SDLBusinessException.class);
                throw sdlBusinessException;
            } else if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.DELETE_USER_EXCEPTION.value()) {
                DeleteUserException deleteUserException = jacksonObjectMapper.readValue(unknownHttpStatusCodeExcp
                        .getResponseBodyAsString(), DeleteUserException.class);
                    throw deleteUserException;
            } else {
                throw new RuntimeException("Problem when calling the WebServices" + unknownHttpStatusCodeExcp
                    .getRawStatusCode());
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleAccessUnAuthorizedException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws AccessUnAuthorizedException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.ACCESS_UNAUTHORIZED_EXCEPTION.value()) {
                AccessUnAuthorizedException accessUnAuthorizedException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), AccessUnAuthorizedException.class);
                throw accessUnAuthorizedException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleMaximumNumberOfAlertsReachedException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws MaximumNumberOfAlertsReachedException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() ==
                    HttpStatusCodes.MAXIMUM_NUMBER_OF_ALERTS_REACHED_EXCEPTION.value()) {
                MaximumNumberOfAlertsReachedException maximumNumberOfAlertsReachedException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(),
                            MaximumNumberOfAlertsReachedException.class);
                throw maximumNumberOfAlertsReachedException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleUserNameNotFoundException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws UserNameNotFoundException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.USERNAME_NOTFOUND_EXCEPTION.value()) {
                UserNameNotFoundException userNameNotFoundException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserNameNotFoundException.class);
                throw userNameNotFoundException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleUserAlreadyActivatedException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws UserAlreadyActivatedException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.USER_ALREADY_ACTIVATED_EXCEPTION.value()) {
                UserAlreadyActivatedException userAlreadyActivatedException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserAlreadyActivatedException.class);
                throw userAlreadyActivatedException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleUserNameAlreadyExistsException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws UserNameAlreadyExistsException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.USERNAME_ALREADY_EXISTS_EXCEPTION.value()) {
                UserNameAlreadyExistsException userNameAlreadyExistsException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserNameAlreadyExistsException.class);
                throw userNameAlreadyExistsException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleUserNotActiveException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws UserNotActiveException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.USER_NOT_ACTIVE_EXCEPTION.value()) {
                UserNotActiveException userNotActiveException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), UserNotActiveException.class);
                throw userNotActiveException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handlePaymentGatewayUserException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws PaymentGatewayUserException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.PAYPAL_GATEWAY_USER_EXCEPTION.value()) {
                PaymentGatewayUserException paymentGatewayUserException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), PaymentGatewayUserException.class);
                throw paymentGatewayUserException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handlePaymentGatewaySystemException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws PaymentGatewaySystemException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.PAYPAL_GATEWAY_SYSTEM_EXCEPTION.value()) {
                PaymentGatewaySystemException paymentGatewaySystemException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), PaymentGatewaySystemException.class);
                throw paymentGatewaySystemException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleBadPasswordException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws BadPasswordException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.BAD_PASSWORD_EXCEPTION.value()) {
                BadPasswordException badPasswordException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), BadPasswordException.class);
                throw badPasswordException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleDuplicateAlertException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws DuplicateAlertException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.DUPLICATE_ALERT_EXCEPTION.value()) {
                DuplicateAlertException duplicateAlertException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), DuplicateAlertException.class);
                throw duplicateAlertException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleInvalidDataException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws InvalidDataException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.INVALID_DATA_EXCEPTION.value()) {
                InvalidDataException invalidDataException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), InvalidDataException.class);
                throw invalidDataException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleSDLBusinessException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws SDLBusinessException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.SDL_BUSINESS_EXCEPTION.value()) {
                SDLBusinessException sDLBusinessException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), SDLBusinessException.class);
                throw sDLBusinessException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleMaxUsersExceededException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws MaxUsersExceededException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.MAX_USERS_EXCEEDED_EXCEPTION.value()) {
            	MaxUsersExceededException maxUsersExceededException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), MaxUsersExceededException.class);
                throw maxUsersExceededException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    public void handleDeleteUserException(ObjectMapper jacksonObjectMapper,
            UnknownHttpStatusCodeException unknownHttpStatusCodeExcp) throws DeleteUserException {
        try {
            if (unknownHttpStatusCodeExcp.getRawStatusCode() == HttpStatusCodes.DELETE_USER_EXCEPTION.value()) {
            	DeleteUserException deleteUserException = jacksonObjectMapper
                    .readValue(unknownHttpStatusCodeExcp.getResponseBodyAsString(), DeleteUserException.class);
                throw deleteUserException;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

}
