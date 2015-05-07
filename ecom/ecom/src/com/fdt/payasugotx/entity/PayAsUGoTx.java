package com.fdt.payasugotx.entity;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fdt.common.entity.Tx;
import com.fdt.security.entity.Access;

@Entity
@Table(name = "ECOMM_PAYASUGO_TX")
public class PayAsUGoTx extends Tx {

	private static final long serialVersionUID = -6789544754500627107L;

	@Column(name="SITE_ID")
    private Long siteId = null;

	@Column(name = "USER_ID", nullable = false)
	private Long userId = null;
	
    @Column(name = "IS_CERTIFIED", nullable = false)
    @Type(type="yes_no")
    protected boolean isCertified = false;

	@Column(name = "ACCESS_ID")
	private Long accessId = null;

    @Column(name = "PAGE_COUNT")
    private int pageCount;

    @Column(name = "ITEM_COUNT")
    private int itemCount;
    
    @Column(name="FIRM_ADMIN_USER_ACCESS_ID")
    private Long firmAdminUserAccessId;
	
    @Transient 
	private Access access = null;

	@Transient
	private List<PayAsUGoTxItem> payAsUGoTxItems = null;

	@Transient
	private String userName = null;

	@Transient
	private Long itemsPurchased = 0L;

	@Transient
	private Long itemsRefunded = 0L;
	
	@Transient
	private String userFirstName;

	@Transient
	private String userLastName;

	public Long getItemsPurchased() {
		return itemsPurchased;
	}

	public void setItemsPurchased(Long itemsPurchased) {
		this.itemsPurchased = itemsPurchased;
	}

	public Long getItemsRefunded() {
		return itemsRefunded;
	}

	public void setItemsRefunded(Long itemsRefunded) {
		this.itemsRefunded = itemsRefunded;
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public void setPayAsUGoTxItem(PayAsUGoTxItem payAsUGoTxItem) {
		if (this.payAsUGoTxItems == null) {
			this.payAsUGoTxItems =  new LinkedList<PayAsUGoTxItem>();
		}
		this.payAsUGoTxItems.add(payAsUGoTxItem);
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	public List<PayAsUGoTxItem> getPayAsUGoTxItems() {
		return payAsUGoTxItems;
	}

	public void setPayAsUGoTxItems(List<PayAsUGoTxItem> payAsUGoTxItems) {
		this.payAsUGoTxItems = payAsUGoTxItems;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isCertified() {
		return isCertified;
	}

	public void setCertified(boolean isCertified) {
		this.isCertified = isCertified;
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}
	
	

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public Long getFirmAdminUserAccessId() {
		return firmAdminUserAccessId;
	}

	public void setFirmAdminUserAccessId(Long firmAdminUserAccessId) {
		this.firmAdminUserAccessId = firmAdminUserAccessId;
	}


	public String getUserFirstName() {
		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}


	@Override
    public String toString() {
        return "PayAsUGoTransaction ["
        		+ "siteId=" + siteId  
        		+ ", userId=" + userId 
        		+ ", isCertified=" + isCertified 
        		+ ", access=" + access 
        		+ ", accessId=" + accessId 
        		+ ", payAsUGoTxItems=" + payAsUGoTxItems 
        		+ ", itemsPurchased=" + itemsPurchased 
        		+ ", itemsRefunded=" + itemsRefunded 
                + ", id=" + id 
                + ", pageCount=" + pageCount 
                + ", itemCount=" + itemCount 
                + ", userFirstName=" + userFirstName 
                + ", userLastName=" + userLastName 
                + ", firmAdminUserAccessId=" + firmAdminUserAccessId 
                + ", createdDate=" + createdDate 
                + ", modifiedDate=" + modifiedDate 
                + ", modifiedBy=" + modifiedBy 
                + ", createdBy=" + createdBy 
                + ", active=" + active 
                + "]";
    }


}