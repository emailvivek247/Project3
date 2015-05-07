package com.fdt.security.dao;

public final class EComAdminUserHQL {

    public static String LOAD_USER_BY_USERNAME =
        "Select user"
        + " From EComAdminUser user "
        + " Where user.active = :isActive "
                    + " AND user.accountNonExpired= :isAccountNonExpired "
                    + " AND user.credentialsNonExpired= :isCredentialsNonExpired "
                    + " AND user.accountNonLocked= :isAccountNonLocked "
                    + " AND user.username = :username";

    public static String FIND_USER_EVENT_BY_USER_NAME = "SELECT aue.id, aue.token, " +
                                                            "au.username, au.firstName, au.lastName, au.active " +
                                                            "FROM EComAdminUserEvent aue inner join aue.user au " +
                                                            "WHERE " +
                                                            "au.username = :username";

    public static String FIND_USER_EVENT_BY_USER_NAME_REQ_TOKEN = "FROM EComAdminUserEvent aue INNER JOIN aue.user au " +
                                                "WHERE au.username = :username AND " +
                                                " aue.token = :requestToken ";

}


