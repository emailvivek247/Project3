package com.fdt.recurtx.dto;

import com.fdt.common.dto.AbstractBaseDTO;
import com.fdt.ecom.entity.CreditUsageFee;
import com.fdt.subscriptions.dto.AccessDetailDTO;

public class UpgradeDowngradeDTO extends AbstractBaseDTO {

    private static final long serialVersionUID = 6605458336655448460L;

    /** The User Accessid for Current Subscription**/
    private Long currentUserAccessId = null;

    /** The Fee for Current Subscription**/
    private Double currentFee = 0.0d;

    /** The Fee for New Subscription**/
    private Double newFee = 0.0d;

    /** The Unused Balance for the Current Subscription**/
    private Double unUsedBalance = 0.0d;

    /** The New Balance which the User Has to Pay for the Upgrades/Downgrades **/
    private Double newBalance = 0.0d;

   /** The Downgrade Fee for donwgrades **/
    private Double downgradeFee = 0.0d;

    /** This indicates whether it is downgrade/Upgrade **/
    private boolean isDowngrade = false;

    private boolean isCharge = false;

    /** This variable Stores the Card Usage Fee **/
    private CreditUsageFee cardUsageFee = null;

    /** This variable Stores the Transaction Id **/
    private String transactionId = null;

    /** This variable Stores the User Account Detail **/
    private UserAccountDetailDTO existingUserAccountDetail = null;

    /** This variable Stores the New Access Details **/
    private AccessDetailDTO newAccessDetailDTO = null;

    /** This variable is used to check whether an Existing User Account Exists or Not **/
    private boolean isAcctExistForCurSub = true;

    /** This variable is used to identify whether the new subscription during the change subscription is a restricted one **/
    private boolean accessUnAuthorizedExceptionFlag = false;

    /** This variable is used store the secondary transaction (charge/refund) performed during the change of subscription.**/
    private String secondaryTxId = null;

    public String getSecondaryTxId() {
        return secondaryTxId;
    }

    public void setSecondaryTxId(String secondaryTxId) {
        this.secondaryTxId = secondaryTxId;
    }

    public Long getCurrentUserAccessId() {
        return currentUserAccessId;
    }

    public void setCurrentUserAccessId(Long currentUserAccessId) {
        this.currentUserAccessId = currentUserAccessId;
    }

    public Double getCurrentFee() {
        return currentFee;
    }

    public void setCurrentFee(Double currentFee) {
        this.currentFee = currentFee;
    }

    public Double getNewFee() {
        return newFee;
    }

    public void setNewFee(Double newFee) {
        this.newFee = newFee;
    }

    public Double getUnUsedBalance() {
        return unUsedBalance;
    }

    public void setUnUsedBalance(Double unUsedBalance) {
        this.unUsedBalance = unUsedBalance;
    }

    public Double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(Double newBalance) {
        this.newBalance = newBalance;
    }

    public Double getDowngradeFee() {
        return downgradeFee;
    }

    public void setDowngradeFee(Double downgradeFee) {
        this.downgradeFee = downgradeFee;
    }

    public boolean isDowngrade() {
        return isDowngrade;
    }

    public void setDowngrade(boolean isDowngrade) {
        this.isDowngrade = isDowngrade;
    }

    public CreditUsageFee getCardUsageFee() {
        return cardUsageFee;
    }

    public void setCardUsageFee(CreditUsageFee cardUsageFee) {
        this.cardUsageFee = cardUsageFee;
    }

    public UserAccountDetailDTO getExistingUserAccountDetail() {
        return existingUserAccountDetail;
    }

    public void setExistingUserAccountDetail(
            UserAccountDetailDTO existingUserAccountDetail) {
        this.existingUserAccountDetail = existingUserAccountDetail;
    }

    public AccessDetailDTO getNewAccessDetailDTO() {
        return newAccessDetailDTO;
    }

    public void setNewAccessDetailDTO(AccessDetailDTO newAccessDetailDTO) {
        this.newAccessDetailDTO = newAccessDetailDTO;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isAcctExistForCurSub() {
        return isAcctExistForCurSub;
    }

    public void setAcctExistForCurSub(boolean isAcctExistForCurSub) {
        this.isAcctExistForCurSub = isAcctExistForCurSub;
    }

    public boolean isAccessUnAuthorizedExceptionFlag() {
        return accessUnAuthorizedExceptionFlag;
    }

    public void setAccessUnAuthorizedExceptionFlag(
            boolean accessUnAuthorizedExceptionFlag) {
        this.accessUnAuthorizedExceptionFlag = accessUnAuthorizedExceptionFlag;
    }

    public boolean isCharge() {
        return isCharge;
    }

    public void setCharge(boolean isCharge) {
        this.isCharge = isCharge;
    }

    @Override
    public String toString() {
        return "UpgradeDowngradeDTO [currentUserAccessId="
                + currentUserAccessId + ", currentFee=" + currentFee
                + ", newFee=" + newFee + ", unUsedBalance=" + unUsedBalance
                + ", newBalance=" + newBalance + ", downgradeFee="
                + downgradeFee + ", isDowngrade=" + isDowngrade + ", isCharge="
                + isCharge + ", cardUsageFee=" + cardUsageFee
                + ", transactionId=" + transactionId
                + ", existingUserAccountDetail=" + existingUserAccountDetail
                + ", newAccessDetailDTO=" + newAccessDetailDTO
                + ", isAcctExistForCurSub=" + isAcctExistForCurSub
                + ", accessUnAuthorizedExceptionFlag="
                + accessUnAuthorizedExceptionFlag + ", secondaryTransId="
                + secondaryTxId + ", createdDate=" + createdDate
                + ", modifiedDate=" + modifiedDate + ", modifiedBy="
                + modifiedBy + ", active=" + active + ", createdBy="
                + createdBy + "]";
    }
}