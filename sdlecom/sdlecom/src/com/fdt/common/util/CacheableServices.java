package com.fdt.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fdt.common.util.client.ServiceStub;
import com.fdt.ecom.entity.Location;


@Component
public class CacheableServices {
	
	@Autowired
	@Qualifier("serviceStubRS")
	private ServiceStub service = null;

	
	@Cacheable("findLocationByNameAndAccessName")
	public Location findLocationByNameAndAccessName(String locationName, String accessName){
		return this.service.getLocationByNameAndAccessName(locationName, accessName);
	}

	

}
