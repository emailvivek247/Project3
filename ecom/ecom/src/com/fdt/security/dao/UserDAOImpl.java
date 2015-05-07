package com.fdt.security.dao;

import static com.fdt.security.dao.UserHQL.FIND_USER_EVENT_BY_USER_NAME;
import static com.fdt.security.dao.UserHQL.FIND_USER_EVENT_BY_USER_NAME_REQ_TOKEN;
import static com.fdt.security.dao.UserHQL.GET_ADMIN_USER_ACCESS_BY_USER_ID;
import static com.fdt.security.dao.UserHQL.GET_FIRM_USER_ACCESS_BY_USERID_ACCESSIDS;
import static com.fdt.security.dao.UserHQL.GET_USER_ACCESS_BY_USER_ID_ACCESS_IDS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.util.CreditCardUtil;
import com.fdt.security.dto.FirmUserDTO;
import com.fdt.security.entity.Access;
import com.fdt.security.entity.User;
import com.fdt.security.entity.UserAccess;
import com.fdt.security.entity.UserEvent;
import com.fdt.security.exception.UserNameNotFoundException;
import com.fdt.subscriptions.entity.SubscriptionFee;

@Repository
public class UserDAOImpl extends AbstractBaseDAOImpl implements UserDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public User getUserDetails(String username, String nodeName) {
        User user = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_USER_DETAILS");
        query.setParameter("username", username);
        query.setParameter("nodeName", nodeName);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Object[] row = (Object[]) resultListIterator.next();
            user = new User();
            user.setId(this.getLongFromBigInteger(row[0]));
            user.setUsername(this.getString(row[1]));
            user.setPassword(this.getString(row[2]));
            user.setActive(this.getBoolean(row[3]));
            user.setAccountNonExpired(this.getBoolean(row[4]));
            user.setCredentialsNonExpired(this.getBoolean(row[5]));
            user.setAccountNonLocked(this.getBoolean(row[6]));
            user.setCardAvailable(this.getBoolean(row[8]));
            user.setCreditCardActive(this.getBoolean(row[9]));
            CreditCard creditCard = new CreditCard();
            String number =  null;
            if (row[10] != null && !StringUtils.isEmpty(row[10].toString())) {
                number = this.getPbeStringEncryptor().decrypt(row[10].toString());
                creditCard.setCardType(CreditCardUtil.getCardType(number));
                number = number.substring(number.length() - 4, number.length());
                creditCard.setNumber(row[10] == null ? null : number);
                creditCard.setActive(getBoolean(row[9]));
                user.setCreditCard(creditCard);
            }
            user.setAuthorizationPending(this.getBoolean(row[39]));
            user.setPayedUser(this.getBoolean(row[11]));
            user.setPaymentDue(this.getBoolean(row[12]));
            user.setFirstName(this.getString(row[13]));
            user.setLastName(this.getString(row[14]));
            user.setLastLoginTime(this.getDate(row[17]));
            user.setCreatedDate(this.getDate(row[18]));
            List<Access> accessList = new LinkedList<Access>();
            List<Site> sites = new LinkedList<Site>();
            resultListIterator.previous(); // To reset the iterator position to first row.
            while(resultListIterator.hasNext()) {
                row = (Object[]) resultListIterator.next();
                Access access = new Access();
                access.setId(this.getLongFromInteger(row[22]));
                access.setCode(this.getString(row[7]));
                access.setGuestFlg(this.getBoolean(row[15]));
                access.setAccessType(this.getAccessType(row[16]));
                access.setComments(this.getString(row[24]));
                access.setAccessOverriden(this.getBoolean(row[25]));
                access.setAuthorized(this.getBoolean(row[26]));
                access.setActive(this.getBoolean(row[37]));
                access.setFirmLevelAccess(this.getBoolean(row[34]));
                UserAccess userAccess = new UserAccess();
                userAccess.setFirmAccessAdmin(this.getBoolean(row[35]));
                userAccess.setId(this.getLongFromInteger(row[38]));
                userAccess.setFirmAdminUserAccessId(this.getLongFromInteger(row[36]));
                userAccess.setActive(this.getBoolean(row[28]));
                access.addUserAccess(userAccess);
                Long siteId = this.getLongFromInteger(row[19]);
                if (siteId != null) {
                    Site site = new Site();
                    site.setId(siteId);
                    site.setName(this.getString(row[20]));
                    site.setDescription(this.getString(row[27]));
                    sites.add(site);
                    access.setSite(site);
                }
                accessList.add(access);
            }
            user.setRegisteredNode(this.getString(row[21]));
            user.setSites(SetUniqueList.decorate(sites));
            user.setAccess(SetUniqueList.decorate(accessList));
            user.setCurrentLoginTime(this.getDate(row[23]));
            user.setPhone(this.getString(row[29]));
            user.setAcceptedTerms(this.getBoolean(row[30]));
            user.setFirmName(this.getString(row[31]));
            user.setFirmNumber(this.getString(row[32]));
            user.setBarNumber(this.getString(row[33]));
        }
        return user;
    }

    @Override
    public User getUserDetailsForAdmin(String username) {
        User user = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_USER_DETAILS_FOR_ADMIN")
                              .setParameter("username", username);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Object[] row = (Object[]) resultListIterator.next();
            user = new User();
            user.setId(this.getLongFromBigInteger(row[0]));
            user.setUsername(this.getString(row[1]));
            user.setPassword(this.getString(row[2]));
            user.setActive(this.getBoolean(row[3]));
            user.setAccountNonExpired(this.getBoolean(row[4]));
            user.setCredentialsNonExpired(this.getBoolean(row[5]));
            user.setAccountNonLocked(this.getBoolean(row[6]));
            user.setCardAvailable(this.getBoolean(row[8]));
            user.setCreditCardActive(this.getBoolean(row[9]));
            CreditCard creditCard = new CreditCard();
            String number =  null;
            if (row[10] != null && !StringUtils.isEmpty(row[10].toString())) {
                number = this.getPbeStringEncryptor().decrypt(row[10].toString());
                creditCard.setCardType(CreditCardUtil.getCardType(number));
                number = number.substring(number.length() - 4, number.length());
                creditCard.setNumber(row[10] == null ? null : number);
                creditCard.setActive(getBoolean(row[9]));
                user.setCreditCard(creditCard);
            }
            user.setPayedUser(this.getBoolean(row[11]));
            user.setPaymentDue(this.getBoolean(row[12]));
            user.setFirstName(this.getString(row[13]));
            user.setLastName(this.getString(row[14]));
            user.setLastLoginTime(this.getDate(row[17]));
            user.setCreatedDate(this.getDate(row[18]));
            List<Access> accessList = new LinkedList<Access>();
            List<Site> sites = new LinkedList<Site>();
            resultListIterator.previous(); // To reset the iterator position to first row.
            while(resultListIterator.hasNext()) {
                row = (Object[]) resultListIterator.next();
                Access access = new Access();
                access.setId(this.getLongFromInteger(row[22]));
                access.setCode(this.getString(row[7]));
                access.setGuestFlg(this.getBoolean(row[15]));
                access.setAccessType(this.getAccessType(row[16]));
                access.setComments(this.getString(row[24]));
                access.setAccessOverriden(this.getBoolean(row[25]));
                Long siteId = this.getLongFromInteger(row[19]);
                if (siteId != null) {
                    Site site = new Site();
                    site.setId(siteId);
                    site.setName(this.getString(row[20]));
                    sites.add(site);
                }
                accessList.add(access);
            }
            user.setRegisteredNode(this.getString(row[21]));
            user.setSites(SetUniqueList.decorate(sites));
            user.setAccess(SetUniqueList.decorate(accessList));
            user.setCurrentLoginTime(this.getDate(row[23]));
            user.setPhone(this.getString(row[26]));
            user.setFirmName(this.getString(row[27]));
            user.setFirmNumber(this.getString(row[28]));
            user.setBarNumber(this.getString(row[29]));

        }
        return user;
    }

    @Cacheable("getAccess")
    public List<Access> getAccess() {
        Session session = currentSession();
        List<Access> accessList = null;
        Query query = session.getNamedQuery("GET_ACCESS");
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            accessList = new LinkedList<Access>();
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Access access = new Access();
                Object[] row = (Object[]) resultListIterator.next();
                access.setId(this.getLongFromInteger(row[0]));
                access.setCode(this.getString(row[1]));
                access.setDescription(this.getString(row[2]));
                access.setGuestFlg(this.getBoolean(row[3]));
                access.setAccessType(this.getAccessType(row[4]));
                access.setAuthorizationRequired(this.getBoolean(row[5]));
                access.setFirmLevelAccess(this.getBoolean(row[6]));
    			access.setMaxUsersAllowed(this.getInteger(row[7]));
    			access.setMaxDocumentsAllowed(this.getInteger(row[8]));
    			SubscriptionFee fee = new SubscriptionFee();
    			fee.setFee(this.getDoubleFromBigDecimal(row[9]));
    			access.setSubscriptionFee(fee);
                accessList.add(access);
            }
        }
        return accessList;
    }

    public List<String> getSitesForUser(String username) {
        Session session = currentSession();
        List<String> siteNameList = null;
        Query query = session.getNamedQuery("GET_SITES_FOR_USER")
                            .setParameter("userName", username);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            siteNameList = new LinkedList<String>();
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                String row = (String) resultListIterator.next();
                siteNameList.add(row);
            }
        }
        return siteNameList;
    }

    public boolean isUserArchivable(String username) {
        Session session = currentSession();
        Query query = session.getNamedQuery("IS_USER_ARCHIVABLE")
                            .setParameter("userName", username);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void saveAccess(Access access) {
        Session session = currentSession();
        session.saveOrUpdate(access);
        session.flush();
    }

    @Override
    public void saveUser(User user) {
        Session session = currentSession();
        session.saveOrUpdate(user);
        session.flush();
    }

    @Override
    public void saveUserAcess(List<UserAccess> userAccessList) {
        Session session = currentSession();
        for (UserAccess userAccess : userAccessList) {
            session.saveOrUpdate(userAccess);
        }
        session.flush();
    }

    @Override
    public void saveUserAcess(UserAccess userAccess) {
        Session session = currentSession();
        session.saveOrUpdate(userAccess);
        session.flush();
    }

    @Override
    public void saveUserEvent(List<UserEvent> userEvents) {
        Session session = currentSession();
        for (UserEvent userEvent : userEvents) {
            session.saveOrUpdate(userEvent);
        }
        session.flush();
    }

    @Override
    public void saveUserEvent(UserEvent userEvent) {
        Session session = currentSession();
        session.saveOrUpdate(userEvent);
        session.flush();
    }

    @Override
    public void deleteUserEvents(List<UserEvent> userEvents) {
        Session session = currentSession();
        for (UserEvent userEvent : userEvents) {
            session.delete(userEvent);
        }
        session.flush();
    }

    @Override
    public UserEvent findUserEvent(String userName, String requestToken) {
        Session session = currentSession();
        UserEvent userEvent = null;
        List<UserEvent> userEvents = new LinkedList<UserEvent>();
        List<Object> resultList = session.createQuery(FIND_USER_EVENT_BY_USER_NAME_REQ_TOKEN)
                .setParameter("username", userName)
                .setParameter("requestToken", requestToken)
                .list();
        Iterator<Object> iterator = resultList.iterator();
        while(iterator.hasNext()){
            Object[] row = (Object[]) iterator.next();
            userEvent = new UserEvent();
            userEvent.setUser(row[1] == null ? null : (User) row[1]);
            if(row[0] != null){
                userEvent = (UserEvent) row[0];
            }
            userEvents.add(userEvent);
        }
        if (userEvents.size() > 0) {
            userEvent = (UserEvent) userEvents.get(0);
        }
        return userEvent;

    }

    @Override
    public UserEvent findUserEvent(String userName) {
        Session session = currentSession();
        UserEvent userEvent = null;
        User user = null;
        List<Object> resultList = session.createQuery(FIND_USER_EVENT_BY_USER_NAME)
                .setParameter("username", userName)
                .list();
        List<UserEvent> userEvents = new LinkedList<UserEvent>();
        Iterator<Object> iterator = resultList.iterator();
        while(iterator.hasNext()){
            Object[] row = (Object[]) iterator.next();
            userEvent = new UserEvent();
            user = new User();
            userEvent.setId(this.getLong(row[0]));
            userEvent.setToken(this.getString(row[1]));
            user.setUsername(this.getString(row[2]));
            user.setFirstName(this.getString(row[3]));
            user.setLastName(this.getString(row[4]));
            user.setActive(row[5] == null ? false : (Boolean) row[5]);
            userEvent.setUser(user);
            userEvents.add(userEvent);
        }
        if (userEvents.size() > 0) {
            userEvent = (UserEvent) userEvents.get(0);
        }
        return userEvent;
    }

    public int archiveUser(String userName, String comments, String modifiedBy, String machineName) {
        Session session = currentSession();
        Query query = session.getNamedQuery("ARCHIVE_USER")
                             .setParameter("userName", userName)
                             .setParameter("comments", comments)
                             .setParameter("modifiedBy", modifiedBy)
                             .setParameter("machineName", machineName);
        List<Object> resultList = query.list();
        int recordsUpdated = 0;
        if(resultList.size() > 0) {
            return recordsUpdated = resultList.size();
        }
        return recordsUpdated;
    }


    public int enableDisableUserAccess(Long userAccessId, boolean isEnable, String modifiedBy, String comments,
            boolean isAccessOverridden, String endDate){
    	List<Long> userAccessIds = new ArrayList<Long>();
    	userAccessIds.add(userAccessId);
    	return this.enableDisableUserAccesses(userAccessIds, isEnable, modifiedBy, comments, isAccessOverridden, endDate);
    }


    public int enableDisableUserAccesses(List<Long> userAccessIds, boolean isEnable, String modifiedBy, String comments,
            boolean isAccessOverridden, String endDate) {
        Boolean enableDisable = Boolean.FALSE;
        if (isEnable) {
            enableDisable = Boolean.TRUE;
        }
        Boolean isAccessOverriddenObj = Boolean.FALSE;
        if (isAccessOverridden) {
            isAccessOverriddenObj = Boolean.TRUE;
        }
        Date overriddenUntillDate = null;
        if(!StringUtils.isBlank(endDate)){
        	overriddenUntillDate = DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(endDate).withTime(23,59,59,1).toDate();
		}
        Session session = currentSession();
        int recordsModified = session.createQuery("Update UserAccess useraccess " +
                                "Set useraccess.active = :isEnabled , " +
                                "useraccess.modifiedDate = :modifiedDate, " +
                                "useraccess.modifiedBy = :modifiedBy, " +
                                "useraccess.comments = :comments, " +
                                "useraccess.overriddenUntillDate = :overriddenUntillDate, " +
                                "useraccess.accessOverriden = :accessOverriden " +
                                "where useraccess.id  in (:userAccessIds)")
                                .setParameter("isEnabled", enableDisable)
                                .setParameterList("userAccessIds", userAccessIds)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("modifiedBy", modifiedBy)
                                .setParameter("comments", comments)
                                .setParameter("overriddenUntillDate", overriddenUntillDate)
                                .setParameter("accessOverriden", isAccessOverriddenObj)
                                .executeUpdate();
        return recordsModified;
    }

    public int enableDisableFirmLevelUserAccess(Long userAccessId, boolean isEnable, String modifiedBy, String comments,
            boolean isAccessOverridden, boolean isFirmAccessAdmin) {
        Boolean enableDisable = Boolean.FALSE;
        if (isEnable) {
            enableDisable = Boolean.TRUE;
        }
        Boolean isAccessOverriddenObj = Boolean.FALSE;
        if (isAccessOverridden) {
            isAccessOverriddenObj = Boolean.TRUE;
        }

        Boolean isFirmAccessAdminObj = Boolean.FALSE;
        if (isFirmAccessAdmin) {
        	isFirmAccessAdminObj = Boolean.TRUE;
        }

        Session session = currentSession();
        int recordsModified = session.createQuery("Update UserAccess useraccess " +
                                "Set useraccess.active = :isEnabled , " +
                                "useraccess.modifiedDate = :modifiedDate, " +
                                "useraccess.modifiedBy = :modifiedBy, " +
                                "useraccess.comments = :comments, " +
                                "useraccess.accessOverriden = :accessOverriden, " +
                                "useraccess.isFirmAccessAdmin = :isFirmAccessAdmin " +
                                "where useraccess.id = :userAccessId")
                                .setParameter("isEnabled", enableDisable)
                                .setParameter("userAccessId", userAccessId)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("modifiedBy", modifiedBy)
                                .setParameter("comments", comments)
                                .setParameter("accessOverriden", isAccessOverriddenObj)
                                .setParameter("isFirmAccessAdmin", isFirmAccessAdminObj)
                                .executeUpdate();
        return recordsModified;
    }

    public int updateFirmUserAccess(Long userAccessId, Long userId,
    			String modifiedBy, String comments) {

        Session session = currentSession();
        int recordsModified = session.getNamedQuery("UPDATE_USER_ACCESS")
                                .setParameter("userAccessId", userAccessId)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("modifiedBy", modifiedBy)
                                .setParameter("comments", comments)
                                .setParameter("userId", userId)
                                .executeUpdate();
        return recordsModified;
    }

    /**
     * Get User with UserAccess & Access details.
     *
     * @param userName
     * @return
     */
    public User getUser(String userName){
    	User user = null;
    	Session session = currentSession();
    	Query query = session.getNamedQuery("GET_USER")
    							.setParameter("userName", userName)
    							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

    	List result = query.list();
    	List<UserAccess> userAccessList = new ArrayList<UserAccess>();
		for(Object object : result){
			Map row = (Map) object;
			// First Row, we want to create an user.
			// Rest of the rows will have repeated user info as per SQL.
    		if(user == null) {
				user = new User();
				user.setId(this.getLongFromString(row.get("userId")));
				user.setUsername(this.getString(row.get("userName")));
				user.setFirstName(this.getString(row.get("firstName")));
				user.setLastName(this.getString(row.get("lastName")));
				user.setPhone(this.getString(row.get("phone")));
				user.setAccountNonExpired(this.getString(row.get("accountNonExpired")).equals("Y") ? true : false);
				user.setAccountNonLocked(this.getString(row.get("accountNonLocked")).equals("Y") ? true : false);
				user.setActive(this.getString(row.get("user_active")).equals("Y") ? true : false);
				user.setPassword(this.getString(row.get("password")));
    		}
			UserAccess userAccess = new UserAccess();
			userAccess.setId(this.getLongFromString(row.get("userAccessId")));
			userAccess.setActive(this.getString(row.get("userAccessActive")).equals("Y") ? true : false);
			userAccess.setAccessOverriden(this.getString(row.get("accessOverridden")).equals("Y") ? true : false);
			userAccess.setFirmAccessAdmin(this.getString(row.get("isFirmAccessAdmin")).equals("Y") ? true : false);
			userAccess.setFirmAdminUserAccessId(this.getLongFromString(row.get("firmAdminUserAccessId")));
			//userAccess.setUser(user);
			Access access = new Access();
			access.setId(this.getLongFromString(row.get("accessId")));
			access.setCode(this.getString(row.get("code")));
			access.setDescription(this.getString(row.get("description")));
			access.setGuestFlg(this.getString(row.get("guestFlg")).equals("Y") ? true : false);
			access.setMaxUsersAllowed(this.getIntegerFromString(row.get("maxUsersAllowed")));
			access.setFirmLevelAccess(this.getString(row.get("isFirmLevelAccess")).equals("Y") ? true : false);
			access.setAuthorizationRequired(this.getString(row.get("isAuthorizationRequired")).equals("Y") ? true : false);
			// Set the site info
			Site site = new Site();
			site.setId(this.getLongFromInteger(row.get("siteId")));
			site.setName(this.getString(row.get("siteName")));
			site.setDescription(this.getString(row.get("siteDescription")));
			access.setSite(site);
			userAccess.setAccess(access);
			userAccessList.add(userAccess);
    	}
		// User could be null at this point as it could be an invalid username, check for null
		if(user != null) {
			user.setUserAccessList(userAccessList);
		}
        return user;
    }


    /**
     * Get Firm Admin User with UserAccess & Access details.
     *
     * @param userName
     * @param accessId
     * @return
     */
    public User getFirmAdminUser(String firmUserName, Long accessId){
    	User user = null;
    	Session session = currentSession();
    	Query query = session.getNamedQuery("GET_FIRM_ADMIN_USER")
    							.setParameter("firmUserName", firmUserName)
    							.setParameter("accessId", accessId)
    							.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

    	List result = query.list();
    	List<UserAccess> userAccessList = new ArrayList<UserAccess>();
		for(Object object : result){
			Map row = (Map) object;
			// First Row, we want to create an user.
			// Rest of the rows will have repeated user info as per SQL.
    		if(user == null) {
				user = new User();
				user.setId(this.getLongFromString(row.get("userId")));
				user.setUsername(this.getString(row.get("userName")));
				user.setFirstName(this.getString(row.get("firstName")));
				user.setLastName(this.getString(row.get("lastName")));
				user.setPhone(this.getString(row.get("phone")));
				user.setAccountNonExpired(this.getString(row.get("accountNonExpired")).equals("Y") ? true : false);
				user.setAccountNonLocked(this.getString(row.get("accountNonLocked")).equals("Y") ? true : false);
				user.setActive(this.getString(row.get("user_active")).equals("Y") ? true : false);
				user.setPassword(this.getString(row.get("password")));
    		}
			UserAccess userAccess = new UserAccess();
			userAccess.setId(this.getLongFromString(row.get("userAccessId")));
			userAccess.setActive(this.getString(row.get("userAccessActive")).equals("Y") ? true : false);
			userAccess.setAccessOverriden(this.getString(row.get("accessOverridden")).equals("Y") ? true : false);
			userAccess.setFirmAccessAdmin(this.getString(row.get("isFirmAccessAdmin")).equals("Y") ? true : false);
			userAccess.setFirmAdminUserAccessId(this.getLongFromString(row.get("firmAdminUserAccessId")));
			//userAccess.setUser(user);
			Access access = new Access();
			access.setId(this.getLongFromString(row.get("accessId")));
			access.setCode(this.getString(row.get("code")));
			access.setDescription(this.getString(row.get("description")));
			access.setGuestFlg(this.getString(row.get("guestFlg")).equals("Y") ? true : false);
			access.setMaxUsersAllowed(this.getIntegerFromString(row.get("maxUsersAllowed")));
			access.setFirmLevelAccess(this.getString(row.get("isFirmLevelAccess")).equals("Y") ? true : false);
			access.setAuthorizationRequired(this.getString(row.get("isAuthorizationRequired")).equals("Y") ? true : false);
			// Set the site info
			Site site = new Site();
			site.setId(this.getLongFromInteger(row.get("siteId")));
			site.setName(this.getString(row.get("siteName")));
			site.setDescription(this.getString(row.get("siteDescription")));
			access.setSite(site);
			userAccess.setAccess(access);
			userAccessList.add(userAccess);
    	}
		// User could be null at this point as it could be an invalid username, check for null
		if(user != null) {
			user.setUserAccessList(userAccessList);
		}
        return user;
    }

    @Override
    public int updateLastLoginTime(String userName) {
        Session session = currentSession();
        int recordsModified = session.createQuery("Update User  user " +
                "Set user.lastLoginTime = user.currentLoginTime, " +
                "user.currentLoginTime  = :currentLoginTime, " +
                "user.isEmailNotificationSent  = :isEmailNotificationSent " +
                "where user.username = :username ")
                .setParameter("currentLoginTime", new Date())
                .setParameter("isEmailNotificationSent", false)
                .setParameter("username", userName)
                .executeUpdate();
        return recordsModified;
    }

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy){
        Boolean lockUnlock = Boolean.TRUE;
        if (isLock) {
            lockUnlock = Boolean.FALSE;
        }
        Session session = currentSession();
        session.createQuery("Update User  user " +
                "Set user.accountNonLocked = :lockUnlock, " +
                "    user.modifiedDate = :modifiedDate,  " +
                "    user.modifiedBy = :modifiedBy   " +
                "where user.username = :username ")
                .setParameter("lockUnlock", lockUnlock)
                .setParameter("modifiedDate", new Date())
                .setParameter("modifiedBy", modifiedBy)
                .setParameter("username", userName)
                .executeUpdate();
    }

    public void updatePassword(String userName, String encodedPassword, String modifiedBy) {
        Session session = currentSession();
        session.createQuery("Update User user " +
                                "Set user.password = :encodedPassword, " +
                                "user.modifiedDate = :modifiedDate, " +
                                "user.modifiedBy = :modifiedBy " +
                                "Where user.username = :userName ) ")
                                .setParameter("encodedPassword", encodedPassword)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("userName", userName)
                                .setParameter("modifiedBy", modifiedBy)
                                .executeUpdate();

    }

    // ? do you want to update firm number , name  and bar number ?
    public void updateUser(User updatedUser, String modifiedBy) throws UserNameNotFoundException {
        Session session = currentSession();
        Query userQuery = session.createQuery("from User where username = :username");
        userQuery.setParameter("username", updatedUser.getUsername());
        User existingUser = (User)userQuery.list().get(0);
        if (existingUser == null) {
            throw new UserNameNotFoundException("User Not Found Exception");
        }
        if (!StringUtils.isBlank(updatedUser.getFirstName())){
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (!StringUtils.isBlank(updatedUser.getLastName())) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (!StringUtils.isBlank(updatedUser.getPhone())) {
            existingUser.setPhone(updatedUser.getPhone());
        }
        if (!StringUtils.isBlank(updatedUser.getPassword())) {
            existingUser.setPassword(updatedUser.getPassword());
        }
        if (!StringUtils.isBlank(updatedUser.getFirmName())) {
            existingUser.setFirmName(updatedUser.getFirmName());
        }
        if (!StringUtils.isBlank(updatedUser.getFirmNumber())) {
            existingUser.setFirmNumber(updatedUser.getFirmNumber());
        }
        if (!StringUtils.isBlank(updatedUser.getBarNumber())) {
            existingUser.setBarNumber(updatedUser.getBarNumber());
        }
        existingUser.setModifiedBy(modifiedBy);
        existingUser.setModifiedDate(new Date());
        session.saveOrUpdate(existingUser);
        session.flush();
    }

    public List<Term> getNewTermsAndConditionsforUser(String userName, String nodeName) {
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_NEW_TERMS_FOR_USER")
                            .setParameter("userName", userName)
                            .setParameter("nodeName", nodeName);
        List<Object> resultSet = sqlQuery.list();
        List<Term> newTermsList = new LinkedList<Term>();
        if (resultSet.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                Term term = new Term();
                Site site = new Site();
                term.setId(this.getLongFromInteger(row[0]));
                term.setDescription(this.getString(row[1]));
                site.setId(this.getLongFromInteger(row[2]));
                site.setDescription(this.getString(row[3]));
                term.setSite(site);
                newTermsList.add(term);
            }
        }
        return newTermsList;
    }

    public List<User> getInactiveUsers() {
        List<User> userList = new LinkedList<User>();
        Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_INACTIVE_USERS");
        List<Object> resultSet = sqlQuery.list();
        List<UserAccess> userAccessList = null;
        Long userId = null;
        if(resultSet.size() > 0){
        	Map<Long, User> uniqueUsers =  new HashMap<Long, User>();
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
            	User user = null;
            	Object[] row = (Object[]) resultSetIterator.next();
            	userId = this.getLongFromBigInteger(row[0]);
            	if(uniqueUsers.get(userId) == null) {
            		 user = new User();
            		 userAccessList = new LinkedList<UserAccess>();
            		 user.setUserAccessList(userAccessList);
                     user.setId(userId);
                     user.setUsername(this.getString(row[1]));
                     user.setFirstName(this.getString(row[2]));
                     user.setLastName(this.getString(row[3]));
                     user.setRegisteredNode(this.getString(row[4]));
                     user.setCreatedBy(this.getString(row[5]));
                     user.setCurrentLoginTime(this.getDate(row[6]));
                     user.setCreatedDate(this.getDate(row[7]));
                     user.setAccountDeletionDate(this.getDate(row[8]));
                     user.setFirmName(this.getString(row[9]));
                     user.setFirmNumber(this.getString(row[10]));
                     user.setBarNumber(this.getString(row[11]));
                     userList.add(user);
                     uniqueUsers.put(userId, user);
            	} else {
            		user = uniqueUsers.get(userId);
            		userAccessList = user.getUserAccessList();
            	}
            	UserAccess userAccess = new UserAccess();
            	Access access = new Access();
            	userAccess.setAccess(access);
            	userAccess.setAccessOverriden(this.getBoolean(row[12]));
            	access.setCode(this.getString(row[13]));
            	access.setDescription(this.getString(row[14]));
            	userAccessList.add(userAccess);
           }
        }
        return userList;
    }

    public void updateModifiedDateOfUserEvent(Long userEventId) {
    	Session session = currentSession();
        session.createQuery("Update UserEvent userEvent " +
                "Set userEvent.modifiedDate = :modifiedDate " +
                "where userEvent.id = :userEventId ")
                .setParameter("modifiedDate", new Date())
                .setParameter("userEventId", userEventId)
                .executeUpdate();
    }


    /**
     *  This method will retrieve the total number of users for a given firm and subscription
     *
     * @param userId
     * @param accessId
     * @return
     */
    public int getFirmUsersCount(Long adminUserId, Long accessId){
    	Session session = currentSession();
        Query sqlQuery =  session.getNamedQuery("GET_FIRM_USERS_COUNT")
        							.setParameter("adminUserId", adminUserId)
        							.setParameter("accessId",  accessId);
        List<Object> resultList = sqlQuery.list();
        int totalUsers = 0;
		if(resultList.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
			if(resultListIterator.hasNext()) {
				totalUsers = (Integer) resultListIterator.next();
			}
		}
		return totalUsers;
    }


    /**
     * This method return all the users for a given firm by passing the userName.
     * It will also accept accessId and filter users based on userName and accessId
	 *
	 * @param userName
	 * @param accessId
	 * @return
	 */
    public List<FirmUserDTO> getFirmUsersbySubscriptionAndUserName(String userName, Long accessId){
    	Session session = currentSession();
    	Query query = null;
		query = session.getNamedQuery("GET_FIRM_USERS_BY_USERNAME_AND_SUBSCRIPTION")
				  .setParameter("userName", userName)
				  .setParameter("accessId",  accessId)
				  .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
    	List<Object> resultSet = query.list();
    	List<FirmUserDTO> users = new ArrayList<FirmUserDTO>();
        if(resultSet.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            for(Object object : resultSet)
            {
    			Map row = (Map) object;
            	FirmUserDTO userDTO = new FirmUserDTO();
                userDTO.setId(this.getLongFromBigInteger(row.get("id")));
                userDTO.setUsername(this.getString(row.get("userName")));
                userDTO.setFirstName(this.getString(row.get("firstName")));
                userDTO.setLastName(this.getString(row.get("lastName")));
                userDTO.setPhone(this.getString(row.get("phone")));
                userDTO.setFirmName(this.getString(row.get("firmName")));
                userDTO.setFirmNumber(this.getString(row.get("firmNumber")));
                userDTO.setBarNumber(this.getString(row.get("barNumber")));
                userDTO.setIsFirmAccessAdmin(this.getBoolean(row.get("isFirmAccessAdmin")));
                userDTO.setUserAccessActive(this.getBoolean(row.get("userAccessStatus")));
                userDTO.setUserLocked(!this.getBoolean(row.get("userNonLocked")));
                userDTO.setUserAccessId(this.getLongFromInteger(row.get("userAccessId")));
                userDTO.setAccessId(this.getLongFromInteger(row.get("accessId")));
                userDTO.setNodeName(this.getString(row.get("nodeName")));
                users.add(userDTO);
           }
        }
        return users;
    }

	/**
     * This method return all the users for a given firm (admin user id):
     * If subscription (accessId is supplied then it will find the users under a given subscriptions
	 *
	 * @param userName
	 * @return
	 */
    public List<FirmUserDTO> getFirmUsers(String adminUserName, Long accessId){
    	Session session = currentSession();
    	Query query = null;
    	if(accessId == null){
    		// Find Firm level users for all the subscriptions
    		query = session.getNamedQuery("GET_FIRM_USERS")
    						  .setParameter("adminUserName", adminUserName);
    	} else {
    		// Find firm level users for a given subscriptions
    		query = session.getNamedQuery("GET_FIRM_USERS_BY_SUBSCRIPTION")
					  .setParameter("adminUserName", adminUserName)
					  .setParameter("accessId",  accessId);
    	}
    	List<Object> resultSet = query.list();
    	List<FirmUserDTO> users = new ArrayList<FirmUserDTO>();
        if(resultSet.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
            	FirmUserDTO userDTO = new FirmUserDTO();
                Object[] row = (Object[]) resultSetIterator.next();
                userDTO.setUserId(this.getLongFromBigInteger(row[0]));
                userDTO.setUsername(this.getString(row[1]));
                userDTO.setFirstName(this.getString(row[2]));
                userDTO.setLastName(this.getString(row[3]));
                userDTO.setPhone(this.getString(row[4]));
                //
                if(accessId != null && !accessId.equals(0L)){
	                boolean purchasedDocument = this.getString(row[5]).equals("Y") ? true : false;
	                boolean paidRecurringFees = this.getString(row[6]).equals("Y") ? true : false;

	                //If user has either purchased documents OR paid for recurring fees then he has paid for the documents
	                userDTO.setPaidTransactions(purchasedDocument | paidRecurringFees);
	                userDTO.setUserAccessId(this.getLongFromInteger(row[7]));
	                userDTO.setUserAccessActive(this.getString(row[8]).equals("Y") ? true : false);
	                userDTO.setUserLocked(this.getString(row[9]).equals("Y") ? false : true);
	                userDTO.setNodeName(this.getString(row[10]));
                } else {
                	userDTO.setUserLocked(this.getString(row[5]).equals("Y") ? false : true);
                	userDTO.setNodeName(this.getString(row[6]));
                }


                users.add(userDTO);
           }
        }
        return users;
    }

    /**
     * Find out UserAccess List by user name
     *
     * @param userName
     * @return
     */
    public List<UserAccess> getAdminUserAccessByUserName(String userName){
    	Session session = currentSession();
    	Query query = session.createQuery(GET_ADMIN_USER_ACCESS_BY_USER_ID)
    						  .setParameter("userName", userName);

        List<UserAccess> userAccessList = query.list();
        return userAccessList;
    }

    /**
     * Find User Access by user access ids
     *
     * @param accessIds
     * @return
     */
    public List<UserAccess> getUserAccessByUserIdAccessIds(String userName, List<Long> accessIds){
    	Session session = currentSession();
    	Query query = session.createQuery(GET_USER_ACCESS_BY_USER_ID_ACCESS_IDS)
    							.setParameter("userName", userName)
    							.setParameterList("accessIds", accessIds);

        List<UserAccess> userAccessList = query.list();
        return userAccessList;
    }

    /**
     * Find Firm User Access by user access ids
     *
     * @param accessIds
     * @return
     */
    public List<UserAccess> getFirmUserAccessByUserNameAccessId(String userName, Long accessId){
    	Session session = currentSession();
    	Query query = session.createQuery(GET_FIRM_USER_ACCESS_BY_USERID_ACCESSIDS)
    							.setParameter("userName", userName)
    							.setParameter("accessId", accessId);

        List<UserAccess> userAccessList = query.list();
        return userAccessList;
    }

    /**
     * Get all the firm level users (child) by parent/admin user
     *
     * @param adminUserId
     * @param accessId
     * @return
     */
    public List<UserAccess> getUserAccessForFirmLevelUsers(Long adminUserId, Long accessId){
    	Session session = currentSession();
    	Query query = session.getNamedQuery("GET_FIRM_LEVEL_USER_ACCESS")
    							.setParameter("adminUserId", adminUserId)
    							.setParameter("accessId", accessId);

    	List<Object> resultSet = query.list();

    	List<UserAccess> userAccessList = new ArrayList<UserAccess>();
        if(resultSet.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
                Object[] row = (Object[]) resultSetIterator.next();

                UserAccess userAccess = new UserAccess();
            	userAccess.setId(this.getLongFromInteger(row[0]));

            	Access access = new Access();
            	access.setId(this.getLongFromInteger(row[1]));
            	userAccess.setAccess(access);

            	User user = new User();
            	user.setId(this.getLongFromBigInteger(row[2]));
            	user.setUsername(this.getString(row[3]));
            	userAccess.setUser(user);

            	userAccess.setActive(this.getString(row[4]).equals("Y") ? true : false);
            	userAccess.setAccessOverriden(this.getString(row[5]).equals("Y") ? true : false);
            	userAccess.setAuthorized(this.getString(row[6]).equals("Y") ? true : false);
            	userAccess.setFirmAccessAdmin(this.getString(row[7]).equals("Y") ? true : false);
            	userAccess.setFirmAdminUserAccessId(this.getLongFromInteger(row[8]));
            	userAccessList.add(userAccess);
            }
        }
        return userAccessList;
    }

    public CreditCard getFirmCreditCardDetails(String username) {
        CreditCard cardInfo = null;
        Session session = currentSession();
        List resultSet = session.getNamedQuery("GET_FIRM_CREDIT_CARD_DETAILS")
                        .setParameter("userName", username).list();
        if(resultSet.size() > 0){
            cardInfo = new CreditCard();
            Object[] row = (Object[]) resultSet.get(0);
            cardInfo.setId(this.getLongFromInteger(row[0]));
            cardInfo.setName(this.getString(row[1]));
            cardInfo.setNumber(row[2] == null ? null : this.getPbeStringEncryptor().decrypt(row[2].toString()));
            cardInfo.setExpiryMonth(this.getInteger(row[3]));
            cardInfo.setExpiryYear(this.getInteger(row[4]));
            cardInfo.setAddressLine1(this.getString(row[5]));
            cardInfo.setAddressLine2(this.getString(row[6]));
            cardInfo.setCity(this.getString(row[7]));
            cardInfo.setState(this.getString(row[8]));
            cardInfo.setZip(this.getString(row[9]));
            cardInfo.setPhone(this.getLongFromBigInteger(row[10]));
            cardInfo.setActive(this.getBoolean(row[11]));
            cardInfo.setCreatedDate(this.getDate(row[12]));
            cardInfo.setModifiedDate(this.getDate(row[13]));
            cardInfo.setModifiedBy(this.getString(row[14]));
            cardInfo.setUserId(this.getLongFromBigInteger(row[15]));
            cardInfo.setCreatedBy(this.getString(row[16]));
        }
        return cardInfo;
    }

    public CreditCard getCreditCardDetails(String username) {
        CreditCard cardInfo = null;
        Session session = currentSession();
        List resultSet = session.getNamedQuery("GET_CREDIT_CARD_DETAILS")
                        .setParameter("userName", username).list();
        if(resultSet.size() > 0){
            cardInfo = new CreditCard();
            Object[] row = (Object[]) resultSet.get(0);
            cardInfo.setId(this.getLongFromInteger(row[0]));
            cardInfo.setName(this.getString(row[1]));
            cardInfo.setNumber(row[2] == null ? null : this.getPbeStringEncryptor().decrypt(row[2].toString()));
            cardInfo.setExpiryMonth(this.getInteger(row[3]));
            cardInfo.setExpiryYear(this.getInteger(row[4]));
            cardInfo.setAddressLine1(this.getString(row[5]));
            cardInfo.setAddressLine2(this.getString(row[6]));
            cardInfo.setCity(this.getString(row[7]));
            cardInfo.setState(this.getString(row[8]));
            cardInfo.setZip(this.getString(row[9]));
            cardInfo.setPhone(this.getLongFromBigInteger(row[10]));
            cardInfo.setActive(this.getBoolean(row[11]));
            cardInfo.setCreatedDate(this.getDate(row[12]));
            cardInfo.setModifiedDate(this.getDate(row[13]));
            cardInfo.setModifiedBy(this.getString(row[14]));
            cardInfo.setUserId(this.getLongFromBigInteger(row[15]));
            cardInfo.setCreatedBy(this.getString(row[16]));
        }
        return cardInfo;
    }


    public CreditCard getCreditCardDetails(Long userId) {
        CreditCard cardInfo = null;
        Session session = currentSession();
        List resultSet = session.getNamedQuery("GET_CREDIT_CARD_DETAILS_BY_USERID")
                         .setParameter("userId", userId).list();
        if(resultSet.size() > 0){
            cardInfo = new CreditCard();
            Object[] row = (Object[]) resultSet.get(0);
            cardInfo.setId(this.getLongFromInteger(row[0]));
            cardInfo.setName(this.getString(row[1]));
            cardInfo.setNumber(row[2] == null ? null : this.getPbeStringEncryptor().decrypt(row[2].toString()));
            cardInfo.setExpiryMonth(this.getInteger(row[3]));
            cardInfo.setExpiryYear(this.getInteger(row[4]));
            cardInfo.setAddressLine1(this.getString(row[5]));
            cardInfo.setAddressLine2(this.getString(row[6]));
            cardInfo.setCity(this.getString(row[7]));
            cardInfo.setState(this.getString(row[8]));
            cardInfo.setZip(this.getString(row[9]));
            cardInfo.setPhone(this.getLongFromBigInteger(row[10]));
            cardInfo.setActive(this.getBoolean(row[11]));
            cardInfo.setCreatedDate(this.getDate(row[12]));
            cardInfo.setModifiedDate(this.getDate(row[13]));
            cardInfo.setModifiedBy(this.getString(row[14]));
            cardInfo.setUserId(this.getLongFromBigInteger(row[15]));
            cardInfo.setCreatedBy(this.getString(row[16]));
        }
        return cardInfo;
    }


    public void saveCreditCard(List<CreditCard> creditCards) {
        Session session = currentSession();
        for(CreditCard creditCard:creditCards) {
            session.saveOrUpdate(creditCard);
        }
        session.flush();
    }

    public void saveCreditCard(CreditCard creditCard) {
        Session session = currentSession();
        session.saveOrUpdate(creditCard);
        session.flush();
    }

    public int authorize(List<Long> userAccessIds, boolean isAuthorized, String modifiedBy, boolean isActive,
    		boolean isFirmAccessAdmin) {
        Boolean isAuthorizedObj = Boolean.FALSE;
        if (isAuthorized) {
            isAuthorizedObj = Boolean.TRUE;
        }
        Boolean isActiveObj = Boolean.FALSE;
        if (isActive) {
        	isActiveObj = Boolean.TRUE;
        }
        Boolean isFirmAccessAdminObj = Boolean.FALSE;
        if (isFirmAccessAdmin) {
        	isFirmAccessAdminObj = Boolean.TRUE;
        }
        Session session = currentSession();
        int recordsModified = session.createQuery("Update UserAccess useraccess " +
                                "Set useraccess.isAuthorized = :isAuthorized, " +
                                "useraccess.modifiedDate = :modifiedDate, " +
                                "useraccess.authorizationDate = :modifiedDate, " +
                                "useraccess.authorizedBy = :modifiedBy, " +
                                "useraccess.modifiedBy = :modifiedBy, " +
                                "useraccess.active = :isActive, " +
                                "useraccess.isFirmAccessAdmin = :isFirmAccessAdmin " +
                                "where useraccess.id in (:userAccessIds)")
                                .setParameter("isAuthorized", isAuthorizedObj)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("modifiedBy", modifiedBy)
                                .setParameter("isActive", isActiveObj)
                                .setParameter("isFirmAccessAdmin", isFirmAccessAdminObj)
                                .setParameterList("userAccessIds", userAccessIds)
                                .executeUpdate();
        return recordsModified;

    }


    public FirmUserDTO getUserByUserAccessId(Long userAccessId){
    	Session session = currentSession();
   		Query query = session.getNamedQuery("GET_USER_BY_USER_ACCESS_ID")
    						  .setParameter("userAccessId", userAccessId);
    	List<Object> resultSet = query.list();
    	FirmUserDTO user = null;
        if(resultSet.size() > 0){
            ListIterator<Object> resultSetIterator = (ListIterator<Object>) resultSet.listIterator();
            while(resultSetIterator.hasNext()) {
            	user = new FirmUserDTO();
                Object[] row = (Object[]) resultSetIterator.next();
                user.setUsername(this.getString(row[0]));
                user.setUserId(this.getLongFromBigInteger(row[1]));
                user.setAccessId(this.getLongFromInteger(row[2]));
                user.setIsFirmAccessAdmin(this.getBoolean(row[3]));
                user.setFirmLevelAccess(this.getBoolean(row[4]));
           }
        }
    	return user;
    }

    public int updateFirmCreditCardInUserAccount(Long userAccessId, Long creditCardId){
        Session session = currentSession();
        int recordsModified = session.createSQLQuery("Update ECOMM_USERS_ACCOUNT "
        		+ "SET "
        		+ "CREDIT_CARD_ID = :creditCardId "
        		+ "WHERE USER_ACCESS_ID = :userAccessId ")
                                .setParameter("userAccessId", userAccessId)
                                .setParameter("creditCardId", creditCardId)
                                .executeUpdate();
        return recordsModified;
    }

    public void updateisEmailNotificationSent(String userName, boolean isEmailNotificationSent) {
		 Session session = currentSession();
			session.createQuery("Update User  user " +
			"Set user.isEmailNotificationSent  = :isEmailNotificationSent " +
			"where user.username = :username ")
			.setParameter("isEmailNotificationSent", isEmailNotificationSent)
			.setParameter("username", userName)
			.executeUpdate();
	     }

}
