package com.fdt.security.dao;

import java.util.List;

import com.fdt.common.dto.PageRecordsDTO;
import com.fdt.security.dto.SearchCriteriaDTO;
import com.fdt.subscriptions.dto.SubscriptionDTO;

public interface UserAdminDAO {

	public PageRecordsDTO findUsers(SearchCriteriaDTO searchCriteria);

    public List<SubscriptionDTO> getUserInfoForAdmin(String userName, String siteName);

}
