package com.fdt.ecom.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fdt.common.entity.AbstractBaseEntity;
import com.fdt.security.entity.User;

@Entity
@Table(name = "AUTH_USERS_TERMS")
public class UserTerm extends AbstractBaseEntity {

    private static final long serialVersionUID = 9015039589197863835L;

    public UserTerm() {
    }

    public UserTerm(User user, Term term) {
        this.user = user;
        this.term = term;
        this.setCompositePrimaryKey(new CompositePrimaryKey(user.getId(), term.getId()));
    }

    @Embeddable
    public static class CompositePrimaryKey implements Serializable{

        private static final long serialVersionUID = 1L;

        @Column(name="USER_ID", nullable = false)
        private Long userId;

        @Column(name="TERM_ID", nullable = false)
        private Long termId;

        public CompositePrimaryKey() {
        }

        public CompositePrimaryKey(Long userId, Long termId){
            this.userId=userId;
            this.termId=termId;
        }
    }

    private CompositePrimaryKey compositePrimaryKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", insertable = false, updatable = false, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TERM_ID", insertable = false, updatable = false, nullable = false)
    private Term term;


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public void setCompositePrimaryKey(CompositePrimaryKey compositePrimaryKey) {
        this.compositePrimaryKey = compositePrimaryKey;
    }

    public CompositePrimaryKey getCompositePrimaryKey() {
        return compositePrimaryKey;
    }

    @Override
    public String toString() {
        return "UserTerm [compositePrimaryKey=" + compositePrimaryKey
                + ", user=" + user + ", term=" + term + ", id=" + id
                + ", createdDate=" + createdDate + ", modifiedDate="
                + modifiedDate + ", modifiedBy=" + modifiedBy + ", createdBy="
                + createdBy + ", active=" + active + "]";
    }
}
