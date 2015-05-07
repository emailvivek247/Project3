$(function() {
    $("#btnUpdateAcct").click(function() {
    	if ($(".formError").length > 0) {    		
    		return false;	
    	}
    $("#ERROR").children().remove();
    $("#SUCCESS").children().remove();
    frmErrors = $('div.formErrorContent');
    $('#processing').show();
    $('#btnUpdateAcct').attr('disabled', 'disabled');
    if (frmErrors.length > 0) return false;
    $('#ccinfo-form').validationEngine('hide');	
	var accountName = $("input#accountName").val();
	var accountNumber = $("input#number").val();
	var expMonthS = $("input#expMonthS").val();
	var expYear = $("input#expYear").val();
	var cvv = $("input#cvv").val();
	var addressLine1 = $("input#addressLine1").val();
	var addressLine2 = $("input#addressLine2").val();
	var city = $("input#city").val();
	var state = $("input#state").val();
	var zip = $("input#zip").val();
	var phoneNumber = $("input#phoneNumber").val();
	var username = $("#username").val();
	var dataString = 'accountName='+ accountName + '&number=' + accountNumber + '&expMonthS=' + expMonthS + '&expYear=' + expYear + '&cvv=' + cvv + '&addressLine1=' + addressLine1 + '&addressLine2=' + addressLine2 + '&city=' + city + '&state=' + state + '&zip=' + zip + '&phoneNumber=' + phoneNumber + '&username=' + username;	
	
	$.ajax({
      type: "POST",
      url: "updatecreditcard.admin",
      data: dataString,
      dataType: "json",
      success: function(data) {    	    
    	  $.each(data, function(i,item){
    		 var elementID = "#" + item.code;   
    		 if (item.code != "SUCCESS" && item.code!="ERROR") {
    			 $(elementID).validationEngine('showPrompt', item.description, 'fail', 'topRight', true); 
    		 } else if (item.code == "SUCCESS") {
    			 $("#ERROR").children().remove();
    			 $("#SUCCESS").children().remove();
    			 $("#SUCCESS").append('<p id="' + elementID + 'msg">' + item.description + '</p>'); 
    			 $("#btnUpdateAcct").hide();
    			 location.reload();
    		 } else {
    			 $("#ERROR").children().remove();
    			 $("#SUCCESS").children().remove();
    			 $("#ERROR").append('<p id="' + elementID + 'msg">' + item.description + '</p>'); 
    		 }
    		 $('#processing').hide();
    		 $("#btnUpdateAcct").removeAttr("disabled");
          });       
      },
      error:(function() { })
     });
    return false;
	});
});