package com.fdt.ecom.dao;

import java.util.List;
import java.util.Map;

import com.fdt.achtx.entity.CheckHistory;
import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.common.entity.ErrorCode;
import com.fdt.ecom.dto.UserCountDTO;
import com.fdt.ecom.entity.BankDetails;
import com.fdt.ecom.entity.Code;
import com.fdt.ecom.entity.Merchant;
import com.fdt.ecom.entity.NodeConfiguration;
import com.fdt.ecom.entity.ReceiptConfiguration;
import com.fdt.ecom.entity.Site;
import com.fdt.ecom.entity.SiteConfiguration;
import com.fdt.ecom.entity.Term;
import com.fdt.ecom.entity.UserTerm;
import com.fdt.ecom.entity.enums.PaymentType;
import com.fdt.security.entity.Access;

public interface EComDAO {

    public List<Site> getSitesForNode(String nodeName);

    public List<Access> getAccessesForSite(String siteId);

    public Site getSiteDetails(Long siteId);

    public List<Site> getSites();

    public NodeConfiguration getNodeConfiguration(String nodeName);

    public SiteConfiguration getSiteConfiguration(Long siteId);

    public void updateNodeConfiguration(NodeConfiguration nodeConfiguration);

    public void updateSiteConfiguration(SiteConfiguration siteConfiguration);

    public void saveUserTerm(List<UserTerm> userTerms);

    public void saveUserTerm(UserTerm userTerm);

    public void saveErrorCode(ErrorCode errorCode);

    public List<Code> getCodes(String codeCategory);

    public Map<String, Access> getAccessListWithNonRecurringFee(List<String> accessCodeList);

    public Site getSiteDetailsBySiteName(String siteName);

    public List<ReceiptConfiguration> getReceiptConfigurationForSiteAndPaymentType(String siteName, PaymentType paymentType);

    public List<Merchant> getMerchantDetailsBySite(Long siteId);

    public Term getTerm(String siteName);

    public boolean doUserAccountsExistForUser(String userName);

    public void deleteCreditCard(String userName);

    public List<UserCountDTO> getUserCountsForAllSite();

    public UserCountDTO getUserCountForSite(Long siteId);

    public List<UserCountDTO> getUserCountsBySubForASite(Long siteId);

    public List<UserCountDTO> getUserDistributionBySub(Long siteId, Long accessId);


    public PageRecordsDTO  lookupOTCTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, String productName, String productType, String invoiceId,
			int startFrom, int numberOfRecords) ;

    public PageRecordsDTO  lookupWebTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, String productId, String productName, String productType,
			String invoiceId, int startFrom, int numberOfRecords);

    public PageRecordsDTO  lookupRecurringTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, int startFrom, int numberOfRecords);

    public PageRecordsDTO  lookupPayAsUGoTx(String txRefNumber, String cardName, String cardNumber, String transactionStartDate,
    		String transactionEndDate, String siteName, String productId, String productName, String productType,
			int startFrom, int numberOfRecords);

    public Site getSiteAdminDetails(Long siteId);

    public List<CheckHistory> getCheckHistories(Long siteId, String fromDate, String toDate, String checkNum, Double checkAmt);

    public boolean doVoidCheck(Long checkNumber, String comments);

    public CheckHistory getCheckHistory(Long checkNumber);

    public void saveReceiptConfiguration(ReceiptConfiguration receiptConfiguration);

    public List<ReceiptConfiguration> getReceiptConfigurationsForSite(Long siteId);

    public ReceiptConfiguration getReceiptConfigurationDetail(Long receiptConfigurationId);

    public PageRecordsDTO getErrorLog(String fromDate, String toDate, String userName, int startFromRecord, int numberOfRecords);

    public BankDetails getBankDetailsBySite(Long siteId);

    public void deleteErrorLogContents(Long errorLogId);

    public PaymentType getPaymentTypeForTransaction(String txRefNumber);

}
