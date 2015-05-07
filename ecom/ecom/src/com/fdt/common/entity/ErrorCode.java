package com.fdt.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ERROR_LOG")
public class ErrorCode extends AbstractBaseEntity {

    private static final long serialVersionUID = -1962414193562262869L;

    @Column(name="ERR_CODE", nullable = false)
    private String code = null;

    @Column(name="ERR_DESCRIPTION")
    private String description = null;

    @Column(name="MODULE_NAME")
    private String moduleName = null;

    @Column(name="FUNCTION_NAME", nullable = false)
    private String functionName = null;

    @Column(name="USER_NAME")
    private String userName = null;

    @Column(name = "IS_USER_EXCEPTION", nullable = false)
    @Type(type="yes_no")
    private boolean isUserException = false;

    @Transient
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isUserException() {
		return isUserException;
	}

	public void setUserException(boolean isUserException) {
		this.isUserException = isUserException;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ErrorCode other = (ErrorCode) obj;
        if (code == null) {
            if (other.code != null)
                return false;
        } else if (!code.equals(other.code))
            return false;
        return true;
    }
}