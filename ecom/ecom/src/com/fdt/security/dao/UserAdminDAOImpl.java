package com.fdt.security.dao;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.CreditCard;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.security.entity.User;
import com.fdt.subscriptions.dto.SubscriptionDTO;


@Repository
@SuppressWarnings("unchecked")
public class UserAdminDAOImpl extends AbstractBaseDAOImpl implements UserAdminDAO {



	@Override
    public PageRecordsDTO findUsers(SearchCriteriaDTO searchCriteria) {
        List<User> users = new LinkedList<User>();
        User user = null;
        String firstName = "%%";
        String lastName = "%%";
        String userName = "%%";

        String firmName = "%%";
        String firmNumber = "%%";
        String barNumber = "%%";

        String strActive = "%%";
        String strAuthorized= "%%";
        String strSiteId = "%%";
        String strAccessId = "%%";
        String strPaidUser = "%%";
        String authorized = "%%";
        String useraccessActive = "%%";
        String strOverriddenAccess = "%%";
        if (searchCriteria.getActive().equalsIgnoreCase("ACTIVE")) {
        	strActive = "Y";
        } else if (searchCriteria.getActive().equalsIgnoreCase("INACTIVE")) {
        	strActive = "N";
        }
        if (searchCriteria.getPaiduser().equalsIgnoreCase("Y")) {
        	strPaidUser = "Y";
        } else if (searchCriteria.getPaiduser().equalsIgnoreCase("N")) {
        	strPaidUser = "N";
        }
        if(!StringUtils.isBlank(searchCriteria.getFirstName())) {
        	firstName = "%" + searchCriteria.getFirstName() + "%";
        }
        if(!StringUtils.isBlank(searchCriteria.getLastName())) {
        	lastName = "%" + searchCriteria.getLastName() + "%";
        }
        if(!StringUtils.isBlank(searchCriteria.getUserName())) {
        	userName = "%" + searchCriteria.getUserName() + "%";
        }
        if(!StringUtils.isBlank(searchCriteria.getFirmName())) {
        	firmName = "%" + searchCriteria.getFirmName() + "%";
        }
        if(!StringUtils.isBlank(searchCriteria.getFirmNumber())) {
        	firmNumber = "%" + searchCriteria.getFirmNumber() + "%";
        }
        if(!StringUtils.isBlank(searchCriteria.getBarNumber())) {
        	barNumber = "%" + searchCriteria.getBarNumber() + "%";
        }
        if(searchCriteria.getSiteId() != null) {
        	strSiteId = searchCriteria.getSiteId().toString();
        }
        if(searchCriteria.getAccessId() != null) {
        	strAccessId = searchCriteria.getAccessId().toString();
        }
        if (searchCriteria.getUserSubscriptionStatus().equalsIgnoreCase("ACTIVE")) {
        	useraccessActive = "Y";
        	strOverriddenAccess = "N";
        } else if (searchCriteria.getUserSubscriptionStatus().equalsIgnoreCase("INACTIVE")) {
        	useraccessActive = "N";
        	strOverriddenAccess = "N";
        } else if (searchCriteria.getUserSubscriptionStatus().equalsIgnoreCase("OVERRIDDEN")) {
        	useraccessActive = "Y";
        	strOverriddenAccess = "Y";
        } else if (searchCriteria.getUserSubscriptionStatus().equalsIgnoreCase("PENDINGAUTH")) {
        	strAuthorized = "N";
        }
        Session session = currentSession();
        Query query = session.getNamedQuery("FIND_USERS");
        String sql = query.getQueryString();

        // See if sorting columns are supplied. If it's supplied then map it to the SQL column.
        // By Default we have ORDER BY DATE_TIME_CREATED DESC : Replace it with new column name
        // Currently , order by is supported for one column only.

        String sortColumn = searchCriteria.getSortField();
        if(!StringUtils.isBlank(sortColumn) && !StringUtils.isBlank(searchCriteria.getSortType())){
        	sql = sql.replaceFirst("ORDER BY DATE_TIME_CREATED DESC", "ORDER BY " + sortColumn + " "
        		+ searchCriteria.getSortType().toUpperCase());
        }

        query =	session.createSQLQuery(sql)
                              .setParameter("firstName", firstName)
                              .setParameter("lastName", lastName)
                              .setParameter("userName", userName)
                              .setParameter("siteId", strSiteId)
                              .setParameter("accessId", strAccessId)
                              .setParameter("active", strActive)
                              .setParameter("pendingAuth", strAuthorized)
                              .setParameter("overriddenAccess", strOverriddenAccess)
                              .setParameter("paidUser", strPaidUser)
                              .setParameter("authorized", authorized)
                              .setParameter("useraccessActive", useraccessActive)
                              .setParameter("firmName", firmName)
                              .setParameter("firmNumber", firmNumber)
                              .setParameter("barNumber", barNumber)
                              .setParameter("barNumber", barNumber)
                              .setParameter("offsetRows", searchCriteria.getStartingFrom())
                              .setParameter("numberOfRows", searchCriteria.getNumberOfRecords());

        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                user = new User();
                user.setId(this.getLongFromBigInteger(row[0]));
                user.setUsername(this.getString(row[1]));
                user.setFirstName(this.getString(row[2]));
                user.setLastName(this.getString(row[3]));
                user.setAccountNonLocked(this.getBoolean(row[4]));
                user.setLastLoginTime(this.getDate(row[5]));
                user.setCreatedDate(this.getDate(row[6]));
                user.setModifiedDate(this.getDate(row[7]));
                user.setPhone(this.getString(row[8]));
                user.setActive(this.getBoolean(row[9]));
                user.setWaitingForAuthorization(this.getBoolean(row[10]));
                user.setAccessOverridden(this.getBoolean(row[11]));
                user.setPayedUser(this.getBoolean(row[12]));
                user.setFirmName(this.getString(row[13]));
                user.setFirmNumber(this.getString(row[14]));
                user.setBarNumber(this.getString(row[15]));
                String userAccessActive = this.getString(row[16]);
                user.setPaymentDue(userAccessActive.equals("N") ? true : false);
                user.setFirmAdmin(this.getBoolean(row[17]));
                users.add(user);
            }
        }
        int count = this.findUsersCount(firstName, lastName, userName, strSiteId, strAccessId, strActive, strAuthorized,
        				strOverriddenAccess, strPaidUser, authorized, useraccessActive, firmName, firmNumber, barNumber);
		PageRecordsDTO gridUsers = new PageRecordsDTO(users, count);
        return gridUsers;
    }

	private int findUsersCount(String firstName, String lastName, String userName, String strSiteId, String strAccessId,
			String strActive, String strAuthorized, String strOverriddenAccess, String strPaidUser, String authorized,
				String useraccessActive, String firmName, String firmNumber, String barNumber){
		Session session = currentSession();
        Query countQuery = session.getNamedQuery("FIND_USERS_COUNT")
                .setParameter("firstName", firstName)
                .setParameter("lastName", lastName)
                .setParameter("userName", userName)
                .setParameter("siteId", strSiteId)
                .setParameter("accessId", strAccessId)
                .setParameter("active", strActive)
                .setParameter("pendingAuth", strAuthorized)
                .setParameter("overriddenAccess", strOverriddenAccess)
                .setParameter("paidUser", strPaidUser)
                .setParameter("authorized", authorized)
                .setParameter("useraccessActive", useraccessActive)
                .setParameter("firmName", firmName)
                .setParameter("firmNumber", firmNumber)
                .setParameter("barNumber", barNumber);
		List<Object> countResultList = countQuery.list();
		int count = 0;
		if(countResultList.size() > 0) {
			ListIterator<Object> resultListIterator = (ListIterator<Object>) countResultList.listIterator();
			if(resultListIterator.hasNext()) {
				count = (Integer) resultListIterator.next();
			}
		}
		return count;

	}

	public List<SubscriptionDTO> getUserInfoForAdmin(String userName, String siteName) {
		User user = null;
		List<SubscriptionDTO> subscriptionDTOs = null;
        Session session = currentSession();
        if(StringUtils.isBlank(siteName)) {
        	siteName = "%%";
        }
        Query query = session.getNamedQuery("GET_USER_INFO_FOR_ADMIN")
                              .setParameter("userName", userName)
                              .setParameter("siteName", siteName);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
        	user = new User();
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            subscriptionDTOs = new LinkedList<SubscriptionDTO>();
            while(resultListIterator.hasNext()) {
            	Object[] row = (Object[]) resultListIterator.next();
        	    SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        	    subscriptionDTO.setUser(user);
        	    subscriptionDTOs.add(subscriptionDTO);
        	    subscriptionDTO.setSiteId(this.getLongFromInteger(row[0]));
                subscriptionDTO.setSiteName(this.getString(row[1]));
                subscriptionDTO.setUserAccountId(this.getLongFromBigInteger(row[2]));
                subscriptionDTO.setUserAccessId(this.getLongFromInteger(row[3]));
                subscriptionDTO.setAccountModifiedDate(this.getDate(row[4]));
                subscriptionDTO.setUserAccountActive(this.getBoolean(row[5]));
                subscriptionDTO.setLastBillingDate(this.getDate(row[6]));
                subscriptionDTO.setNextBillingDate(this.getDate(row[7]));
                subscriptionDTO.setIsMarkedForCancellation(this.getBoolean(row[8]));
                subscriptionDTO.setAccessId(this.getLongFromInteger(row[9]));
                subscriptionDTO.setSubscription(this.getString(row[10]));
                subscriptionDTO.setAuthorizationRequired(this.getBoolean(row[11]));
                subscriptionDTO.setActive(this.getBoolean(row[12]));
                subscriptionDTO.setAccessOverridden(this.getBoolean(row[13]));
                subscriptionDTO.setAuthorized(this.getBoolean(row[14]));
                subscriptionDTO.setAuthorizedBy(this.getString(row[15]));
                subscriptionDTO.setAuthorizationDate(this.getDate(row[16]));
                subscriptionDTO.setComments(this.getString(row[17]));
                subscriptionDTO.setModifiedBy(this.getString(row[18]));
                subscriptionDTO.setCreatedBy(this.getString(row[19]));
                subscriptionDTO.setModifiedDate(this.getDate(row[20]));
                subscriptionDTO.setCreatedDate(this.getDate(row[21]));
                subscriptionDTO.setCategory(this.getString(row[22]));
                subscriptionDTO.setSubscriptionFee(this.getDoubleFromBigDecimal(row[23]));
                subscriptionDTO.setFirmLevelAccess(this.getBoolean(row[49]));
                subscriptionDTO.setFirmAccessAdmin(this.getBoolean(row[50]));
                subscriptionDTO.setOverriddenUntillDate(this.getDate(row[52]));
            	if(user.getId() != null) {
            		continue;
            	}
	            user.setId(this.getLongFromBigInteger(row[24]));
	            user.setUsername(this.getString(row[25]));
	            user.setFirstName(this.getString(row[26]));
	            user.setLastName(this.getString(row[27]));
	            user.setLastLoginTime(this.getDate(row[28]));
	            user.setCreatedDate(this.getDate(row[29]));
	            user.setRegisteredNode(this.getString(row[30]));
	            user.setCurrentLoginTime(this.getDate(row[31]));
	            user.setPhone(this.getString(row[32]));
	            user.setActive(this.getBoolean(row[33]));
	            user.setAccountNonExpired(this.getBoolean(row[34]));
	            user.setCredentialsNonExpired(this.getBoolean(row[35]));
	            user.setAccountNonLocked(this.getBoolean(row[36]));
	            user.setFirmName(this.getString(row[51]));
	            CreditCard creditCard = null;
	            String number =  null;
	            if (row[37] != null) {
	            	user.setCardAvailable(true);
	            	creditCard = new CreditCard();
	            	creditCard.setId(this.getLongFromInteger(row[37]));
	                number = this.getPbeStringEncryptor().decrypt(row[38].toString());
	                creditCard.setCardType(SystemUtil.getCardType(number));
	                int length = number.length();
	                number = number.replace(number.substring(0, length-4), "XXXX-XXXX-XXXX-");
	                creditCard.setNumber(row[38] == null ? null : number);
	                creditCard.setName(this.getString(row[39]));
	                creditCard.setExpiryMonth(this.getInteger(row[40]));
	                creditCard.setExpiryYear(this.getInteger(row[41]));
	                creditCard.setAddressLine1(this.getString(row[42]));
	                creditCard.setAddressLine2(this.getString(row[43]));
	                creditCard.setCity(this.getString(row[44]));
	                creditCard.setState(this.getString(row[45]));
	                creditCard.setZip(this.getString(row[46]));
	                creditCard.setPhone(this.getLongFromBigInteger(row[47]));
	                creditCard.setActive(this.getBoolean(row[48]));
	                user.setCreditCardActive(this.getBoolean(row[48]));
	                user.setCreditCard(creditCard);
	            }
            }
        }
        return subscriptionDTOs;
	}

}
