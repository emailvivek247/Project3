package com.fdt.sdl.styledesigner.util;

import javax.servlet.http.HttpServletRequest;

import net.javacoding.xsearch.config.DatasetConfiguration;

public class DeviceDetectorUtil {

	/** HTTP-USER agents for Tablets **/
	private static final String TABLET_VERIZON_SAMSUNG_GALAXY = "SCH-I800";

	private static final String TABLET_SAMSUNG_GALAXY = "GT-P1000";

	private static final String TABLET_IPAD = "iPad";

	/** HTTP-USER agents for Mobile **/
	private static final String MOBILE = "Mobile";

	private static final String MOBILE_IPHONE = "iPhone";

	private static final String MOBILE_BLACKBERRY = "BlackBerry";

	private static final String MOBILE_HTC = "HTC";

	private static final String MOBILE_HP = "HP";

	private static final String MOBILE_LG = "LG";

	private static final String MOBILE_MOTOROLA = "MOT";

	private static final String MOBILE_NOKIA = "Nokia";

	private static final String MOBILE_SAMSUNG = "SAMSUNG";

	private static final String MOBILE_SONYERICSSON = "SonyEricsson";

	private static final String MOBILE_GOOGLE_NEXUS = "Nexus";

	private static final String MOBILE_WINDOWS_PHONE = "Windows Phone";

	public static String identifyDevice(DatasetConfiguration indexConfiguration, HttpServletRequest request) {
		String user_agent = request.getHeader("user-agent");
		/** Check Whether the request comes from a Tablet **/
		if (user_agent != null && isRequestTablet(user_agent)) {
			return indexConfiguration.getTabletTemplateName();
		/** Check Whether the request comes from a Mobile **/			
		} else if (user_agent != null && isRequestMobile(user_agent)) {
			return indexConfiguration.getMobileTemplateName();
		} else {
		/** Else the Request Comes from a PC **/			
			return indexConfiguration.getDefaultTemplateName();
		}
	}

	private static boolean isRequestMobile(String user_agent) {
		if (user_agent.indexOf(MOBILE) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_IPHONE) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_BLACKBERRY) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_HTC) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_HP) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_LG) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_MOTOROLA) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_NOKIA) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_SAMSUNG) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_SONYERICSSON) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_GOOGLE_NEXUS) > 0)
			return true;
		else if (user_agent.indexOf(MOBILE_WINDOWS_PHONE) > 0)
			return true;
		else
			return false;
	}

	private static boolean isRequestTablet(String user_agent) {
		if (user_agent.indexOf(TABLET_VERIZON_SAMSUNG_GALAXY) > 0)
			return true;
		else if (user_agent.indexOf(TABLET_SAMSUNG_GALAXY) > 0)
			return true;
		else if (user_agent.indexOf(TABLET_IPAD) > 0)
			return true;
		else
			return false;
	}
}
