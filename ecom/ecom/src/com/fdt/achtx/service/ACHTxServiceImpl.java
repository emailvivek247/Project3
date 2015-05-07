package com.fdt.achtx.service;

import static com.fdt.common.SystemConstants.NOTIFY_ADMIN;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fdt.achtx.dao.ACHTxDAO;
import com.fdt.achtx.dto.ACHTxDTO;
import com.fdt.common.exception.SDLBusinessException;
import com.fdt.common.exception.SDLException;
import com.fdt.common.util.SystemUtil;
import com.fdt.ecom.entity.enums.PaymentType;

@Service("aCHTransacationService")
public class ACHTxServiceImpl implements ACHTxService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${actransactions.fidelity.url}")
    private String url = null;

    @Autowired
    private ACHTxDAO achDAO = null;

    @Transactional(readOnly = true)
    public ACHTxDTO getACHDetailsForTransfer(Long siteId, PaymentType paymentType,
        String createdBy, String machineName) throws SDLException {
    	ACHTxDTO achDTO = null;
    	try {
    		achDTO = this.achDAO.getACHDetailsForTransfer(siteId, paymentType, createdBy, machineName);
	    } catch (Exception exception) {
	        logger.error(NOTIFY_ADMIN, "Exception Occured in " + exception);
	        throw new SDLException("Error Occurred while doing ACH Transfer" + exception.getMessage());
	    }
        return achDTO;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public ACHTxDTO doACHTransfer(PaymentType paymentType, Long siteId, String machineIp,
            String createdBy) throws SDLException, SDLBusinessException  {
        ACHTxDTO achDTO = null;
        try {
            achDTO = this.achDAO.doACHTransfer(paymentType, siteId, machineIp, createdBy);
            /** DO ACH Only if the Transaction Amount is Greater Than ZERO **/
            if (achDTO.getTxAmount() > 0) {
                String achTxRefNumber = this.doFidelityACHTransfer(achDTO);
                achDTO.setTxRefNumber(achTxRefNumber);
                this.achDAO.updateCheckHistory(achDTO.getCheckNumber(), achTxRefNumber);
            } else {
                throw new SDLBusinessException("Balance is zero for the provided criteria");
            }
        } catch (SDLBusinessException sDLBusinessException) {
            logger.error(NOTIFY_ADMIN, "Exception Occured in " + sDLBusinessException);
            throw sDLBusinessException;
        } catch (Exception exception) {
            logger.error(NOTIFY_ADMIN, "Exception Occured in " + exception);
            throw new SDLException("Error Occurred while doing ACH Transfer" + exception.getMessage());
        }
        return achDTO;
    }

    private String doFidelityACHTransfer(ACHTxDTO achDTO) throws SDLBusinessException, Exception {
    	String achTxRefNumber = null;
        String amount = String.valueOf(achDTO.getTxAmount());
        String siteName = achDTO.getSiteName();
        String tranNum = achDTO.getCheckNumber();
        String routingNumber = achDTO.getAcctRoutingNo();
        String accountNumber = achDTO.getAcctNumber();

        if(StringUtils.isBlank(amount)){
        	throw new SDLBusinessException("xAmount Cannot Be Null/Empty");
		}
        if(StringUtils.isBlank(siteName)){
        	throw new SDLBusinessException("xName i.e, xName Cannot Be Null/Empty");
		}
        if(StringUtils.isBlank(tranNum)){
        	throw new SDLBusinessException("xTranNum i.e, checkNumber Cannot Be Null/Empty");
		}
        if(StringUtils.isBlank(routingNumber)){
        	throw new SDLBusinessException("xRouting Cannot Be Null/Empty");
		}
        if(StringUtils.isBlank(accountNumber)){
        	throw new SDLBusinessException("xAccount Cannot Be Null/Empty");
		}

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("xAmount", amount);
        vars.put("xName", siteName);
        vars.put("xTranNum", tranNum);
        vars.put("xRouting", routingNumber);
        vars.put("xAccount", accountNumber);
        RestTemplate restTemplate = new RestTemplate();
        logger.info("ACH Request String: " + url);
        String responseString = restTemplate.getForObject(url, String.class, vars);
		logger.info("ACH Response String: " + responseString);
		FidelityResponse fidelityResponse = this.constructFidelityResponse(SystemUtil.decodeURL(responseString));
		if(fidelityResponse.getxResult().equalsIgnoreCase("A")) {
			achTxRefNumber = fidelityResponse.getxRefNum();
		} else if (fidelityResponse.getxResult().equalsIgnoreCase("D")) {
			 throw new SDLBusinessException(fidelityResponse.getxError());
		} else if (fidelityResponse.getxResult().equalsIgnoreCase("E")) {
			 throw new SDLBusinessException(fidelityResponse.getxError());
		}
		return achTxRefNumber;
    }

   /** This method is used to build FidelityResponse object from the fidelityResponse String.
    * The method tokenizes the responseString with '&' as delimiter and populates each variable in FidelityResponse object
    * accordingly**/
   private FidelityResponse constructFidelityResponse(String responseString) {
		FidelityResponse fidelityResponse = null;
		StringTokenizer responseStringTokenizer = new StringTokenizer(responseString, "&");
		fidelityResponse = new FidelityResponse();
    	String keyValuePair = null;
		while (responseStringTokenizer.hasMoreTokens()) {
			keyValuePair = responseStringTokenizer.nextToken();
			if (!StringUtils.isBlank(keyValuePair)) {
				if (keyValuePair.contains("xResult")) {
					fidelityResponse.setxResult(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xStatus")) {
					fidelityResponse.setxStatus(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xError")) {
					fidelityResponse.setxError(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xAuthCode")) {
					fidelityResponse.setxAuthCode(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xRefNum")) {
					fidelityResponse.setxRefNum(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xToken")) {
					fidelityResponse.setxToken(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xBatch")) {
					fidelityResponse.setxBatch(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xAvsResult")) {
					fidelityResponse.setxAvsResult(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xAvsResultCode")) {
					fidelityResponse.setxAvsResultCode(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xCvvResult")) {
					fidelityResponse.setxCvvResult(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
				if (keyValuePair.contains("xCvvResultCode")) {
					fidelityResponse.setxCvvResultCode(StringUtils.substring(keyValuePair,
							StringUtils.indexOf(keyValuePair, "=") + 1));
				}
			}
		}
    	return fidelityResponse;
	}
}
