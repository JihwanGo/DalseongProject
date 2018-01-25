package com.dalseonggun.DalseongTour;

/**
 * Created by Jinhee on 2017-02-06.
 */

public enum LoginErrorCode {
    NONE(0),
    SERVER_ERRROR(-100),
    NO_AUTHORIZED(-99),
    CONNECTION_ERROR(-98),
    INVALID_REQUEST(-1),
    USER_CANCEL(1),
    NO_REGISTERED_USER(2),
    NO_KAKAOSTORY_USER(3);

    private final int errorCode;
    LoginErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

}
