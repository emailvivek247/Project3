package com.fdt.security.dao;

import static com.fdt.security.dao.EComAdminUserHQL.FIND_USER_EVENT_BY_USER_NAME;
import static com.fdt.security.dao.EComAdminUserHQL.FIND_USER_EVENT_BY_USER_NAME_REQ_TOKEN;
import static com.fdt.security.dao.EComAdminUserHQL.LOAD_USER_BY_USERNAME;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.list.SetUniqueList;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import com.fdt.common.dao.EcomAdminAbstractBaseDAOImpl;
import com.fdt.security.entity.EComAdminAccess;
import com.fdt.security.entity.EComAdminSite;
import com.fdt.security.entity.EComAdminUser;
import com.fdt.security.entity.EComAdminUserAccess;
import com.fdt.security.entity.EComAdminUserEvent;
import com.fdt.security.entity.EComAdminUserSite;

@Repository
@SuppressWarnings("unchecked")
public class EComAdminUserDAOImpl extends EcomAdminAbstractBaseDAOImpl implements EComAdminUserDAO {

    @Override
    public EComAdminUser getUserDetails(String username) {
        EComAdminUser user = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_ADMIN_USER_DETAILS");
        query.setParameter("username",username);
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            Object[] row = (Object[]) resultListIterator.next();
            user = new EComAdminUser();
            user.setId(row[0] == null ? null : (Long)((BigInteger)row[0]).longValue());
            user.setUsername(row[1] == null ? null : row[1].toString());
            user.setFirstName(row[2] == null ? null : row[2].toString());
            user.setLastName(row[3] == null ? null : row[3].toString());
            user.setActive(row[4] == null ? false : this.convertToBoolean( row[4].toString()));
            user.setAccountNonExpired(row[5] == null ? false : this.convertToBoolean( row[5].toString()));
            user.setAccountNonLocked(row[6] == null ? false : this.convertToBoolean( row[6].toString()));
            user.setCredentialsNonExpired(row[7] == null ? false : this.convertToBoolean( row[7].toString()));
            user.setLastLoginTime(row[8] == null ? null :  (Date)((Timestamp)row[8]));
            user.setCreatedDate(row[9] == null ? null :  (Date)((Timestamp)row[9]));
            user.setPassword(row[14] == null ? null : row[14].toString());
            List<EComAdminAccess> accessList = new LinkedList<EComAdminAccess>();
            List<EComAdminSite> sites = new LinkedList<EComAdminSite>();
            resultListIterator.previous();
            while(resultListIterator.hasNext()){
                row = (Object[]) resultListIterator.next();
                EComAdminAccess eComAdminAccess = new EComAdminAccess();
                EComAdminSite ecomAdminSite = new EComAdminSite();
                ecomAdminSite.setId(row[10] == null ? null : (Long)((Integer)row[10]).longValue());
                ecomAdminSite.setName(row[11] == null ? null : row[11].toString());
                eComAdminAccess.setId(row[12] == null ? null : (Long)((Integer)row[12]).longValue());
                eComAdminAccess.setCode(row[13] == null ? null : row[13].toString());
                if (ecomAdminSite.getId() != null) {
                    sites.add(ecomAdminSite);
                }
                if (eComAdminAccess.getId() != null) {
                    accessList.add(eComAdminAccess);
                }
            }
            user.setSites(SetUniqueList.decorate(sites));
            user.setAccess(SetUniqueList.decorate(accessList));
        }
        return user;
    }

    @Override
    public List<EComAdminUser> getUsers() {
        List<EComAdminUser> users = new LinkedList<EComAdminUser>();
        EComAdminUser user = null;
        Session session = currentSession();
        Query query = session.getNamedQuery("GET_ADMIN_USERS");
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                Object[] row = (Object[]) resultListIterator.next();
                user = new EComAdminUser();
                user.setId(row[0] == null ? null : (Long)((BigInteger)row[0]).longValue());
                user.setUsername(row[1] == null ? null : row[1].toString());
                user.setFirstName(row[2] == null ? null : row[2].toString());
                user.setLastName(row[3] == null ? null : row[3].toString());
                user.setActive(row[4] == null ? false : this.convertToBoolean( row[4].toString()));
                user.setAccountNonLocked(row[5] == null ? false : this.convertToBoolean(row[5].toString()));
                users.add(user);
                List<EComAdminSite> sites = new LinkedList<EComAdminSite>();
                EComAdminSite ecomAdminSite = new EComAdminSite();
                ecomAdminSite.setName(row[6] == null ? null : row[6].toString());
                sites.add(ecomAdminSite);
                user.setSites(SetUniqueList.decorate(sites));
            }
        }
        return users;
    }

    public List<EComAdminAccess> getAccess() {
        Session session = currentSession();
        List<EComAdminAccess> accessList = null;
        Query query = session.getNamedQuery("GET_ADMIN_ACCESS");
        List<Object> resultList = query.list();
        if(resultList.size() > 0) {
            accessList = new LinkedList<EComAdminAccess>();
            ListIterator<Object> resultListIterator = (ListIterator<Object>) resultList.listIterator();
            while(resultListIterator.hasNext()) {
                EComAdminAccess access = new EComAdminAccess();
                Object[] row = (Object[]) resultListIterator.next();
                access.setId(row[0] == null ? null :  (Long)((Integer)row[0]).longValue());
                access.setCode(row[1] == null ? null : row[1].toString());
                access.setDescription(row[2] == null ? null : row[2].toString());
                accessList.add(access);
            }
        }
        return accessList;
    }

    @Override
    public void saveAccess(EComAdminAccess access) {
        Session session = currentSession();
        session.saveOrUpdate(access);
    }

    @Override
    public void saveUser(EComAdminUser user) {
        Session session = currentSession();
        session.saveOrUpdate(user);
    }

    @Override
    public void saveUserAcess(List<EComAdminUserAccess> userAccessList) {
        Session session = currentSession();
        for (EComAdminUserAccess userAccess : userAccessList) {
            session.saveOrUpdate(userAccess);
        }
    }

    @Override
    public void saveUserAcess(EComAdminUserAccess userAccess) {
        Session session = currentSession();
        session.saveOrUpdate(userAccess);
    }

    @Override
    public void saveUserEvent(EComAdminUserEvent userEvent) {
        Session session = currentSession();
        session.saveOrUpdate(userEvent);
    }

    @Override
    public void saveUserEvent(List<EComAdminUserEvent> eComAdminUserEvents) {
        Session session = currentSession();
        for (EComAdminUserEvent eComAdminUserEvent : eComAdminUserEvents) {
            session.saveOrUpdate(eComAdminUserEvent);
        }
    }

    @Override
    public void deleteUserEvents(List<EComAdminUserEvent> userEvents) {
        Session session = currentSession();
        for (EComAdminUserEvent userEvent : userEvents) {
            session.delete(userEvent);
        }
    }

    @Override
    public void saveUserSite(List<EComAdminUserSite> eComAdminUserSites) {
        Session session = currentSession();
        for (EComAdminUserSite eComAdminUserSite : eComAdminUserSites) {
            session.delete(eComAdminUserSite);
        }
    }

    @Override
    public void saveUserSite(EComAdminUserSite eComAdminUserSite) {
        Session session = currentSession();
        session.saveOrUpdate(eComAdminUserSite);
    }

    @Override
    public EComAdminUserEvent findUserEvent(String userName, String requestToken) {
        Session session = currentSession();
        EComAdminUserEvent eComAdminUserEvent = null;
        List<EComAdminUserEvent> eComAdminUserEvents = new LinkedList<EComAdminUserEvent>();
        List<Object> resultList = session.createQuery(FIND_USER_EVENT_BY_USER_NAME_REQ_TOKEN)
                .setParameter("username", userName)
                .setParameter("requestToken", requestToken)
                .list();
        Iterator<Object> iterator = resultList.iterator();
        while(iterator.hasNext()){
            Object[] row = (Object[]) iterator.next();
            eComAdminUserEvent = new EComAdminUserEvent();
            eComAdminUserEvent.setUser(row[1] == null ? null : (EComAdminUser) row[1]);
            if(row[0] != null){
                eComAdminUserEvent = (EComAdminUserEvent) row[0];
            }
            eComAdminUserEvents.add(eComAdminUserEvent);
        }
        if (eComAdminUserEvents.size() > 0) {
            eComAdminUserEvent = (EComAdminUserEvent) eComAdminUserEvents.get(0);
        }
        return eComAdminUserEvent;

    }

    public EComAdminUserEvent findUserEvent(String userName) {
        Session session = currentSession();
        EComAdminUserEvent eComAdminUserEvent = null;
        EComAdminUser user = null;
        List<Object> resultList = session.createQuery(FIND_USER_EVENT_BY_USER_NAME)
        		.setParameter("username", userName).list();

        List<EComAdminUserEvent> eComAdminUserEvents = new LinkedList<EComAdminUserEvent>();
        Iterator<Object> iterator = resultList.iterator();
        while(iterator.hasNext()){
            Object[] row = (Object[]) iterator.next();
            eComAdminUserEvent = new EComAdminUserEvent();
            user = new EComAdminUser();
            eComAdminUserEvent.setId(row[0] == null ? null :  (Long)row[0]);
            eComAdminUserEvent.setToken(row[1] == null ? null : row[1].toString());
            user.setUsername(row[2] == null ? null : row[2].toString());
            user.setFirstName(row[3] == null ? null : row[3].toString());
            user.setLastName(row[4] == null ? null : row[4].toString());
            user.setActive(row[5] == null ? false : (Boolean) row[5]);
            eComAdminUserEvent.setUser(user);
            eComAdminUserEvents.add(eComAdminUserEvent);
        }
        if (eComAdminUserEvents.size() > 0) {
            eComAdminUserEvent = (EComAdminUserEvent) eComAdminUserEvents.get(0);
        }
        return eComAdminUserEvent;
    }

    public void enableDisableUserAccess(List<Long> userAccessIds, boolean isEnable) {
        Boolean enableDisable = Boolean.FALSE;
        if (isEnable) {
            enableDisable = Boolean.TRUE;
        }
        Session session = currentSession();
        session.createQuery("Update UserAccess useraccess " +
                                "Set useraccess.isActive = :isEnabled , useraccess.modifiedDate = :modifiedDate " +
                                "where useraccess.id IN :userAccessIds")
                                .setParameter("isEnabled", enableDisable)
                                .setParameterList("userAccessIds", userAccessIds)
                                .setParameter("modifiedDate", new Date())
                                .executeUpdate();
    }

    public void enableDisableUserAccess(Long userAccessId, boolean isEnable) {
        List<Long> userAccessIds = new LinkedList<Long>();
        userAccessIds.add(userAccessId);
        this.enableDisableUserAccess(userAccessIds, isEnable);
    }

    @Override
    public EComAdminUser getUser(String username) {
        EComAdminUser user = null;
        Session session = currentSession();
        List<EComAdminUser> users = session.createQuery(LOAD_USER_BY_USERNAME)
            .setParameter("isActive", Boolean.TRUE)
            .setParameter("isAccountNonExpired", Boolean.TRUE)
            .setParameter("isCredentialsNonExpired", Boolean.TRUE)
            .setParameter("isAccountNonLocked", Boolean.TRUE)
            .setParameter("username", username )
            .list();
        if (!users.isEmpty()) {
            user = (EComAdminUser) users.get(0);
        }
        return user;
    }

    @Override
    public void updateLastLoginTime(String userName) {
        Session session = currentSession();
        session.createQuery("Update EComAdminUser  user " +
                "Set user.lastLoginTime = :lastLoginTime " +
                "where user.username = :username ")
                .setParameter("lastLoginTime", new Date())
                .setParameter("username", userName)
                .executeUpdate();
    }

    public void lockUnLockUser(String userName, boolean isLock, String modifiedBy){
        Boolean lockUnlock = Boolean.TRUE;
        if (isLock) {
            lockUnlock = Boolean.FALSE;
        }
        Session session = currentSession();
        session.createQuery("Update EComAdminUser  user " +
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

    public void deleteExistingUserAccesses(Long userId) {
        Session session = currentSession();
        session.createQuery("Delete EComAdminUserAccess  eComAdminUserAccess " +
                            " Where eComAdminUserAccess.user.id = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
    }

    public void updatePassword(String userName, String encodedPassword) {
        Session session = currentSession();
        session.createQuery("Update EComAdminUser user " +
                                "Set user.password = :encodedPassword, " +
                                "user.modifiedDate = :modifiedDate " +
                                "Where user.username = :userName ) ")
                                .setParameter("encodedPassword", encodedPassword)
                                .setParameter("modifiedDate", new Date())
                                .setParameter("userName", userName)
                                .executeUpdate();

    }

	public int archiveAdminUser(String adminUserName) {
        Session session = currentSession();
        Query query = session.getNamedQuery("ARCHIVE_ADMIN_USER")
                             .setParameter("userName", adminUserName);
        List<Object> resultList = query.list();
        int recordsUpdated = 0;
        if(resultList.size() > 0) {
            return recordsUpdated = resultList.size();
        }
        return recordsUpdated;
    }

	public void updateModifiedDateOfEComAdminUserEvent(Long userEventId) {
		Session session = currentSession();
        session.createQuery("Update EComAdminUserEvent userEvent " +
                "Set userEvent.modifiedDate = :modifiedDate " +
                "where userEvent.id = :userEventId ")
                .setParameter("modifiedDate", new Date())
                .setParameter("userEventId", userEventId)
                .executeUpdate();

	}
}
