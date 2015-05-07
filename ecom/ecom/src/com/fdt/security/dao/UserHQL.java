package com.fdt.security.dao;

public final class UserHQL {

    public static String LOAD_USER_BY_USERNAME =
        "Select user"
        + " From User user "
        + " Where user.username = :username ";

    public static String FIND_USER_EVENT_BY_USER_NAME = "SELECT aue.id, aue.token, " +
                                                            "au.username, au.firstName, au.lastName, au.active " +
                                                            "FROM UserEvent aue inner join aue.user au " +
                                                            "WHERE " +
                                                            "au.username = :username";

    public static String FIND_USER_EVENT_BY_USER_NAME_REQ_TOKEN = "FROM UserEvent aue INNER JOIN aue.user au " +
                                                "WHERE au.username = :username AND " +
                                                " aue.token = :requestToken ";

    public static String GET_ADMIN_USER_ACCESS_BY_USER_ID = 
    		"SELECT userAccess from UserAccess userAccess " + 
    		"WHERE " + 
			"user.username =:userName AND " +
			"userAccess.isFirmAccessAdmin = true"; 


    public static String GET_FIRM_USER_ACCESS_BY_USERID_ACCESSIDS = 
    		"SELECT userAccess from UserAccess userAccess " + 
    		"WHERE " + 
    		"user.username =:userName AND " +
			"access.id = :accessId AND " +
			"isFirmAccessAdmin = false AND " +
			"userAccess.firmAdminUserAccessId is not null"; 

    public static String GET_USER_ACCESS_BY_USER_ID_ACCESS_IDS = 
    		"SELECT userAccess from UserAccess userAccess " + 
    		"WHERE " + 
			"user.userName =:userName AND " +
			"userAccess.access.id in (:accessIds)"; 
    
}


