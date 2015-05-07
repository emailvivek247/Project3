$(function() {
    $("#btnFindUser").click(function() {
    	if ($(".formError").length > 0) {    		
    		return false;	
    	}
    $("#SEARCH_ERROR").children().remove();
    $("#SEARCH_SUCCESS").children().remove();
    frmErrors = $('div.formErrorContent');
    $('#search_processing').show();
    $('#btnFindUser').attr('disabled', 'disabled');
    if (frmErrors.length > 0) return false;
    $('#frmUserSearch').validationEngine('hide');
	var username = $("#search_username").val();
	var dataString = 'username=' + username;
	
	$.ajax({
      type: "POST",
      url: "findUser.admin",
      data: dataString,
      dataType: "json",
      success: function(data) {    	    
    	  $.each(data, function(i,item){
    		 var elementID = "#" + item.code;   
    		 if (item.code != "SEARCH_SUCCESS" && item.code!="SEARCH_ERROR") {
    			 $(elementID).validationEngine('showPrompt', item.description, 'fail', 'topRight', true); 
    		 } else if (item.code == "SEARCH_SUCCESS") {
    			 $(location).attr('href','viewUserDetails.admin?' + dataString);
    		 } else {
    			 $("#SEARCH_ERROR").children().remove();
    			 $("#SEARCH_SUCCESS").children().remove();
    			 $("#SEARCH_ERROR").append('<p id="' + elementID + 'msg">' + item.description + '</p>'); 
    		 }
    		 $('#search_processing').hide();
    		 $("#btnFindUser").removeAttr("disabled");
          });       
      },
      error:(function() { })
     });
    return false;
	});
});;