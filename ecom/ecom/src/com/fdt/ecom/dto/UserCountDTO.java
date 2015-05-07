package com.fdt.ecom.dto;

import com.fdt.common.dto.AbstractBaseDTO;

public class UserCountDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = -6205698478462471244L;

    private int key = 0;

    private String description = null;

    private int userCount = 0;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	@Override
	public String toString() {
		return "UserCountDTO [key=" + key + ", description=" + description
				+ ", userCount=" + userCount + ", createdDate=" + createdDate
				+ ", modifiedDate=" + modifiedDate + ", modifiedBy="
				+ modifiedBy + ", active=" + active + ", createdBy="
				+ createdBy + "]";
	}
}