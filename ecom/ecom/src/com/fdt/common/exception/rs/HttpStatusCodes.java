package com.fdt.common.exception.rs;


public enum HttpStatusCodes {

    /** Security Related Exception codes **/
    
    USERNAME_ALREADY_EXISTS_EXCEPTION(432),

    USER_NOT_ACTIVE_EXCEPTION(433),

    BAD_PASSWORD_EXCEPTION(434),

    INVALID_DATA_EXCEPTION(435),

    DUPLICATE_ALERT_EXCEPTION(436),

    MAXIMUM_NUMBER_OF_ALERTS_REACHED_EXCEPTION(437),

    USERNAME_NOTFOUND_EXCEPTION(438),

    USER_ALREADY_ACTIVATED_EXCEPTION(439),

    USERACCOUNT_EXISTS_EXCEPTION(440),
    

    /** Ecom Related Exception codes **/

    ACCESS_UNAUTHORIZED_EXCEPTION(441),

    PAYPAL_GATEWAY_USER_EXCEPTION(442),

    PAYPAL_GATEWAY_SYSTEM_EXCEPTION(443),

    /** Project Common Related Exception codes **/

    SDL_BUSINESS_EXCEPTION(464),

    SDL_EXCEPTION(465),

    
    MAX_USERS_EXCEEDED_EXCEPTION(445),
    
    DELETE_USER_EXCEPTION(446);


    /** PayPal Related Exception codes **/

    private final int value;

    private HttpStatusCodes(int value) {
        this.value = value;
    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public static HttpStatusCodes valueOf(int statusCode) {
        for (HttpStatusCodes status : values()) {
            if (status.value == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
    }

    public boolean equals(HttpStatusCodes httpStatusCodes) {
         for (HttpStatusCodes code: HttpStatusCodes.values()){
             if (this == code) {
                 return true;
             }
         }
        return false;
    }

    /**
     * Return the integer value of this status code.
     */
    public int value() {
        return this.value;
    }
}