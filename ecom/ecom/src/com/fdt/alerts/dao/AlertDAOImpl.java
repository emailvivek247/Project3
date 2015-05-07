package com.fdt.alerts.dao;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fdt.alerts.dto.AlertNameHyperLinkKeyMap;
import com.fdt.alerts.entity.UserAlert;
import com.fdt.common.dao.AbstractBaseDAOImpl;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.email.dto.EMailDTO;
import com.fdt.security.exception.DuplicateAlertException;
import com.fdt.security.exception.UserNameNotFoundException;

@Repository
public class AlertDAOImpl extends AbstractBaseDAOImpl implements AlertDAO {

private final Logger logger = LoggerFactory.getLogger(getClass());

    private static int ALERT_PAGE_SIZE = 3;

    public List<UserAlert> getUserAlerts(String nodeName, int firstResult) {
        List<UserAlert> userAlerts = null;
        UserAlert userAlert = null;
        List<Long> userIdList = new LinkedList<Long>();
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_NUMBER_OF_ALERTS_FOR_A_USER");
        query.setParameter("nodeName", nodeName);
        query.setFirstResult(firstResult);
        query.setMaxResults(ALERT_PAGE_SIZE);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                userIdList.add(this.getLongFromBigInteger(row[0]));

            }
            Query query2 = session.getNamedQuery("GET_USER_ALERTS_FOR_NODE");
            query2.setParameter("nodeName", nodeName);
            query2.setParameterList("userIdList", userIdList);
            List<Object> resultList2 = query2.list();
            if(resultList2.size() > 0) {
                userAlerts = new LinkedList<UserAlert>();
                ListIterator<Object> resultListIterator2 = (ListIterator<Object>) resultList2.listIterator();
                while(resultListIterator2.hasNext()) {
                    Object[] row2 = (Object[]) resultListIterator2.next();
                    userAlert =  new UserAlert();
                    userAlert.setId(this.getLongFromBigInteger(row2[0]));
                    userAlert.setUserId(this.getLongFromBigInteger(row2[1]));
                    userAlert.setUsername(this.getString(row2[2]));
                    userAlert.setFirstName(this.getString(row2[3]));
                    userAlert.setLastName(this.getString(row2[4]));
                    userAlert.setNodeName(this.getString(row2[5]));
                    userAlert.setAlertName(this.getString(row2[6]));
                    userAlert.setBaseURL(this.getString(row2[7]));
                    userAlert.setIndexName(this.getString(row2[8]));
                    userAlert.setTemplateName(this.getString(row2[9]));
                    userAlert.setAlertQuery(this.getString(row2[10]));
                    userAlert.setCreatedDate(this.getDate(row2[11]));
                    userAlert.setModifiedDate(this.getDate(row2[12]));
                    userAlert.setModifiedBy(this.getString(row2[13]));
                    userAlert.setCreatedBy(this.getString(row2[17]));
                    userAlert.setActive(this.getBoolean(row2[14]));
                    userAlert.setSiteName(this.getString(row2[15]));
                    userAlert.setComments(this.getString(row2[16]));
                    userAlerts.add(userAlert);
                }
            }
        }
        return userAlerts;
    }


    public void sendEmail(EMailDTO emailDTO, List<AlertNameHyperLinkKeyMap> alertNameHyperLinkKeyMap) {

    }


    public NodeConfiguration getNodeConfiguration(String nodeName) {
        return null;
    }


    public List<UserAlert> getUserAlertsByUserName(String userName, String nodeName) {
        List<UserAlert> userAlerts = new LinkedList<UserAlert>();
        UserAlert userAlert = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_USER_ALERTS_FOR_USER");
        query.setParameter("nodeName", nodeName);
        query.setParameter("userName", userName);
        List<Object> resultList2 = query.list();
        if(resultList2.size() > 0) {
            ListIterator<Object> resultListIterator2 = (ListIterator<Object>) resultList2.listIterator();
            while(resultListIterator2.hasNext()) {
                Object[] row = (Object[]) resultListIterator2.next();
                userAlert =  new UserAlert();
                userAlert.setId(this.getLongFromBigInteger(row[0]));
                userAlert.setUserId(this.getLongFromBigInteger(row[1]));
                userAlert.setNodeName(this.getString(row[2]));
                userAlert.setAlertName(this.getString(row[3]));
                userAlert.setBaseURL(this.getString(row[4]));
                userAlert.setIndexName(this.getString(row[5]));
                userAlert.setTemplateName(this.getString(row[6]));
                userAlert.setAlertQuery(this.getString(row[7]));
                userAlert.setCreatedDate(this.getDate(row[8]));
                userAlert.setModifiedDate(this.getDate(row[9]));
                userAlert.setModifiedBy(this.getString(row[10]));
                userAlert.setActive(this.getBoolean(row[11]));
                userAlert.setSiteName(this.getString(row[12]));
                userAlert.setComments(this.getString(row[13]));
                userAlerts.add(userAlert);
            }
        }
        return userAlerts;
    }

    public void saveUserAlert(UserAlert userAlert) throws UserNameNotFoundException, DuplicateAlertException {

    }

    public void deleteUserAlerts(String userName, List<Long> userAlertIdList) {
        Session session = currentSession();
        session.getNamedQuery("DELETE_USER_ALERTS_BY_USER_NAME_AND_ALERT_ID_LIST")
                .setParameter("userName", userName)
                .setParameterList("userAlertIdList", userAlertIdList)
                .executeUpdate();
    }

    public boolean isAlertExist(String userName, String alertName) {
        Session session = currentSession();
        Query sqlQuery = session.getNamedQuery("IS_DUPLICATE_ALERT")
                             .setParameter("userName", userName)
                             .setParameter("alertName", alertName);
        List<Object> resultList = sqlQuery.list();
        boolean isAlertExist = false;
        if(resultList.size() > 0) {
            isAlertExist = true;
        }
        return isAlertExist;
    }

    public int saveUserAlert(String userName, UserAlert userAlert) {
        Session session = currentSession();
        String isActive = "Y";
        if (!userAlert.isActive()) {
            isActive ="N";
        }
        int noOfRecordsDeleted = session.getNamedQuery("INSERT_USER_ALERT")
                                        .setParameter("userName", userName)
                                        .setParameter("nodeName", userAlert.getNodeName())
                                        .setParameter("alertName", userAlert.getAlertName().trim())
                                        .setParameter("baseURL", userAlert.getBaseURL())
                                        .setParameter("indexName", userAlert.getIndexName())
                                        .setParameter("templateName", userAlert.getTemplateName())
                                        .setParameter("alertQuery", userAlert.getAlertQuery().trim())
                                        .setParameter("createdDate", new Date())
                                        .setParameter("modifiedDate",new Date())
                                        .setParameter("modifiedUserName", userName)
                                        .setParameter("isActive", isActive)
                                        .setParameter("siteName", userAlert.getSiteName())
                                        .setParameter("comments", userAlert.getComments())
                                        .executeUpdate();
        return noOfRecordsDeleted;
    }
}