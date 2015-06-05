package com.fdt.sdl.admin.ui.action.messagecenter;

import org.apache.struts.action.ActionForm;

public class MessageForm extends ActionForm {

	private static final long serialVersionUID = 5889685946696199944L;
	
	private String lastUpdatedDateTime;
	
    private String htmlMessageContent;

	public String getLastUpdatedDateTime() {
		return lastUpdatedDateTime;
	}

	public void setLastUpdatedDateTime(String lastUpdatedDateTime) {
		this.lastUpdatedDateTime = lastUpdatedDateTime;
	}

	public String getHtmlMessageContent() {
		return htmlMessageContent;
	}

	public void setHtmlMessageContent(String htmlMessageContent) {
		this.htmlMessageContent = htmlMessageContent;
	}
    
}