<!doctype html public "-//W3C//DTD HTML 4.0 Transitional//EN" >
<html>
	<head>
		<title>Fairfax County Circuit Court, CPAN Coversheet Application v2.0</title>
		<meta http-equiv="X-UA-Compatible" content="IE=8" />  
		<meta name="CODE_LANGUAGE" content="C#" />
		<meta name="vs_defaultClientScript" content="JavaScript" />
        <meta http-equiv="cache-control" content="max-age=0" />
        <meta http-equiv="cache-control" content="no-cache" />
        <meta http-equiv="expires" content="0" />
        <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
        <meta http-equiv="pragma" content="no-cache" />
		<style type="text/css">
            input.BUTTON { font-weight: bold; font-family: Courier;
                height: 21px;
            }
		</style>

<script language="JavaScript" type="text/javascript">
    // global flag to avoid duplicate error messages during paste operations
    var g_avoidDuplicateAlertsFromThisMethod = false;
    // ***************************************************************************
    // Browser Test
    // ***************************************************************************
    var G_isNetscape = false;

    if (navigator.appName.indexOf("Microsoft") != -1) {
        G_isNetscape = false;
    } else if (navigator.appName.indexOf("Netscape") != -1) {
        G_isNetscape = true;
    }


    function setWidth() {
        // set the width of the select list
        // the number of nbsp will change depending on the browser
        nbr = 85;
        if (G_isNetscape != null) { // Netscape
            nbr = 105;
        }
        document.write("<option>");
        for (j = 0; j < nbr; j++) {
            document.write("&nbsp;");
        }
        document.write("</option>");
    }
    // *******************************************************
    // A function insert the DEM field to the document
    // *******************************************************
    function insertDEMField() {
        if (!G_isNetscape) {
            document.write("<td>####-AA-##-A-##<br/><input type='text' name='DEMNumber' size=15 maxlength=15 onkeypress='return validate(event,\"####-CC-##-C-##\", this);' onpaste='validatePastedValue(\"####-CC-##-C-##\",this, true);' onblur='if(validateValue(\"####-CC-##-C-##\",this))makeUpperCase(this);'></td>");
        } else {
            document.write("<input type='hidden' name='DEMNumber'>");

            document.write("<td><Table><tr>");
            document.write("<td align='center'><FONT size=2>####</FONT></td>");
            document.write("<td align='center'><FONT size=2>AA</FONT></td>");
            document.write("<td align='center'><FONT size=2>##</FONT></td>");
            document.write("<td align='center'><FONT size=2>A</FONT></td>");
            document.write("<td align='center'><FONT size=2>##</FONT></td>");
            document.write("</tr><tr>");
            document.write("<td><input type='text' name='DEMNumber1' size=4 maxlength=4 onkeypress='return validate(event,\"####\",this)' onpaste='validatePastedValue(\"####\",this);' onblur='validateValue(\"####\",this);'></td>");
            document.write("<td><input type='text' name='DEMNumber2' size=2 maxlength=2 onkeypress='return validate(event,\"CC\",this)' onpaste='validatePastedValue(\"CC\",this, true);' onblur='if(validateValue(\"CC\",this))makeUpperCase(this);'></td>");
            document.write("<td><input type='text' name='DEMNumber3' size=2 maxlength=2 onkeypress='return validate(event,\"##\",this)' onpaste='validatePastedValue(\"##\",this);' onblur='validateValue(\"##\",this);'></td>");
            document.write("<td><input type='text' name='DEMNumber4' size=1 maxlength=1 onkeypress='return validate(event,\"C\",this)' onpaste='validatePastedValue(\"C\",this, true);' onblur='if(validateValue(\"C\",this))makeUpperCase(this);'></td>");
            document.write("<td><input type='text' name='DEMNumber5' size=2 maxlength=2 onkeypress='return validate(event,\"##\",this)' onpaste='validatePastedValue(\"##\",this);' onblur='validateValue(\"##\",this);'></td>");
            document.write("</tr></table></td>");
        }
    }

    // ***************************************************************************
    // This fuction allows users the ability to add Intruments and Action Codes to the
    // List box
    // ***************************************************************************
    function AddInst() {
        var str = document.forms[0].InstrumentTypeList.options[document.forms[0].InstrumentTypeList.selectedIndex].text;
        var cod = document.forms[0].InstrumentTypeList.options[document.forms[0].InstrumentTypeList.selectedIndex].value;

        if (str != "") {
            var n = document.forms[0].instList.length;
            var blank = document.forms[0].instList.options[n - 1].text;

            var NotFoundFlag = true;
            for (var i = 0; i < document.forms[0].instList.length - 1; i++) {
                if (document.forms[0].instList.options[i].text == str) {
                    NotFoundFlag = false;
                }
            }
            
            if (NotFoundFlag) {

                document.forms[0].instList.options[n - 1] = new Option(str, cod);
                document.forms[0].instList.options[n] = new Option(blank, blank);
                document.forms[0].instList.selectedIndex = n - 1;

                var temp = document.forms[0].FullInstList.value;
                document.forms[0].FullInstList.value = temp + "|" + str;

                document.forms[0].FullCodeList.value = document.forms[0].FullCodeList.value + "|" + cod;

                document.forms[0].InstrumentTypeList.selectedIndex = 0;
            } else {
                alert("'" + str + "' already exists in the list");
            }
        }
    }
    // ***************************************************************************
    // This fuction allows users the ability to Remove Intruments and Action Codes to the
    // List box
    // ***************************************************************************
    function RemoveInst() {
        var nbr = 0;
        var NotSelectedFlag = true;
        var SelectedRecord = "";
        var IndexCount = 0;
        // dont remove the last name
        for (var i = 0; i < document.forms[0].instList.length - 1; i++) {
            if (document.forms[0].instList.options[i].selected) {
                SelectedRecord = document.forms[0].instList.options[i].text;
                document.forms[0].instList.options[i] = null;
                NotSelectedFlag = false;
                nbr++;
            }
            IndexCount = IndexCount + 1
        }

        // Here is where we check to see what has happened based on the users
        // Remove request.

        if (IndexCount == 0) {
            alert("All Records have been removed");
        } else if (NotSelectedFlag) {
            alert("Please select an 'Instrument' to be removed");
        } else {
            alert("Record '" + SelectedRecord + "' has been removed");
        }

        var str = "";
        var code = "";
        for (var i = 0; i < document.forms[0].instList.length - 1; i++) {
            str += "|" + document.forms[0].instList.options[i].text;
            code += "|" + document.forms[0].instList.options[i].value;
        }
        document.forms[0].FullInstList.value = str;
        document.forms[0].FullCodeList.value = code;
    }
    // ***************************************************************************
    // This fuction allows users the ability to Change Intruments
    // ***************************************************************************
    function ChangeInst() {
        var nbr = 0;
        var FoundFlag = false;
        var IndexCount = 0;
        for (var i = 0; i < document.forms[0].instList.length - 1; i++) {
            if (document.forms[0].instList.options[i].selected) {
                FoundFlag = true;
                nbr++;
            }
            IndexCount = IndexCount + 1;
        }

        if (FoundFlag) {

            var pos = document.forms[0].instList.selectedIndex;
            if (pos == document.forms[0].instList.length - 1) return;
            var str = document.forms[0].instList.options[pos].text;
            if (str == "") return;

            var a = str.split("\t");
            for (var i = 0; i < document.forms[0].InstrumentTypeList.length; i++) {
                if (a[0] == document.forms[0].InstrumentTypeList.options[i].text) {
                    document.forms[0].InstrumentTypeList.selectedIndex = i;
                    break;
                }
            }

            document.forms[0].instList.options[pos] = null;
            document.forms[0].InstrumentTypeList.focus();
        } else if (IndexCount > 0) {
            alert("Please select an 'Instrument' to change");
        } else {
            alert("No Instruments in the List to change");
        }
        var str = "";
        var code = "";
        for (var i = 0; i < document.forms[0].instList.length - 1; i++) {
            str += "|" + document.forms[0].instList.options[i].text;
            code += "|" + document.forms[0].instList.options[i].value;
        }
        document.forms[0].FullInstList.value = str;
        document.forms[0].FullCodeList.value = code;
    }
    // ***************************************************************************
    // clears names and instrument types
    // ***************************************************************************
    function finishClear() {
        for (var i = document.forms[0].instList.length - 2; i > -1; i--) {
            document.forms[0].instList.options[i] = null;
        }
        for (var i = document.forms[0].NameList.length - 2; i > -1; i--) {
            document.forms[0].NameList.options[i] = null;
        }
        for (var i = document.forms[0].TMNumberList.length - 2; i > -1; i--) {
            document.forms[0].TMNumberList.options[i] = null;
        }
        document.forms[0].TMType[0].checked = true;
        onTMTypeChange();
    }
	function setDefaultValue(txtObj){
		if (trim(txtObj.value) == "")
		txtObj.value = "0";
	}
	function makeUpperCase(obj) {
        obj.value = obj.value.toUpperCase();
    }
            

    // ***************************************************************************
    // Validations
    // ***************************************************************************

    // ***************************************************************************
    // OnKey Press Validations:
    // Checks to make sure no special characters are typed - added OCM 12/19/2000
    // ***************************************************************************
    function checkSpecChar(e, obj) {
        var charCode = (navigator.appName == "Netscape") ? e.which : e.keyCode;
        status = charCode;
        // Only allow upper case
        if (isLowerAlpha(charCode)) {
            //setCharCode (e, charCode-32);
            obj.value += String.fromCharCode(charCode - 32).toUpperCase();
            return false;
        }
        // Only allow special characters comma, ampersand, and at symbol. All others are ignored
        if ((charCode > 32 && charCode < 48) || (charCode > 57 && charCode < 64) || (charCode > 90 && charCode < 97) || (charCode > 122)) {
            if (charCode == 38 || charCode == 44) {
                return true;
            }
            else {
                return false;
            }
        }
        return true;
    }
    // ***************************************************************************
    // Onblur Validations:
    // ***************************************************************************
    function hasNoSpecialChars(obj) {
        for (k = 0; k < obj.value.length; k++) {
            var charCode = obj.value.charCodeAt(k);
            if ((charCode > 32 && charCode < 48) || (charCode > 57 && charCode < 64) || (charCode > 90 && charCode < 97) || (charCode > 122)) {
                if (charCode == 38 || charCode == 44) {
                    continue;
                }
                else {
                    obj.focus();
                    obj.select();
                    return false;
                }
            }
        }
        return true;
    }
    function validateValue(fmt, obj) { 
        // Most elements ont his page use both onblur and onpaste which inturn call this method
        // Furhter since this method shows alerts, it results in a onblur event on this obj and will cause this same method to be called
        // resulting in duplicate alerts (annoying to user) so:
        if(g_avoidDuplicateAlertsFromThisMethod)
        {
            g_avoidDuplicateAlertsFromThisMethod = false;
            return true;       
        }
	    var current = obj.value
	    var len     = current.length

        for(index=0; index<fmt.length; index++){
            if(current.length <= index)return true;
                        
		    var pchar   = fmt.charAt (index);
            var charCode= current.charCodeAt(index);
		    if (charCode == fmt.charCodeAt (index) && formatStr.indexOf (pchar,0) < 0) return true;
		    if (pchar == '~'){
                return false;
            }

		    if (!G_isNetscape) { // doesn't work with netscape
			    while (index < fmt.length && formatStr.indexOf (pchar,0) < 0) {
				    obj.value += pchar
				    pchar = fmt.charAt (++index);
			    }
			    current = obj.value
		    }
            
		    // greater than control chars
		    if (charCode > 31 && index < fmt.length) {
			    if (pchar == 'n' || pchar == 'N') {
				    if (!isNumeric (charCode)) {
					    g_avoidDuplicateAlertsFromThisMethod = true;
                        alert("Only a numeric character may be entered here.");
                        obj.focus();
                        obj.select();
                        return false;
				    }
			    } else if (pchar == '9') {
			    if ((charCode != 32) && (!isDigit (charCode))) {
                        g_avoidDuplicateAlertsFromThisMethod = true;
					    alert("Only a number or a space may be entered here.");
                        obj.focus();
                        obj.select();
                        return false;
				    }
			    } else if (pchar == '#') {
			    if (!isDigit (charCode)) {
                        g_avoidDuplicateAlertsFromThisMethod = true;
					    alert("Only a number may be entered here.");
                        obj.focus();
                        obj.select();
                        return false;
				    }
			    } else if (pchar == 'A' || pchar == 'a') {
				    if (!isAlphaNumeric (charCode)) {
					    g_avoidDuplicateAlertsFromThisMethod = true;
                        alert("Only alphanumeric may be entered here.");
                        obj.focus();
                        obj.select();
                        return false;
				    }
			    } else if (pchar == 'C' || pchar == 'c') {
				    if (!isAlpha (charCode)) {
					    g_avoidDuplicateAlertsFromThisMethod = true;
                        alert("Only characters may be entered here.");
                        obj.focus();
                        obj.select();
                        return false;
				    }
			    }else if(pchar == '!'){
                    continue;
                }else if (charCode != pchar.charCodeAt(0)) {
				    g_avoidDuplicateAlertsFromThisMethod = true;
                    alert("Only a "+pchar+" may be entered here.");
                    obj.focus();
                    obj.select();
                    return false;
			    }
		    }
        }
	    return true;
    }
	// ***************************************************************************
	// Adds a name and associated flags to the NameList
	// ***************************************************************************
	function AddName() {
	    var str = document.forms[0].Names.value;

	    if (str != "") {
   		    while (str.match("  ") ) {
	 		    str = str.replace("  ", " ");
		    }
		    str += "\t"+document.forms[0].Name_Type.options[document.forms[0].Name_Type.selectedIndex].text;
		    if (document.forms[0].Trustee.checked) {
		    str += "\t"+"Trustee";
		    } else {
			    str += "\t"+"Non-Trustee";
		    }
		    if (document.forms[0].Firm.checked) {
			    str += "\t"+"Firm";
		    } else {
			    str += "\t"+"Individual";
		    }

		    //alert(str);
		    var n = document.forms[0].NameList.length;
		    var blank = document.forms[0].NameList.options[n-1].text;

		    var NotFoundFlag = true;
		    var nbr = 0;
		    for (var i=0; i<document.forms[0].NameList.length-1; i++) {
			    if (document.forms[0].NameList.options[i].value == str) {
			    NotFoundFlag = false;
			    var FoundText = document.forms[0].NameList.options[i].value;
			    nbr++;
			    }
		    }

		    if (NotFoundFlag)
		    {
			    document.forms[0].NameList.options[n-1] = new Option(str, str);
			    document.forms[0].NameList.options[n]   = new Option(blank, blank);
			    document.forms[0].NameList.selectedIndex = n-1;

	    //         document.forms[0].Name_Type.selectedIndex=0;
		    } else {
		    alert("'"+FoundText+"' already exists in the list");
		    }
	    } else {
		    alert("Before you can use the [Add Name] button you must enter a name - Please enter a Name");
	    }
	    document.forms[0].Names.focus();
	    document.forms[0].Names.select();
	}

	// ***************************************************************************
	// Removes a name and associated flags to the NameList
	// ***************************************************************************
	function RemoveName () {
		var IndexCount = document.forms[0].NameList.length-1;
		var pos = document.forms[0].NameList.selectedIndex;

		if (IndexCount == 0) {
			alert ("All Records have been removed");
		} else if (pos >= 0 && pos < IndexCount){
			var txt = document.forms[0].NameList.options[pos].value;
			document.forms[0].NameList.options[pos] = null;
			alert ("Record '"+txt+"' has been removed");
		} else {
			alert ("Please select a 'Name' to be removed");
		}
	}
	// ***************************************************************************
	// Allows a name and associated flags to be changed
	// ***************************************************************************
	function ChangeName () {
		var IndexCount = document.forms[0].NameList.length-1;

		if (document.forms[0].NameList.selectedIndex >= 0){

			var pos = document.forms[0].NameList.selectedIndex;
			if (pos == IndexCount) return;
			var str = document.forms[0].NameList.options[pos].text;
			if (str=="") return;

			var a = str.split("\t");

			document.forms[0].Names.value = a[0];

			for (var i=0; i<document.forms[0].Name_Type.length; i++) {
				if (a[1] == document.forms[0].Name_Type.options[i].text) {
					document.forms[0].Name_Type.selectedIndex = i;
					break;
				}
			}
			if (a[2] == "Trustee"){
				document.forms[0].Trustee.checked = true;
			} else {
				document.forms[0].Trustee.checked = false;
			}
			if (a[3] == "Firm"){
				document.forms[0].Firm.checked = true;
			} else {
				document.forms[0].Firm.checked = false;
			}

			document.forms[0].NameList.options[pos]=null;
			document.forms[0].Names.focus();
		} else if (IndexCount > 0) {
				alert("Please select a 'Name' to change");
		} else {
				alert("No Names in the List to change");
		}
	}

	function addChar(val,len,chr){
		while (val.length < len){
			val = chr+val;
		}
		return val;
	}

	function resetTaxMap(){
		if (document.forms[0].TMType[1].checked)
		{
			document.forms[0].TMNumB1.value = "";
			document.forms[0].TMNumB2.value = "";
			document.forms[0].TMNumB3.value = "";
			document.forms[0].TMNumB4.value = "";
			document.forms[0].TMNumB5.value = "";
			document.forms[0].TMNumB6.value = "";
			document.forms[0].TMNumB1.focus();
		}
		else
		{
			document.forms[0].TMNumA1.value = "";
			document.forms[0].TMNumA2.value = "";
			document.forms[0].TMNumA4.value = "";
			document.forms[0].TMNumA5.value = "";
			document.forms[0].TMNumA6.value = "";
			document.forms[0].TMNumA7.value = "";
			document.forms[0].TMNumA1.focus();
		}
	}

	function setTaxMap(val){
		if (document.forms[0].TMType[1].checked){
			document.forms[0].TMNumB1.value = val.substring(0,2);
			document.forms[0].TMNumB2.value = val.substring(3,4);
			document.forms[0].TMNumB3.value = val.substring(5,7);
			document.forms[0].TMNumB4.value = val.substring(8,10);
            document.forms[0].TMNumB5.value = val.substring(11,14);
            document.forms[0].TMNumB6.value = val.substring(14,16);
			document.forms[0].TMNumB1.focus();
		}
		else{
			document.forms[0].TMNumA1.value = val.substring(0,3);
			document.forms[0].TMNumA2.value = val.substring(4,5);
		    document.forms[0].TMNumA4.value = val.substring(8,10);
			document.forms[0].TMNumA5.value = val.substring(11,13);
			document.forms[0].TMNumA6.value = val.substring(14,18);
			document.forms[0].TMNumA7.value = val.substring(19,21);
			document.forms[0].TMNumA1.focus();
		}
	}
	// ***************************************************************************
	// Adds a tax map number and associated flag to the TaxMapNumberList
	// ***************************************************************************
	function AddTaxMap(){
		var str = "";
		if (document.forms[0].TMType[0].checked){
			if (document.forms[0].TMNumA1.value.length != 3 || document.forms[0].TMNumA2.value.length != 1 
				|| document.forms[0].TMNumA4.value.length != 2 || document.forms[0].TMNumA6.value.length != 4){
                alert("Please enter characters 1-3, 4, 6-7 and 10-13, these are mandatory.");
                if (document.forms[0].TMNumA1.value.length != 3)
                    document.forms[0].TMNumA1.focus();
                else if (document.forms[0].TMNumA2.value.length != 1)
                    document.forms[0].TMNumA2.focus();
                else if (document.forms[0].TMNumA4.value.length != 2)
                    document.forms[0].TMNumA4.focus();
                else if (document.forms[0].TMNumA6.value.length != 4)
                    document.forms[0].TMNumA6.focus();
  			        return;
  		        }
                document.forms[0].TMNumA5.value = addChar(document.forms[0].TMNumA5.value,2," ");
                document.forms[0].TMNumA7.value = addChar(document.forms[0].TMNumA7.value,2," ");
		        str = document.forms[0].TMNumA1.value+"-"+document.forms[0].TMNumA2.value+"-"+/*document.TMNumA3.value*/" "+"-"
			        +document.forms[0].TMNumA4.value+"-"+document.forms[0].TMNumA5.value+"-"+document.forms[0].TMNumA6.value+"-"+document.forms[0].TMNumA7.value;
		}
		if (document.forms[0].TMType[1].checked){
            if (document.forms[0].TMNumB1.value.length != 2 || document.forms[0].TMNumB2.value.length != 1 
                || document.forms[0].TMNumB3.value.length != 2 || document.forms[0].TMNumB5.value.length != 3){
            alert("Please enter characters 1-2, 3, 4-5 and 8-10, these are mandatory.");
            if (document.forms[0].TMNumB1.value.length != 2)
                document.forms[0].TMNumB1.focus();
            else if (document.forms[0].TMNumB2.value.length != 1)
                document.forms[0].TMNumB2.focus();
            else if (document.forms[0].TMNumB3.value.length != 2)
                document.forms[0].TMNumB3.focus();
            else if (document.forms[0].TMNumB5.value.length != 3)
                document.forms[0].TMNumB5.focus();
            return;
            }
            if (document.forms[0].TMNumB4.value.length == 1)
            if (isNaN(document.forms[0].TMNumB4.value))
                document.forms[0].TMNumB4.value = addChar(document.forms[0].TMNumB4.value,2," ");
            else 
                document.forms[0].TMNumB4.value = addChar(document.forms[0].TMNumB4.value,2,"0");
            else
            document.forms[0].TMNumB4.value = addChar(document.forms[0].TMNumB4.value,2," ");
      
            document.forms[0].TMNumB6.value = addChar(document.forms[0].TMNumB6.value,2," ");
            str = document.forms[0].TMNumB1.value+" "+document.forms[0].TMNumB2.value+" "+document.forms[0].TMNumB3.value+" "
                +document.forms[0].TMNumB4.value+" "+document.forms[0].TMNumB5.value+document.forms[0].TMNumB6.value;
  	    }
			
		if (str != "") 
		{
			str += "\t"+(document.forms[0].TMType[0].checked ? "County":"City");
			var n = document.forms[0].TMNumberList.length;
			var blank = document.forms[0].TMNumberList.options[n-1].text;
			var NotFoundFlag = true;
			var nbr = 0;
			for (var i=0; i<document.forms[0].TMNumberList.length-1; i++){
				if (document.forms[0].TMNumberList.options[i].value == str) 
				{
				NotFoundFlag = false;
				var FoundText = document.forms[0].TMNumberList.options[i].value;
				nbr++;
				}
			}

			if (NotFoundFlag){
				document.forms[0].TMNumberList.options[n-1] = new Option(str, str);
				document.forms[0].TMNumberList.options[n]   = new Option(blank, blank);
				document.forms[0].TMNumberList.selectedIndex = n-1;
			} 
			else{
			    alert("'"+FoundText.replace("\t"," ")+"' already exists in the list");
			    return;
			}
		} 
		else{
			alert("Before you can use the [Add Tax Map Number] button you must enter a tax map number - Please enter a number");
			return;
		}
		resetTaxMap();
	}
	// ***************************************************************************
	// Removes a tax map number and associated flag to the TMNumberList
	// ***************************************************************************

	function RemoveTaxMap() 
	{
	    var IndexCount = document.forms[0].TMNumberList.length-1;
	    var pos = document.forms[0].TMNumberList.selectedIndex;

	    if (IndexCount == 0){
		    alert ("All Records have been removed");
	    } 
	    else if (pos >= 0 && pos < IndexCount){
		    var txt = document.forms[0].TMNumberList.options[pos].value;
		    document.forms[0].TMNumberList.options[pos] = null;
		    alert ("Record '"+txt.replace("\t"," ")+"' has been removed");
	    } 
	    else{
		    alert ("Please select a 'Tax Map Number' to be removed");
	    }
	}
	// ***************************************************************************
	// Allows a tax map and associated flag to be changed
	// ***************************************************************************
    function ChangeTaxMap(){
		var IndexCount = document.forms[0].TMNumberList.length-1;

		if (document.forms[0].TMNumberList.selectedIndex >= 0){
			var pos = document.forms[0].TMNumberList.selectedIndex;
			if (pos == IndexCount) return;
			var str = document.forms[0].TMNumberList.options[pos].text;
			if (str=="") return;

			var a = str.split("\t");

			if (a[1] == "County")
				document.forms[0].TMType[0].checked = true;
			else
				document.forms[0].TMType[1].checked = true;
		        
			onTMTypeChange();

			setTaxMap(a[0]);

			document.forms[0].TMNumberList.options[pos]=null;
			// document.forms[0].TMNumber.focus();
		} else if (IndexCount > 0) {
				alert("Please select a 'Tax Map Number' to change");
		} else {
				alert("No Names in the List to change");
		}
	}
	// *******************************************************
	// Change form display based on city/county flag
	// *******************************************************
	function onTMTypeChange(){
		var TMType = (document.forms[0].TMType[0].checked ? "County":"City");
			
		if (TMType == "County"){
			TaxMapCounty.style.display = "block";
			TaxMapCity.style.display = "none";
		}
		else{
			TaxMapCounty.style.display = "none";
			TaxMapCity.style.display = "block";
		}
		//  document.forms[0].TMNumber.value = "";
		resetTaxMap();
	}

	//+
	// browser dependent code follows
	//-

	// *******************************************************
	// Update the Tax Map fields when the City/County radio button changes
	// *******************************************************


	// *******************************************************
	// Netscape function to skip a disabled data field
	// *******************************************************
	function skip () { this.blur(); }

	// *******************************************************
	// A function disable a data field
	// *******************************************************
	function disableTextField (field) {
		if (!G_isNetscape) {  //ie
			//field.disabled = true;
            field.readOnly = true;
		} else {                                       //nn
			field.oldOnFocus = field.onfocus;
			field.onfocus = skip;
		}
	}

	// *******************************************************
	// A function enable a data field
	// *******************************************************
	function enableTextField (field) {
		if (!G_isNetscape) {
			//field.disabled = false;
            field.readOnly = false;
		} else {
			field.onfocus = field.oldOnFocus;
		}
	}


	// *******************************************************
	// A function insert the TaxMap field to the document
	// *******************************************************



	// *******************************************************
	// A function to validate Netscape fields
	// kluge because NS has problems with field events in a table (???)
	// *******************************************************
	function validateNS (evt) {
	    var obj = evt.target;

	    if (obj == document.forms[0].Consideration) {
		    return validate (evt, "NNNNNNNNNNNN",obj);
	    } else if (obj == document.forms[0].ConsiderationPercentage) {
		    return validate (evt, "NNNNN",obj);
	    } else if (obj == document.forms[0].AmountNotTaxed) {
		    return validate (evt, "NNNNNNNNNNNN",obj);
	    } else if (obj == document.forms[0].OriginalBook) {
		    return validate (evt, "#####",obj);
	    } else if (obj == document.forms[0].OriginalPage) {
		    return validate (evt, "####",obj);
	    } else if (obj == document.forms[0].TitleCase) {
		    return validate (evt, "!!!!!!!!!!",obj);
	    } else if (obj == document.forms[0].DEMNumber1) {
		    return validate (evt, "####",obj);
	    } else if (obj == document.forms[0].DEMNumber2) {
		    return validate (evt, "CC",obj);
	    } else if (obj == document.forms[0].DEMNumber3) {
		    return validate (evt, "##",obj);
	    } else if (obj == document.forms[0].DEMNumber4) {
		    return validate (evt, "C",obj);
	    } else if (obj == document.forms[0].DEMNumber5) {
		    return validate (evt, "##",obj);
	    }
	}
	// *******************************************************
	// A function to uppercase Netscape fields
	// We can't change the case of the field as the data is enter
	// so we need to wait until we get the change event.
	// *******************************************************
    function upperCaseNS (evt) {
		var obj = evt.target;
		obj.value = obj.value.toUpperCase ();
		return true;
	}
	// *******************************************************
	// A function to validate the TaxMap field
	// The format is dependent upon the City/County radio buttons
	// *******************************************************
	function validateTaxMap (e, obj) {
		if (document.forms[0].TMType[0].checked){
			return validate(e, "###-#-A-##-AA-####-AA~", obj);
		} 
		else{
			return validate(e, "##-#-##-AA-###-AA~", obj);
		}
	}
	// *******************************************************
	// A function to SkipDeactivatedFields
	// This fuction was created because Netscape does not currently support
	// the DEACTIVATE function Like IE does.
	// *******************************************************

	// *******************************************************
	// A function to return the character code of the key entered
	// *******************************************************
    function getCharCode (e) {
		return (G_isNetscape) ? e.which : e.keyCode;
	}

	// *******************************************************
	// A function to set the character code of the event
	// *******************************************************
	function setCharCode (e, charCode) {
		if (G_isNetscape) {
			e.which = charCode;
		} else {
			e.keyCode = charCode;
		}
	}
	// *******************************************************
	// A function to right align a string in a text field
	// *******************************************************
	function rightStringAlign (field, size, pad) {
	    size     = field.size ? field.size : size ? size : 20;
	    padChar  = pad ? pad : ' ';
	    var v = field.value;
	    while (v.length < size) {
		    v = padChar + v;
	    }
	    field.value = v;
	}

	function isDigit (kode) {
	    return ((kode >= 48) && (kode <= 57));
	}

	//digits plus '.', ",", "-" and "+"

	function isNumeric (kode) {
	    return ((kode >= 43 && kode <= 46) || isDigit (kode));
	}

	function isUpperAlpha (kode) {
	    return (kode >= 65  && kode <= 90)
	}

	function isLowerAlpha (kode) {
	    return (kode >= 97 && kode <= 122);
	}

	function isAlpha (kode) {
	    return ((kode == 32) || isUpperAlpha (kode) || isLowerAlpha (kode));
	}

	function isAlphaNumeric (kode) {
	    return isAlpha (kode) || isDigit (kode);
	}

	function checkBounds (obj,lower, upper) {
		var val = obj.value;

		while (val.indexOf(',', 0) >= 0) {
			val = val.replace(',', '');
		}
		val = 1*val;
		if (val < lower || val > upper) {
			obj.value = "";
			alert ("Error "+obj.name+" must be between "+lower+" and "+upper);
		}
	}
	// valid format characters are
	// ! - uppercase (any ascii forced to uppercase)
	// a - alpha numeric (a-z + 0-9)
	// A - alpha numeric (uppercase alpha numeric)
	// N - numeric (number or one of "+-.,"
	// # - number only (0-9)
	// c - alpha character (a-z)
	// C - uppercase alpha character
	// * - any number of characters
	// 9 - any number or a space

	var formatStr = "9#!AaNCc";

    // only allows appropriate characters to be entered
	function validate (e, fmt, obj) {
	    var charCode= getCharCode (e);
	    var current = obj.value
	    var len     = current.length
	    var pchar   = fmt.charAt (len);

	    if (charCode == fmt.charCodeAt (len) && formatStr.indexOf (pchar,0) < 0) return true;
	    if (pchar == '~') return false;

	    if (!G_isNetscape) { // doesn't work with netscape
		    while (len < fmt.length && formatStr.indexOf (pchar,0) < 0) {
			    obj.value += pchar
			    pchar = fmt.charAt (++len);
		    }
		    current = obj.value
	    }
	    // greater than control chars
	    if (charCode > 31 && len < fmt.length) {
		    if (pchar == 'n' || pchar == 'N') {
			    if (!isNumeric (charCode)) {
				    alert("Only a numeric character may be entered here.");
				    return false;
			    }
		    } else if (pchar == '9') {
		    if ((charCode != 32) && (!isDigit (charCode))) {
				    alert("Only a number or a space may be entered here.");
				    return false;
			    }
		    } else if (pchar == '#') {
		    if (!isDigit (charCode)) {
				    alert("Only a number may be entered here.");
				    return false;
			    }
		    } else if (pchar == '!') {
			    if (isLowerAlpha (charCode)) {
				    //setCharCode (e, charCode-32);
                    obj.value += String.fromCharCode(charCode-32).toUpperCase();
                    return false;
			    }
		    } else if (pchar == 'A' || pchar == 'a') {
			    if (!isAlphaNumeric (charCode)) {
				    alert("Only alphanumeric may be entered here.");
				    return false;
			    } else if (pchar == 'A' && isLowerAlpha (charCode)) {
				    //setCharCode (e, charCode-32);
                    obj.value += String.fromCharCode(charCode-32).toUpperCase();
                    return false;
			    }
		    } else if (pchar == 'C' || pchar == 'c') {
			    if (!isAlpha (charCode)) {
				    alert("Only characters may be entered here.");
				    return false;
			    } else if (pchar == 'C' && isLowerAlpha (charCode)) {
				    //setCharCode (e, charCode-32);
                    obj.value += String.fromCharCode(charCode-32).toUpperCase();
                    return false;
			    }
		    } else if (charCode != pchar.charCodeAt(0)) {
			    alert("Only a "+pchar+" may be entered here.");
			    return false;
		    }
	    }
	    return true;
	}

	function submitPrep ()
	{
		document.forms[0].GrantorNameList.value = "";
		document.forms[0].GranteeNameList.value = "";
		document.forms[0].TaxMapNumber.value = "";
		var temp;
		for (var i=0; i<document.forms[0].NameList.length-1; i++) {
			temp = document.forms[0].NameList.options[i].value;
			var arr = temp.split("\t");
			if (arr[0] != "") {
				temp = arr[0];

				if (arr[3] == "Firm")    temp += "_F";
				if (arr[3] == "Individual")    temp += "_I";

				if (arr[2] == "Trustee") temp += "_T";
				if (arr[2] == "Non-Trustee") temp += "_N";

				if (arr[1]=="Grantor") {
					document.forms[0].GrantorNameList.value += temp + "|";
				} else if (arr[1] = "Grantee") {
					document.forms[0].GranteeNameList.value += temp + "|";
				}
			}
		}
		temp = "";
		for (var i=0; i<document.forms[0].TMNumberList.length-1; i++){
			if (temp.length > 0)
			temp += "|";
			temp += document.forms[0].TMNumberList.options[i].value.replace("\t",",");
		}
		document.forms[0].TaxMapNumber.value = temp;

		var RangeCount = 0;
		var str = document.forms[0].PageRanges.value;
		if (str.length > 0) {
		    var arr = str.split(',');
			for (var j=0; j<arr.length; j++) {
				var a2 = arr[j].split('-');
				if (a2.length > 1) {
					RangeCount = RangeCount + a2[1]*1 - a2[0]*1 + 1;
				} else {
					RangeCount++;
				}
			}
		}
		document.forms[0].RangeCount.value = RangeCount;
		if (document.forms[0].Certified.checked) {
			document.forms[0].CertifiedFlag.value = 1;
		} else {
			document.forms[0].CertifiedFlag.value = 0;
		}
		if (G_isNetscape) {
			var form = document.forms[0];
			if (form.DEMNumber1.value != "") {
				form.DEMNumber.value = form.DEMNumber1.value+"-"+
										form.DEMNumber2.value+"-"+
										form.DEMNumber3.value+"-"+
										form.DEMNumber4.value+"-"+
										form.DEMNumber5.value;
			} else {
				form.DEMNumber.value = "";
			}

		}
	}

	function createBarcode () {
        var thisForm = document.getElementById("COVERSHEET_FORM");
        thisForm.FeeFlag.value="0";
        if (thisForm.FullInstList.value == "") {
	        alert ("At least one instrument type must be entered.");
	        thisForm.instList.focus();
	        return;
        }
        for (var i=0; i<thisForm.instList.length-1; i++) {
            if ((thisForm.instList.options[i].value == "RFT0" || thisForm.instList.options[i].value == "RFT1") 
                && thisForm.instList.length != 2){
            alert ("You can not combine " + thisForm.instList.options[i].text + " with any other document types.");
            return;
            }
            if (thisForm.instList.options[i].value == "RFT0" && thisForm.TaxExemption.value == "803"
                && (trim(thisForm.Consideration.value) == "" || trim(thisForm.AmountNotTaxed.value) == "")){
            alert ("Consideration and amount not taxed are both required for "+thisForm.instList.options[i].text);
            return;
            }
        }

        if (trim(thisForm.OriginalBook.value) != "" && isNaN(thisForm.OriginalBook.value)){
			    alert ("Please enter a valid original book number.");
			    thisForm.OriginalBook.focus();
			    return;
        }
        if (trim(thisForm.OriginalPage.value) != "" && isNaN(thisForm.OriginalPage.value)){
			    alert ("Please enter a valid original page number.");
			    thisForm.OriginalPage.focus();
			    return;
        }
        if (trim(thisForm.Consideration.value) != "" && isNaN(thisForm.Consideration.value)){
			    alert ("Please enter a valid consideration.");
			    thisForm.Consideration.focus();
			    return;
        }
        if (trim(thisForm.ConsiderationPercentage.value) != "" && isNaN(thisForm.ConsiderationPercentage.value)){
			    alert ("Please enter a valid consideration %.");
			    thisForm.ConsiderationPercentage.focus();
			    return;
        }
        if (trim(thisForm.AmountNotTaxed.value) != "" && isNaN(thisForm.AmountNotTaxed.value)){
			    alert ("Please enter a valid amount not taxed.");
			    thisForm.AmountNotTaxed.focus();
			    return;
        }
		if (updateRange(1)){
			<!--- alert("range OK"); --->
		} else {
		  return;
		}

		if (thisForm.Certified.checked) {
			enableTextField (thisForm.PageRanges);
		}
		submitPrep ();
        if (thisForm.GrantorNameList.value == ""){
          thisForm.GrantorNameList.value="NONE_F_N|";
        }
        if (thisForm.GranteeNameList.value == ""){
          thisForm.GranteeNameList.value="NONE_F_N|";
        }
		if (thisForm.Certified.checked) {
			disableTextField (thisForm.PageRanges);
		}
        thisForm.RequestType.value="Bars"
		thisForm.submit();
	}

	function calculateFee () {
        var thisForm = document.getElementById("COVERSHEET_FORM");
        thisForm.FeeFlag.value="1";
		if (thisForm.instList.length == 0) {
			alert ("At least one instrument type must be entered");
			thisForm.instList.focus();
			return;
		}
        if (trim(thisForm.Pages.value) != "" && isNaN(thisForm.Pages.value)){
			    alert ("Please enter a valid number of pages.");
			    thisForm.Pages.focus();
			    return;
        }
        if (trim(thisForm.Plats.value) != "" && isNaN(thisForm.Plats.value)){
			    alert ("Please enter a valid number of oversized plats.");
			    thisForm.Plats.focus();
			    return;
        }
        if (trim(thisForm.Covers.value) != "" && isNaN(thisForm.Covers.value)){
			    alert ("Please enter a valid number of coversheet pages.");
			    thisForm.Covers.focus();
			    return;
        }
        if (trim(thisForm.Copies.value) != "" && isNaN(thisForm.Copies.value)){
			    alert ("Please enter a valid number of copies.");
			    thisForm.Copies.focus();
			    return;
        }
        if (trim(thisForm.PlatsCopies.value) != "" && isNaN(thisForm.PlatsCopies.value)){
			    alert ("Please enter a valid number of oversized plats copies.");
			    thisForm.PlatsCopies.focus();
			    return;
        }

		if (!updateRange(1)){
		    return;   		
		}
		   
		var temp = "";
		var code = "";
		var RFTFlag = false;
  		for (var i=0; i<thisForm.instList.length-1; i++) {
			temp += "|" + thisForm.instList.options[i].text;
			code += "|" + thisForm.instList.options[i].value;
			if ((thisForm.instList.options[i].value == "RFT0" || thisForm.instList.options[i].value == "RFT1") 
				&& thisForm.instList.length != 2)
			{
				alert ("You can not combine " + thisForm.instList.options[i].text + " with any other document types.");
				return;
			}
			if (thisForm.instList.options[i].value == "RFT0" && thisForm.TaxExemption.value == "803"
				&& (trim(thisForm.Consideration.value) == "" || trim(thisForm.AmountNotTaxed.value) == ""))
			{
				alert ("Consideration and amount not taxed are both required for "+thisForm.instList.options[i].text);
				return;
			}
		}

		thisForm.FullInstList.value = temp;
		thisForm.FullCodeList.value = code;

		if (thisForm.FullInstList.value == "") {
			alert ("At least one instrument type must be entered");
			return;
		}

		if (thisForm.Certified.checked) {
			enableTextField (thisForm.PageRanges);
		}
		submitPrep ();
		if (thisForm.Certified.checked) {
			disableTextField (thisForm.PageRanges);
		}
		thisForm.RequestType.value="Fees"
		thisForm.submit();
	}

	function validateRange (e, fmt, obj) {
		if (document.forms[0].Certified.checked) {
			return true;
		}
		return validate(e, fmt, obj);
	}

	function validateCertified (e) {
		// since we get the key event before the checkbox has
		// changed we reverse the logic
		if (e != null && getCharCode (e) == " ") {
			if (document.forms[0].Certified.checked) {
				document.forms[0].Certified.checked = false;
			} else {
				document.forms[0].Certified.checked = true;
			}
		}

		if (document.forms[0].Certified.checked) {
			updateRange (3);
			disableTextField (document.forms[0].PageRanges);
		} else {
			enableTextField (document.forms[0].PageRanges);
		}
	}

	function updateRange (r) {
		var ncopy = 1*document.forms[0].Copies.value;
		if (r == 2 && ncopy == 0) {  // Copy was changed to 0
			document.forms[0].Certified.checked = false;
			document.forms[0].PageRanges.value = "";
			enableTextField (document.forms[0].PageRanges);
			return true;
		} else if (r == 3 && ncopy == 0) { // Certify was choosen therefore at least 1 copy
			ncopy = 1;
			document.forms[0].Copies.value = 1;
		}
		var pages = 1*document.forms[0].Pages.value;
		var plats = 1*document.forms[0].Plats.value;
		var covers = 1*document.forms[0].Covers.value;
		var list  =   document.forms[0].PageRanges.value;
		var cert  =   document.forms[0].Certified.checked;
		var tot = pages + covers;
		// update the range

		var range  = "";
		if (ncopy > 0 || cert) {
			if (cert) {
				range = "1-"+tot;
			} else if (r == 1) {  // range field was changed
		         
				if (validate_range(tot)){
		 			range = list;
				} else {
		 			return false;
				}
			} else {
				range = "1-"+tot;
			}
		}
		document.forms[0].PageRanges.value = range;
		   
		return true;
	}

	function validate_range(max_pages){
		var input_str= document.forms[0].PageRanges.value;
			
		var range_array = input_str.split(",");
		for (var i = 0;i<range_array.length; i++){
			if (range_array[i] == "")	{
				alert("Invalid page range (" + range_array[i] + ")");
				document.forms[0].PageRanges.focus();
				document.forms[0].PageRanges.select();
				return false;
			}
			var loc = range_array[i].indexOf("-");
			if (loc > 0){
				var first_page = range_array[i].substr(0, loc);
				var n = parseInt(first_page, 10);
				//alert("first page is " + first_page);
				if (n < 1 || n > max_pages)	{
					alert("Page range " + range_array[i] + " outside document page range (1-" + max_pages + ")");
					document.forms[0].PageRanges.focus();
					document.forms[0].PageRanges.select();
					return false;
				}
				var last_page = range_array[i].substr(loc+1);
				var m = parseInt(last_page, 10);
				if (m < 1 || m > max_pages)	{
					alert("Page range " + range_array[i] + " outside document page range (1-" + max_pages + ")");
					document.forms[0].PageRanges.focus();
					document.forms[0].PageRanges.select();
					return false;
				}
				if (m < n)	{
					alert("Invalid page range (" + range_array[i] + ")");
					document.forms[0].PageRanges.focus();
					document.forms[0].PageRanges.select();
					return false;
				}
			}else{
				if (loc == 0){
						
					alert("Invalid page range (" + range_array[i] + ")");
					document.forms[0].PageRanges.focus();
					document.forms[0].PageRanges.select();
					return false;
					}
				else{
					var n = parseInt(range_array[i], 10);
					if (n < 1 || n > max_pages)	{
						alert("Page " + range_array[i] + " outside document page range (1-" + max_pages + ")");
						document.forms[0].PageRanges.focus();
						document.forms[0].PageRanges.select();
						return false;
						}
					}
			}
		}
		return true;
	}
			
	function trim(str){
		if (str.length == 0)
		return "";
		while (str.charAt(0) == " ")
		str = str.substring(1,str.length);
		while (str.charAt(str.length-1) == " ")
		str = str.substring(0,str.length - 1);
		return str;
	}

    function validatePastedValue(fmt, element, makeUpper) {
        setTimeout(function(){
		    if(validateValue(fmt, element) && makeUpper == true)
                makeUpperCase(element);
	    }, 4);
   }			  

	</script>
	</head>
	<body background="image/blue_bkg.jpg">
		<form name="COVERSHEET_FORM" method="post" action="coversheet.aspx" id="COVERSHEET_FORM" target="popup">
<div>
<input type="hidden" name="__VIEWSTATE" id="__VIEWSTATE" value="/wEPDwUJNzE2MzIwNzg1D2QWAgIBD2QWBAIDDxBkDxagAWYCAQICAgMCBAIFAgYCBwIIAgkCCgILAgwCDQIOAg8CEAIRAhICEwIUAhUCFgIXAhgCGQIaAhsCHAIdAh4CHwIgAiECIgIjAiQCJQImAicCKAIpAioCKwIsAi0CLgIvAjACMQIyAjMCNAI1AjYCNwI4AjkCOgI7AjwCPQI+Aj8CQAJBAkICQwJEAkUCRgJHAkgCSQJKAksCTAJNAk4CTwJQAlECUgJTAlQCVQJWAlcCWAJZAloCWwJcAl0CXgJfAmACYQJiAmMCZAJlAmYCZwJoAmkCagJrAmwCbQJuAm8CcAJxAnICcwJ0AnUCdgJ3AngCeQJ6AnsCfAJ9An4CfwKAAQKBAQKCAQKDAQKEAQKFAQKGAQKHAQKIAQKJAQKKAQKLAQKMAQKNAQKOAQKPAQKQAQKRAQKSAQKTAQKUAQKVAQKWAQKXAQKYAQKZAQKaAQKbAQKcAQKdAQKeAQKfARagARAFEkNFUlRJRklDQVRFIE9GIFNBVAUDQ1MwZxAFBERFRUQFAkQwZxAFD1JFRklOQU5DRSBUUlVTVAUEUkZUMGcQBQVUUlVTVAUCVDBnEAUTQVNTSUdOTUVOVCBPRiBUUlVTVAUFVEFTRzBnEAURUE9XRVIgT0YgQVRUT1JORVkFA1BBMGcQBQlBRkZJREFWSVQFA0FGMGcQBQ1TVUJPUkRJTkFUSU9OBQJTMGcQBRRBRkZJREFWSVQgQ09SUkVDVElPTgUDQUYxZxAFFkFGRklEQVZJVCBNT0RJRklDQVRJT04FA0FGMmcQBRdBRkZJREFWSVQgT0YgQUZGSVhBVElPTgUEQUZBMGcQBSJBRkZJREFWSVQgT0YgQUZGSVhBVElPTiBDT1JSRUNUSU9OBQRBRkExZxAFJEFGRklEQVZJVCBPRiBBRkZJWEFUSU9OIE1PRElGSUNBVElPTgUEQUZBMmcQBR1BRkZJREFWSVQgVE8gU0VWRVIgQUZGSVhBVElPTgUFQUZTQTBnEAUoQUZGSURBVklUIFRPIFNFVkVSIEFGRklYQVRJT04gQ09SUkVDVElPTgUFQUZTQTFnEAUqQUZGSURBVklUIFRPIFNFVkVSIEFGRklYQVRJT04gTU9ESUZJQ0FUSU9OBQVBRlNBMmcQBRFBRkZJREFWSVQgUkVMRUFTRQUDQUYzZxAFCUFHUkVFTUVOVAUCQTBnEAUWQUdSRUVNRU5UIENPTkZJUk1BVElPTgUCQTdnEAUUQUdSRUVNRU5UIENPUlJFQ1RJT04FAkExZxAFFkFHUkVFTUVOVCBNT0RJRklDQVRJT04FAkEyZxAFEUFHUkVFTUVOVCBSRUxFQVNFBQJBM2cQBQpBU1NJR05NRU5UBQRBU0cwZxAFFUFTU0lHTk1FTlQgQ09SUkVDVElPTgUEQVNHMWcQBRdBU1NJR05NRU5UIE1PRElGSUNBVElPTgUEQVNHMmcQBR5BU1NJR05NRU5UIE9GIFRSVVNUIENPUlJFQ1RJT04FBVRBU0cxZxAFEkFTU0lHTk1FTlQgUkVMRUFTRQUEQVNHM2cQBRdBU1NJR05NRU5UIFNVUFBMRU1FTlRBTAUEQVNHNWcQBRRBU1NVTVBUSU9OIEFHUkVFTUVOVAUDQUEwZxAFH0FTU1VNUFRJT04gQUdSRUVNRU5UIENPUlJFQ1RJT04FA0FBMWcQBRxBU1NVTVBUSU9OIEFHUkVFTUVOVCBSRUxFQVNFBQNBQTNnEAUMQklMTCBPRiBTQUxFBQRCT1MwZxAFF0JJTEwgT0YgU0FMRSBDT1JSRUNUSU9OBQRCT1MxZxAFB0JZIExBV1MFA0JMMGcQBRJCWSBMQVdTIENPUlJFQ1RJT04FA0JMMWcQBRRCWSBMQVdTIE1PRElGSUNBVElPTgUDQkwyZxAFC0NFUlRJRklDQVRFBQVDRVJUMGcQBRZDRVJUSUZJQ0FURSBDT1JSRUNUSU9OBQVDRVJUMWcQBRhDRVJUSUZJQ0FURSBNT0RJRklDQVRJT04FBUNFUlQyZxAFHUNFUlRJRklDQVRFIE9GIFNBVCBDT1JSRUNUSU9OBQNDUzFnEAUXQ0VSVElGSUNBVEUgT0YgVFJBTlNGRVIFA0NUMGcQBSJDRVJUSUZJQ0FURSBPRiBUUkFOU0ZFUiBDT1JSRUNUSU9OBQNDVDFnEAUTQ0VSVElGSUNBVEUgUkVMRUFTRQUFQ0VSVDNnEAURQ09ORklSTUFUSU9OIERFRUQFBENGRDBnEAUcQ09ORklSTUFUSU9OIERFRUQgQ09SUkVDVElPTgUEQ0ZEMWcQBQhDT05UUkFDVAUCQzBnEAUTQ09OVFJBQ1QgQ09SUkVDVElPTgUCQzFnEAUVQ09OVFJBQ1QgTU9ESUZJQ0FUSU9OBQJDMmcQBRBDT05UUkFDVCBSRUxFQVNFBQJDM2cQBRRDT1JSRUNUSVZFIEFGRklEQVZJVAUEQ09SMGcQBR9DT1JSRUNUSVZFIEFGRklEQVZJVCBDT1JSRUNUSU9OBQRDT1IxZxAFIUNPUlJFQ1RJVkUgQUZGSURBVklUIE1PRElGSUNBVElPTgUEQ09SMmcQBQtDT1VSVCBPUkRFUgUCTzBnEAUWQ09VUlQgT1JERVIgQ09SUkVDVElPTgUCTzFnEAUYQ09VUlQgT1JERVIgTU9ESUZJQ0FUSU9OBQJPMmcQBQhDT1ZFTkFOVAUEQ09WMGcQBRNDT1ZFTkFOVCBDT1JSRUNUSU9OBQRDT1YxZxAFFUNPVkVOQU5UIE1PRElGSUNBVElPTgUEQ09WMmcQBRBDT1ZFTkFOVCBSRUxFQVNFBQRDT1YzZxAFFUNPVkVOQU5UIFNVUFBMRU1FTlRBTAUEQ09WNWcQBQtERUNMQVJBVElPTgUDREwwZxAFFkRFQ0xBUkFUSU9OIENPUlJFQ1RJT04FA0RMMWcQBRhERUNMQVJBVElPTiBNT0RJRklDQVRJT04FA0RMMmcQBQpERURJQ0FUSU9OBQNETjBnEAUVREVESUNBVElPTiBDT1JSRUNUSU9OBQNETjFnEAUPREVFRCBDT1JSRUNUSU9OBQJEMWcQBSBERUVEIEJFVFdFRU4gTUFSUklFRCBJTkRJVklEVUFMUwUFREJNSTBnEAUrREVFRCBCRVRXRUVOIE1BUlJJRUQgSU5ESVZJRFVBTFMgQ09SUkVDVElPTgUFREJNSTFnEAUMREVFRCBPRiBHSUZUBQNERzBnEAUXREVFRCBPRiBHSUZUIENPUlJFQ1RJT04FA0RHMWcQBQpESVNDTEFJTUVSBQVESVNDMGcQBRVESVNDTEFJTUVSIENPUlJFQ1RJT04FBURJU0MxZxAFCEVBU0VNRU5UBQJFMGcQBRNFQVNFTUVOVCBDT1JSRUNUSU9OBQJFMWcQBQ5IT01FU1RFQUQgREVFRAUDSEQwZxAFGUhPTUVTVEVBRCBERUVEIENPUlJFQ1RJT04FA0hEMWcQBRtIT01FU1RFQUQgREVFRCBNT0RJRklDQVRJT04FA0hEMmcQBRNJTlNUUlVNRU5UIE5PVCBVU0VEBQRJTlUwZxAFBUxFQVNFBQJMMGcQBRBMRUFTRSBBU1NJR05NRU5UBQJMNGcQBR9MRUFTRSBDT01NVU5JQ0FUSU9OUyBFUVVJUE1FTlQgBQRMQ0UwZxAFKUxFQVNFIENPTU1VTklDQVRJT05TIEVRVUlQTUVOVCBBU1NJR05NRU5UBQRMQ0U0ZxAFKUxFQVNFIENPTU1VTklDQVRJT05TIEVRVUlQTUVOVCBDT1JSRUNUSU9OBQRMQ0UxZxAFK0xFQVNFIENPTU1VTklDQVRJT05TIEVRVUlQTUVOVCBNT0RJRklDQVRJT04FBExDRTJnEAUmTEVBU0UgQ09NTVVOSUNBVElPTlMgRVFVSVBNRU5UIFJFTEVBU0UFBExDRTNnEAUbTEVBU0UgQ09NTVVOSUNBVElPTlMgVE9XRVIgBQRMQ1QwZxAFJUxFQVNFIENPTU1VTklDQVRJT05TIFRPV0VSIEFTU0lHTk1FTlQFBExDVDRnEAUlTEVBU0UgQ09NTVVOSUNBVElPTlMgVE9XRVIgQ09SUkVDVElPTgUETENUMWcQBSdMRUFTRSBDT01NVU5JQ0FUSU9OUyBUT1dFUiBNT0RJRklDQVRJT04FBExDVDJnEAUiTEVBU0UgQ09NTVVOSUNBVElPTlMgVE9XRVIgUkVMRUFTRQUETENUM2cQBRBMRUFTRSBDT1JSRUNUSU9OBQJMMWcQBRJMRUFTRSBNT0RJRklDQVRJT04FAkwyZxAFDUxFQVNFIFJFTEVBU0UFAkwzZxAFB0xJQ0VOU0UFBExJQzBnEAUSTElDRU5TRSBDT1JSRUNUSU9OBQRMSUMxZxAFBExJRU4FA0xOMGcQBQ9MSUVOIENPUlJFQ1RJT04FA0xOMWcQBQxMSUVOIFJFTEVBU0UFA0xOM2cQBQtMSVMgUEVOREVOUwUDTFAwZxAFFkxJUyBQRU5ERU5TIENPUlJFQ1RJT04FA0xQMWcQBRNMSVMgUEVOREVOUyBSRUxFQVNFBQNMUDNnEAUOTUVDSEFOSUNTIExJRU4FA01MMGcQBRlNRUNIQU5JQ1MgTElFTiBDT1JSRUNUSU9OBQNNTDFnEAUWTUVDSEFOSUNTIExJRU4gUkVMRUFTRQUDTUwzZxAFBk1FUkdFUgUFTUVSRzBnEAURTUVSR0VSIENPUlJFQ1RJT04FBU1FUkcxZxAFFU1PRElGSUNBVElPTiBPRiBUUlVTVAUFTU9ERjBnEAUgTU9ESUZJQ0FUSU9OIE9GIFRSVVNUIENPUlJFQ1RJT04FBU1PREYxZxAFCE1PUlRHQUdFBQJNMGcQBRNNT1JUR0FHRSBDT1JSRUNUSU9OBQJNMWcQBRVNT1JUR0FHRSBNT0RJRklDQVRJT04FAk0yZxAFEE1PUlRHQUdFIFJFTEVBU0UFAk0zZxAFBk5PVElDRQUETk9UMGcQBRFOT1RJQ0UgQ09SUkVDVElPTgUETk9UMWcQBQZPUFRJT04FA09QMGcQBRFPUFRJT04gQ09SUkVDVElPTgUDT1AxZxAFDk9QVElPTiBSRUxFQVNFBQNPUDNnEAUGUEVSTUlUBQNQTTBnEAURUEVSTUlUIENPUlJFQ1RJT04FA1BNMWcQBRxQT1dFUiBPRiBBVFRPUk5FWSBDT1JSRUNUSU9OBQNQQTFnEAUOUVVJVENMQUlNIERFRUQFA1FEMGcQBRlRVUlUQ0xBSU0gREVFRCBDT1JSRUNUSU9OBQNRRDFnEAUSUVVJVENMQUlNIEVBU0VNRU5UBQNRRTBnEAUdUVVJVENMQUlNIEVBU0VNRU5UIENPUlJFQ1RJT04FA1FFMWcQBRpSRUZJTkFOQ0UgVFJVU1QgQ09SUkVDVElPTgUEUkZUMWcQBQ1SRUxFQVNFIFRSVVNUBQJSMGcQBRhSRUxFQVNFIFRSVVNUIENPUlJFQ1RJT04FAlIxZxAFFlJFU0lHTkFUSU9OIE9GIFRSVVNURUUFA1JUMGcQBSFSRVNJR05BVElPTiBPRiBUUlVTVEVFIENPUlJFQ1RJT04FA1JUMWcQBQpSRVNPTFVUSU9OBQVSRVNMMGcQBRVSRVNPTFVUSU9OIENPUlJFQ1RJT04FBVJFU0wxZxAFDVJFU1VCRElWSVNJT04FA1JFMGcQBRhSRVNVQkRJVklTSU9OIENPUlJFQ1RJT04FA1JFMWcQBRhSRVZPS0UgUE9XRVIgT0YgQVRUT1JORVkFBFJQQTBnEAUjUkVWT0tFIFBPV0VSIE9GIEFUVE9STkVZIENPUlJFQ1RJT04FBFJQQTFnEAUYU1VCT1JESU5BVElPTiBDT1JSRUNUSU9OBQJTMWcQBRtTVUJTVElUVVRFIEFUVE9STkVZIElOIEZBQ1QFBVNBSUYwZxAFJlNVQlNUSVRVVEUgQVRUT1JORVkgSU4gRkFDVCBDT1JSRUNUSU9OBQVTQUlGMWcQBRJTVUJTVElUVVRFIFRSVVNURUUFA1NUMGcQBR1TVUJTVElUVVRFIFRSVVNURUUgQ09SUkVDVElPTgUDU1QxZxAFFlRSQU5TRkVSIE9OIERFQVRIIERFRUQFBERURDBnEAUhVFJBTlNGRVIgT04gREVBVEggREVFRCBDT1JSRUNUSU9OBQREVEQxZxAFIVRSQU5TRkVSIE9OIERFQVRIIERFRUQgUkVWT0NBVElPTgUFRFREUjBnEAUsVFJBTlNGRVIgT04gREVBVEggREVFRCBSRVZPQ0FUSU9OIENPUlJFQ1RJT04FBURURFIxZxAFD1RSVVNUIEFHUkVFTUVOVAUDVEEwZxAFGlRSVVNUIEFHUkVFTUVOVCBDT1JSRUNUSU9OBQNUQTFnEAUcVFJVU1QgQUdSRUVNRU5UIE1PRElGSUNBVElPTgUDVEEyZxAFEFRSVVNUIENPUlJFQ1RJT04FAlQxZxAFElRSVVNUIFNVUFBMRU1FTlRBTAUCVDVnEAUNVFJVU1RFRVMgREVFRAUDVEQwZxAFGFRSVVNURUVTIERFRUQgQ09SUkVDVElPTgUDVEQxZxAFD1RSVVNURUVTIFJFUE9SVAUDVFIwZxAFGlRSVVNURUVTIFJFUE9SVCBDT1JSRUNUSU9OBQNUUjFnEAUIVkFDQVRJT04FAlYwZxAFE1ZBQ0FUSU9OIENPUlJFQ1RJT04FAlYxZxAFCVZJT0xBVElPTgUFVklPTDBnEAUUVklPTEFUSU9OIENPUlJFQ1RJT04FBVZJT0wxZxAFEVZJT0xBVElPTiBSRUxFQVNFBQVWSU9MM2cQBQZXQUlWRVIFBVdBSVYwZxAFEVdBSVZFUiBDT1JSRUNUSU9OBQVXQUlWMWdkZAIFDxBkEBUKBE5PTkUDODAzAzgxMQNDRk8CTkMCTkcCTlMDTlNDA05URgJQUxUKASADODAzAzgxMQNDRk8CTkMCTkcCTlMDTlNDA05URgJQUxQrAwpnZ2dnZ2dnZ2dnZGRkWJ9ce+OvreiL1lTT20PgaXchrpE=" />
</div>

<div>

	<input type="hidden" name="__EVENTVALIDATION" id="__EVENTVALIDATION" value="/wEWqwEC4bysvgUCuryUygQCkf3/mgMCmc2onwcCof3/mgMC8OHXzwkCx73cywQCtLygywQCoP3/mgMC2YrGpgoC4pPkEQLppdSLAwLppbjWCwLppYyxBAK9/bOhBQK8/bOhBQK//bOhBQKH+JuLBgKS/f+aAwKS/duaAwKS/fOaAwKS/feaAwKS/cuaAwKLvNWLBgKLvLnWDgKLvI2xBwLx4dfPCQKLvJEcAou8ycMBArS83MsEAtmK8qYKAof4t4sGAuTkrqQBAuTkso8KArW8yMsEAt6K7qYKAuOTjBAC3pSElA4C2ZSElA4C3JSElA4C34qKpQoCurzoywQC34qOpQoC35SElA4CvuCPywQCvuCTlg0CkP3/mgMCkP3zmgMCkP33mgMCkP3LmgMCwJ+LyQsCwJ+flAQCwJ/j8AwCrP3/mgMCrP3zmgMCrP33mgMCtKOA5AICtKOUzwsCtKP4qwQCtKPM9gwCtKOkvA4Cu7zIywQC3IrupgoC4ZOMEAK7vMDLBALciuamCgKR/fOaAwKWmJT/CQKXmJT/CQK7vKTLBALcitqmCgKA/KfaBgKB/KfaBgKW/f+aAwKW/fOaAwLPvajLBALQis6mCgL1k+wRAqXa7ooNAqn9/5oDAqn9z5oDAtjO0aYKAtjO4ZMNAtjOpYEDAtjOiewLAtjOnbcEAofN3J8HAofN7IwKAofNoPoPAofNtMUIAofNmKABAqn985oDAqn995oDAqn9y5oDApKbpNAOApKbiLsHArO8wMsEAtSK5qYKAoL4u4sGArO8mMoEAtSKvqUKAoL484sGArC8yMsEAtWK7qYKAoP4o4sGAvSVgOgDAveVgOgDAtL/zIkEAu3/zIkEAq79/5oDAq7985oDAq7995oDAq79y5oDAoXNzJ8HAoXN0PoPAra8mMoEAtuKvqUKAoH484sGAse9zMsEAuiK4qYKAuiK8qYKAsS9qMsEAumKzqYKAsS9rMsEAumKwqYKApnNvPoPAqP9/5oDAqP985oDAsW96MsEAu6KjqUKArLhm+gJArPhm+gJAsW9rMsEAu6KwqYKAv6ljIoDAv6lkNULAqD985oDAo34nOQDAoz4nOQDAsq96MsEAu+KjqUKAr/g98sEAr/g25YNAtX5gN4FAtr5gN4FAsu93MsEAuyK8qYKAvGTkBACof3zmgMCof3DmgMCy72oywQC7IrOpgoCy72QygQC7Iq2pQoCp/3/mgMCp/3zmgMC8vOx3Q8C8/Ox3Q8C8fOx3Q8C8fvcsggC8PvcsggCt7H3zA4CtLS72wsC+sn67QcC27qYpgYC1bHD0g4C1bHT0g4C1bGD0g4C6tfU+A4CmZuDuwkCx7GD0g7k3our3txh2jjyvf5oyxpfZsIYTQ==" />
</div>
			<input type="hidden" name="RequestType" id="RequestType" value="Bars" />
			<table cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<td width="14%" align="center">
						<img alt="" src="image/vc4-2.gif" width="125" height="125" border="0" />
					</td>
					<td align="center">
						<table width="74%">
							<tr>
								<td align="center">
									<font face="Arial" size="+2"><strong>Fairfax County Circuit Court</strong></font><br/>
								</td>
							</tr>
							<tr>
								<td align="center">
									<font face="Arial" size="+2"><strong>DMZ Cover Sheet Application v2.2</strong></font><br/>
								</td>
							</tr>
						</table>
					</td>
					<td width="12%" align="center">
						<img alt="" src="image/vc4-2.gif" width="125" height="125" border="0" />
					</td>
				</tr>
			</table>
			<hr size="3" noshade="noshade" />
			<span id="Message"></span>
			<center><B>One Cover Sheet Per Document</B></center>
			<input type="hidden" name="FullInstList" id="FullInstList" /> <input type="hidden" name="FullCodeList" id="FullCodeList"/>
			<input type="hidden" name="GrantorNameList" id="GrantorNameList" /> <input type="hidden" name="GranteeNameList" id="GranteeNameList" />
			<input type="hidden" name="RangeCount" id="RangeCount" value="0" /> <input type="hidden" name="CertifiedFlag" id="CertifiedFlag" value="0" />
			<input type="hidden" name="TaxMapNumber" id="TaxMapNumber" />
			<table border="0">
				<tr>
					<td>
						<span style="font-weight:bold;">Instrument Type</span><br/>
						<select name="InstrumentTypeList" id="InstrumentTypeList">
	<option value="CS0">CERTIFICATE OF SAT</option>
	<option value="D0">DEED</option>
	<option value="RFT0">REFINANCE TRUST</option>
	<option value="T0">TRUST</option>
	<option value="TASG0">ASSIGNMENT OF TRUST</option>
	<option value="PA0">POWER OF ATTORNEY</option>
	<option value="AF0">AFFIDAVIT</option>
	<option value="S0">SUBORDINATION</option>
	<option value="AF1">AFFIDAVIT CORRECTION</option>
	<option value="AF2">AFFIDAVIT MODIFICATION</option>
	<option value="AFA0">AFFIDAVIT OF AFFIXATION</option>
	<option value="AFA1">AFFIDAVIT OF AFFIXATION CORRECTION</option>
	<option value="AFA2">AFFIDAVIT OF AFFIXATION MODIFICATION</option>
	<option value="AFSA0">AFFIDAVIT TO SEVER AFFIXATION</option>
	<option value="AFSA1">AFFIDAVIT TO SEVER AFFIXATION CORRECTION</option>
	<option value="AFSA2">AFFIDAVIT TO SEVER AFFIXATION MODIFICATION</option>
	<option value="AF3">AFFIDAVIT RELEASE</option>
	<option value="A0">AGREEMENT</option>
	<option value="A7">AGREEMENT CONFIRMATION</option>
	<option value="A1">AGREEMENT CORRECTION</option>
	<option value="A2">AGREEMENT MODIFICATION</option>
	<option value="A3">AGREEMENT RELEASE</option>
	<option value="ASG0">ASSIGNMENT</option>
	<option value="ASG1">ASSIGNMENT CORRECTION</option>
	<option value="ASG2">ASSIGNMENT MODIFICATION</option>
	<option value="TASG1">ASSIGNMENT OF TRUST CORRECTION</option>
	<option value="ASG3">ASSIGNMENT RELEASE</option>
	<option value="ASG5">ASSIGNMENT SUPPLEMENTAL</option>
	<option value="AA0">ASSUMPTION AGREEMENT</option>
	<option value="AA1">ASSUMPTION AGREEMENT CORRECTION</option>
	<option value="AA3">ASSUMPTION AGREEMENT RELEASE</option>
	<option value="BOS0">BILL OF SALE</option>
	<option value="BOS1">BILL OF SALE CORRECTION</option>
	<option value="BL0">BY LAWS</option>
	<option value="BL1">BY LAWS CORRECTION</option>
	<option value="BL2">BY LAWS MODIFICATION</option>
	<option value="CERT0">CERTIFICATE</option>
	<option value="CERT1">CERTIFICATE CORRECTION</option>
	<option value="CERT2">CERTIFICATE MODIFICATION</option>
	<option value="CS1">CERTIFICATE OF SAT CORRECTION</option>
	<option value="CT0">CERTIFICATE OF TRANSFER</option>
	<option value="CT1">CERTIFICATE OF TRANSFER CORRECTION</option>
	<option value="CERT3">CERTIFICATE RELEASE</option>
	<option value="CFD0">CONFIRMATION DEED</option>
	<option value="CFD1">CONFIRMATION DEED CORRECTION</option>
	<option value="C0">CONTRACT</option>
	<option value="C1">CONTRACT CORRECTION</option>
	<option value="C2">CONTRACT MODIFICATION</option>
	<option value="C3">CONTRACT RELEASE</option>
	<option value="COR0">CORRECTIVE AFFIDAVIT</option>
	<option value="COR1">CORRECTIVE AFFIDAVIT CORRECTION</option>
	<option value="COR2">CORRECTIVE AFFIDAVIT MODIFICATION</option>
	<option value="O0">COURT ORDER</option>
	<option value="O1">COURT ORDER CORRECTION</option>
	<option value="O2">COURT ORDER MODIFICATION</option>
	<option value="COV0">COVENANT</option>
	<option value="COV1">COVENANT CORRECTION</option>
	<option value="COV2">COVENANT MODIFICATION</option>
	<option value="COV3">COVENANT RELEASE</option>
	<option value="COV5">COVENANT SUPPLEMENTAL</option>
	<option value="DL0">DECLARATION</option>
	<option value="DL1">DECLARATION CORRECTION</option>
	<option value="DL2">DECLARATION MODIFICATION</option>
	<option value="DN0">DEDICATION</option>
	<option value="DN1">DEDICATION CORRECTION</option>
	<option value="D1">DEED CORRECTION</option>
	<option value="DBMI0">DEED BETWEEN MARRIED INDIVIDUALS</option>
	<option value="DBMI1">DEED BETWEEN MARRIED INDIVIDUALS CORRECTION</option>
	<option value="DG0">DEED OF GIFT</option>
	<option value="DG1">DEED OF GIFT CORRECTION</option>
	<option value="DISC0">DISCLAIMER</option>
	<option value="DISC1">DISCLAIMER CORRECTION</option>
	<option value="E0">EASEMENT</option>
	<option value="E1">EASEMENT CORRECTION</option>
	<option value="HD0">HOMESTEAD DEED</option>
	<option value="HD1">HOMESTEAD DEED CORRECTION</option>
	<option value="HD2">HOMESTEAD DEED MODIFICATION</option>
	<option value="INU0">INSTRUMENT NOT USED</option>
	<option value="L0">LEASE</option>
	<option value="L4">LEASE ASSIGNMENT</option>
	<option value="LCE0">LEASE COMMUNICATIONS EQUIPMENT </option>
	<option value="LCE4">LEASE COMMUNICATIONS EQUIPMENT ASSIGNMENT</option>
	<option value="LCE1">LEASE COMMUNICATIONS EQUIPMENT CORRECTION</option>
	<option value="LCE2">LEASE COMMUNICATIONS EQUIPMENT MODIFICATION</option>
	<option value="LCE3">LEASE COMMUNICATIONS EQUIPMENT RELEASE</option>
	<option value="LCT0">LEASE COMMUNICATIONS TOWER </option>
	<option value="LCT4">LEASE COMMUNICATIONS TOWER ASSIGNMENT</option>
	<option value="LCT1">LEASE COMMUNICATIONS TOWER CORRECTION</option>
	<option value="LCT2">LEASE COMMUNICATIONS TOWER MODIFICATION</option>
	<option value="LCT3">LEASE COMMUNICATIONS TOWER RELEASE</option>
	<option value="L1">LEASE CORRECTION</option>
	<option value="L2">LEASE MODIFICATION</option>
	<option value="L3">LEASE RELEASE</option>
	<option value="LIC0">LICENSE</option>
	<option value="LIC1">LICENSE CORRECTION</option>
	<option value="LN0">LIEN</option>
	<option value="LN1">LIEN CORRECTION</option>
	<option value="LN3">LIEN RELEASE</option>
	<option value="LP0">LIS PENDENS</option>
	<option value="LP1">LIS PENDENS CORRECTION</option>
	<option value="LP3">LIS PENDENS RELEASE</option>
	<option value="ML0">MECHANICS LIEN</option>
	<option value="ML1">MECHANICS LIEN CORRECTION</option>
	<option value="ML3">MECHANICS LIEN RELEASE</option>
	<option value="MERG0">MERGER</option>
	<option value="MERG1">MERGER CORRECTION</option>
	<option value="MODF0">MODIFICATION OF TRUST</option>
	<option value="MODF1">MODIFICATION OF TRUST CORRECTION</option>
	<option value="M0">MORTGAGE</option>
	<option value="M1">MORTGAGE CORRECTION</option>
	<option value="M2">MORTGAGE MODIFICATION</option>
	<option value="M3">MORTGAGE RELEASE</option>
	<option value="NOT0">NOTICE</option>
	<option value="NOT1">NOTICE CORRECTION</option>
	<option value="OP0">OPTION</option>
	<option value="OP1">OPTION CORRECTION</option>
	<option value="OP3">OPTION RELEASE</option>
	<option value="PM0">PERMIT</option>
	<option value="PM1">PERMIT CORRECTION</option>
	<option value="PA1">POWER OF ATTORNEY CORRECTION</option>
	<option value="QD0">QUITCLAIM DEED</option>
	<option value="QD1">QUITCLAIM DEED CORRECTION</option>
	<option value="QE0">QUITCLAIM EASEMENT</option>
	<option value="QE1">QUITCLAIM EASEMENT CORRECTION</option>
	<option value="RFT1">REFINANCE TRUST CORRECTION</option>
	<option value="R0">RELEASE TRUST</option>
	<option value="R1">RELEASE TRUST CORRECTION</option>
	<option value="RT0">RESIGNATION OF TRUSTEE</option>
	<option value="RT1">RESIGNATION OF TRUSTEE CORRECTION</option>
	<option value="RESL0">RESOLUTION</option>
	<option value="RESL1">RESOLUTION CORRECTION</option>
	<option value="RE0">RESUBDIVISION</option>
	<option value="RE1">RESUBDIVISION CORRECTION</option>
	<option value="RPA0">REVOKE POWER OF ATTORNEY</option>
	<option value="RPA1">REVOKE POWER OF ATTORNEY CORRECTION</option>
	<option value="S1">SUBORDINATION CORRECTION</option>
	<option value="SAIF0">SUBSTITUTE ATTORNEY IN FACT</option>
	<option value="SAIF1">SUBSTITUTE ATTORNEY IN FACT CORRECTION</option>
	<option value="ST0">SUBSTITUTE TRUSTEE</option>
	<option value="ST1">SUBSTITUTE TRUSTEE CORRECTION</option>
	<option value="DTD0">TRANSFER ON DEATH DEED</option>
	<option value="DTD1">TRANSFER ON DEATH DEED CORRECTION</option>
	<option value="DTDR0">TRANSFER ON DEATH DEED REVOCATION</option>
	<option value="DTDR1">TRANSFER ON DEATH DEED REVOCATION CORRECTION</option>
	<option value="TA0">TRUST AGREEMENT</option>
	<option value="TA1">TRUST AGREEMENT CORRECTION</option>
	<option value="TA2">TRUST AGREEMENT MODIFICATION</option>
	<option value="T1">TRUST CORRECTION</option>
	<option value="T5">TRUST SUPPLEMENTAL</option>
	<option value="TD0">TRUSTEES DEED</option>
	<option value="TD1">TRUSTEES DEED CORRECTION</option>
	<option value="TR0">TRUSTEES REPORT</option>
	<option value="TR1">TRUSTEES REPORT CORRECTION</option>
	<option value="V0">VACATION</option>
	<option value="V1">VACATION CORRECTION</option>
	<option value="VIOL0">VIOLATION</option>
	<option value="VIOL1">VIOLATION CORRECTION</option>
	<option value="VIOL3">VIOLATION RELEASE</option>
	<option value="WAIV0">WAIVER</option>
	<option value="WAIV1">WAIVER CORRECTION</option>

</select>
					</td>
				</tr>
			</table>
			<table width="80%" border="0">
				<tr>
					<td width="40%">
						<select name="instList" size="3">
							<script type="text/javascript">
							    setWidth();
							</script>
						</select>
					</td>
					<td width="40%" align="left">
						<input type="button" class="button" value="Add Instrument Type   " onclick="AddInst()" /><br/>
						<input type="button" class="button" value="Remove Instrument Type" onclick="RemoveInst()" /><br/>
						<input type="button" class="button" value="Change Instrument Type" onclick="ChangeInst()" /><br/>
					</td>
				</tr>
			</table>
			<table border="0">
				<tr>
					<td style="font-weight:bold;">Names to be indexed (Last, First Middle)&nbsp;</td>
					<td style="font-weight:bold;">Type&nbsp;</td>
					<td style="font-weight:bold;">Trustee&nbsp;</td>
					<td style="font-weight:bold;">Firm&nbsp;</td>
				</tr>
				<tr>
					<td>
						<input type="text" name="Names" size="40" maxlength="60" onkeypress="return checkSpecChar(event, this)" onblur='if(hasNoSpecialChars(this)) makeUpperCase(this);' />
					</td>
					<td>
						<select name="Name_Type">
							<option selected="selected">Grantor</option>
							<option>Grantee</option>
						</select>
					</td>
					<td>
						<input type="checkbox" name="Trustee" />
					</td>
					<td>
						<input type="checkbox" name="Firm" />
					</td>
				</tr>
			</table>
			<table width="80%" border="0">
				<tr>
					<td width="40%">
						<select name="NameList" size="3">
							<script type="text/javascript">
							    setWidth();
							</script>
						</select>
					</td>
					<td align="left" width="40%">
						<input type="button" class="button" value="Add Name   " onclick="AddName()" /><br/>
						<input type="button" class="button" value="Remove Name" onclick="RemoveName()" /><br/>
						<input type="button" class="button" value="Change Name" onclick="ChangeName()" /><br/>
					</td>
				</tr>
			</table>

			<script type="text/javascript">
		        if (G_isNetscape) {
			        document.write ("<table name='table1'>");
		        } else {
			        document.write ("<table id='TaxMask' border='0' width='100%' cellspacing=0 cellpadding=0>");
		        }
			</script>

            <table>
			<tr>
				<td><br/></td>
			</tr>
			<tr>
				<td style="font-weight:bold;">Consideration</td>
				<td><input type='text' name='Consideration' onkeypress='return validate(event,"NNNNNNNNNNNN",this)' onpaste='validatePastedValue("NNNNNNNNNNNN",this);' onblur='return validateValue("NNNNNNNNNNNN",this);'
						size='13' maxlength='13' onchange='checkBounds(this,0, 9999999999.99)' /></td>
				<td style="font-weight:bold;">Consideration/Actual Value %</td>
				<td><input type="text" name="ConsiderationPercentage" size="5" maxlength="5" value="100" onkeypress='return validate(event,"NNNNN", this)' onpaste='validatePastedValue("NNNNN", this)' onblur='return validateValue("NNNNN",this);'
						onchange='checkBounds(this,0, 100)' /></td>
			</tr>
			<tr>
				<td style="font-weight:bold;">Amount Not Taxed<br/>
					<FONT size="1">(Assumption or Original Trust)</FONT></td>
				<td><input type="text" name="AmountNotTaxed" size="13" maxlength="13" onkeypress='return validate(event,"NNNNNNNNNNNN", this)' onpaste='validatePastedValue("NNNNNNNNNNNN", this);' onblur='return validateValue("NNNNNNNNNNNN",this);'
						onchange='checkBounds(this,0, 9999999999.99)' /></td>
				 <td style="font-weight:bold;">Actual/Assessed Value</td>
                <td>
                    <input type="text" name="ActualAssessed" size="13" maxlength="13" onkeypress='return validate(event,"NNNNNNNNNNNN", this)' onpaste='validatePastedValue("NNNNNNNNNNNN", this);' onblur='return validateValue("NNNNNNNNNNNN",this);'
						onchange='checkBounds(this,0, 9999999999.99)' />
                </td>
				
            </tr>
                <tr><td style="font-weight:bold;">Tax Exemption</td>               
				<td>
					<select name="TaxExemption" id="TaxExemption">
	<option value=" ">NONE</option>
	<option value="803">803</option>
	<option value="811">811</option>
	<option value="CFO">CFO</option>
	<option value="NC">NC</option>
	<option value="NG">NG</option>
	<option value="NS">NS</option>
	<option value="NSC">NSC</option>
	<option value="NTF">NTF</option>
	<option value="PS">PS</option>

</select>
				</td>               
                <td style="font-weight:bold;">Code Section</td>
                <td>
                    <input type="text" name="CodeSection" size="50" maxlength="99" onkeypress="return checkSpecChar(event, this)" onblur='if(hasNoSpecialChars(this)) makeUpperCase(this);' />
                </td></tr>
			<tr>
				<td valign="bottom"><B>DEM Number</B></td>
				<script type="text/javascript">
				    insertDEMField();
				</script>
				<td style="font-weight:bold;">Original Book<br/>Original Page</td>
				<td><input type="text" name="OriginalBook" size="5" maxlength="5" onkeypress='return validate(event,"#####", this);' onpaste='validatePastedValue("#####", this);' onblur='return validateValue("#####",this);' /><br/>
					<input type="text" name="OriginalPage" size="4" maxlength="4" onkeypress='return validate(event,"####", this);' onpaste='validatePastedValue("####", this);' onblur='return validateValue("####",this);' />
                </td>
			</tr>
			<tr>
				<td></td>
			</tr>
			<tr>
				<td><B>Title Company</B></td>
				<td><input type="text" name='TitleCompany' size="40" maxlength="40" onkeypress='return validate(event,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this)' onpaste='validatePastedValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this, true);' onblur='if(validateValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",this))makeUpperCase(this);' /></td>
				<td><B>Title Case #</B></td>
				<td colspan="2"><input type="text" name='TitleCase' size="10" maxlength="10" onkeypress='return validate(event,"!!!!!!!!!!!", this)' onpaste='validatePastedValue("!!!!!!!!!!!", this, true)' onblur='if(validateValue("!!!!!!!!!!!",this))makeUpperCase(this);' /></td>
			</tr>
			<tr id="TaxMaskRow">
				<td valign="top"><b>Tax Map Number</b></td>
				<td valign="top" colspan="4">
					<table border="0">
						<tr>
							<td colspan="2">
								<input type="radio" name="TMType" value="County" onclick="onTMTypeChange()" checked="checked" />
								County <input type="radio" name="TMType" value="City" onclick="onTMTypeChange()" />
								City
							</td>
						</tr>
						<tr>
							<td colspan="2">
								<!---div id="TaxMapFormat">###-#-*-##-**-####-**</div>
							<input type="text" name="TMNumber" size="21" maxlength="21" onkeypress="return validateTaxMap(event, this)"--->
								<div id="TaxMapCounty">
									<table border="0" cellspacing="0" cellpadding="0">
										<tr style="FONT-SIZE:10px; FONT-STYLE:italic">
											<td align="center" valign="bottom">Grid</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Quad</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Filler</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Double<br/>
												Circle</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Single<br/>
												Circle</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Lot</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Sublot</td>
										</tr>
										<tr>
											<td align="center">###</td>
											<td>&nbsp;</td>
											<td align="center">#</td>
											<td>&nbsp;</td>
											<td align="center">*</td>
											<td>&nbsp;</td>
											<td align="center">##</td>
											<td>&nbsp;</td>
											<td align="center">**</td>
											<td>&nbsp;</td>
											<td align="center">####</td>
											<td>&nbsp;</td>
											<td align="center">**</td>
											<td valign="top" rowspan="3">&nbsp; <a href="Cover_Sheet02.htm" target="help_frame" style="font-weight:bold; FONT-SIZE:10px; VERTICAL-ALIGN:middle; BORDER-TOP-STYLE:double; BORDER-RIGHT-STYLE:double; BORDER-LEFT-STYLE:double; TEXT-DECORATION:none; BORDER-BOTTOM-STYLE:double">
													&nbsp;?&nbsp;</a>
											</td>
										</tr>
										<tr>
											<td><input type="text" name="TMNumA1" size="3" maxlength="3" onkeypress="return validate(event,'###',this)" onpaste="validatePastedValue('###',this)" onblur="return validateValue('###',this)" /></td>
											<td>-</td>
											<td><input type="text" name="TMNumA2" size="1" maxlength="1" onkeypress="return validate(event,'#',this);" onpaste="validatePastedValue('#',this)" onblur="return validateValue('#',this)" /></td>
											<td>-</td>
											<td><input type="text" name="TMNumA3" size="1" maxlength="1" disabled="disabled" style="background-color:#dddddd" /></td>
											<td>-</td>
											<td><input type="text" name="TMNumA4" size="2" maxlength="2" onkeypress="return validate(event,'##',this)" onpaste="validatePastedValue('##',this)" onblur="return validateValue('##',this)"  /></td>
											<td>-</td>
											<td><input type="text" name="TMNumA5" size="2" maxlength="2" onkeypress="return validate(event,'AA',this)" onpaste="validatePastedValue('AA',this, true);" onblur='if(validateValue("AA",this))makeUpperCase(this);' /></td>
											<td>-</td>
											<td><input type="text" name="TMNumA6" size="4" maxlength="4" onkeypress="return validate(event,'####',this)" onpaste="validatePastedValue('####',this)" onblur="return validateValue('####',this)"  /></td>
											<td>-</td>
											<td><input type="text" name="TMNumA7" size="2" maxlength="2" onkeypress="return validate(event,'AA',this)" onpaste="validatePastedValue('AA',this, true);" onblur='if(validateValue("AA",this))makeUpperCase(this);' /></td>
										</tr>
										<tr style="FONT-SIZE:10px">
											<td align="center" style="font-weight:bold">1-3</td>
											<td>&nbsp;</td>
											<td align="center" style="font-weight:bold">4</td>
											<td>&nbsp;</td>
											<td align="center">5</td>
											<td>&nbsp;</td>
											<td align="center" style="font-weight:bold">6-7</td>
											<td>&nbsp;</td>
											<td align="center">8-9</td>
											<td>&nbsp;</td>
											<td align="center" style="font-weight:bold">10-13</td>
											<td>&nbsp;</td>
											<td align="center">14-15</td>
										</tr>
									</table>
								</div>
								<div id="TaxMapCity" style="DISPLAY:none">
									<table border="0" cellspacing="0" cellpadding="0">
										<tr style="FONT-SIZE:10px; FONT-STYLE:italic">
											<td align="center" valign="bottom" colspan="3">MAP<br/>
												<img alt="ConBar.gif" src="image/ConBar.gif"></td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Double<br/>
												Circle</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Single<br/>
												Circle</td>
											<td>&nbsp;</td>
											<td align="center" valign="bottom">Lot</td>
											<td align="center" valign="bottom">Suffix</td>
										</tr>
										<tr>
											<td align="center">##</td>
											<td>&nbsp;</td>
											<td align="center">#</td>
											<td>&nbsp;</td>
											<td align="center">##</td>
											<td>&nbsp;</td>
											<td align="center">**</td>
											<td>&nbsp;</td>
											<td align="center">***</td>
											<td align="center">**</td>
											<td valign="top" rowspan="3">&nbsp; <a href="Cover_Sheet02.htm" target="help_frame" style="font-weight:bold; FONT-SIZE:10px; VERTICAL-ALIGN:middle; BORDER-TOP-STYLE:double; BORDER-RIGHT-STYLE:double; BORDER-LEFT-STYLE:double; TEXT-DECORATION:none; BORDER-BOTTOM-STYLE:double">
													&nbsp;?&nbsp;</a>
											</td>
										</tr>
										<tr>
											<td><input type="text" name="TMNumB1" size="2" maxlength="2" onkeypress="return validate(event,'##',this)" onblur="return validateValue('##',this)" /></td>
											<td>&nbsp;</td>
											<td><input type="text" name="TMNumB2" size="1" maxlength="1" onkeypress="return validate(event,'#',this)" onblur="return validateValue('#',this)" /></td>
											<td>&nbsp;</td>
											<td><input type="text" name="TMNumB3" size="2" maxlength="2" onkeypress="return validate(event,'##',this)" onblur="return validateValue('##',this)" /></td>
											<td>&nbsp;</td>
											<td><input type="text" name="TMNumB4" size="2" maxlength="2" onkeypress="return validate(event,'AA',this)" onblur="if(validateValue('AA',this))makeUpperCase(this);" onpaste="validatePastedValue('AA',this, true);" /></td>
											<td>&nbsp;</td>
											<td><input type="text" name="TMNumB5" size="3" maxlength="3" onkeypress="return validate(event,'AAA',this)" onblur="if(validateValue('AAA',this))makeUpperCase(this);" onpaste="validatePastedValue('AAA',this, true);" /></td>
											<td><input type="text" name="TMNumB6" size="2" maxlength="2" onkeypress="return validate(event,'AA',this)" onblur="if(validateValue('AA',this))makeUpperCase(this);" onpaste="validatePastedValue('AA',this, true);" /></td>
										</tr>
										<tr style="FONT-SIZE:10px">
											<td align="center" style="font-weight:bold">1-2</td>
											<td>&nbsp;</td>
											<td align="center" style="font-weight:bold">3</td>
											<td>&nbsp;</td>
											<td align="center" style="font-weight:bold">4-5</td>
											<td>&nbsp;</td>
											<td align="center">6-7</td>
											<td>&nbsp;</td>
											<td align="center" style="font-weight:bold">8-10</td>
											<td align="center">11-12</td>
										</tr>
									</table>
								</div>
							</td>
						</tr>
						<tr>
							<td valign="top">
								<select name="TMNumberList" size="3">
									<option>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
								</select>
								<br/>
								<br/>
							</td>
							<td valign="top">
								<input type="button" name="AddTax" value="Add Tax Map Number   " class="button" onclick="AddTaxMap()" /><br/>
								<input type="button" name="AddTax" value="Remove Tax Map Number" class="button" onclick="RemoveTaxMap()" /><br/>
								<input type="button" name="AddTax" value="Change Tax Map Number" class="button" onclick="ChangeTaxMap()" />
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td style="font-weight:bold;">Property Description</td>
				<td ><input type="text" name="PropertyDescription" size="51" maxlength="51" onkeypress='return validate(event,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this)' onpaste='validatePastedValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this, true);' onblur='if(validateValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this))makeUpperCase(this);' /></td>
			 </tr>
                <tr>
                    <td style="font-weight:bold;">Multiple Lots? </td>
					<td>
						<input type="checkbox" name="MultipleLots" />
					</td>
                </tr>
                <tr>
				<td style="font-weight:bold;">Return to Party:<br />
                    
				</td>
				
			</tr>
                <tr>
				<td style="font-weight:bold;">Name:</td>
				<td ><input type="text" name="ReturnToPartyName" size="51" maxlength="51" onkeypress='return validate(event,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this)' onpaste='validatePastedValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this, true);' onblur='if(validateValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this))makeUpperCase(this);' /></td>
			 </tr>
                <tr>
				<td style="font-weight:bold;">Address:</td>
				<td ><input type="text" name="ReturnToPartyAddress" size="51" maxlength="51" onkeypress='return validate(event,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this)' onpaste='validatePastedValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this, true);' onblur='if(validateValue("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", this))makeUpperCase(this);' /></td>
			 </tr>
			</table>
			<script type="text/javascript">
		    // Netscape has a problem with these events when in the above code
		    // so this is a work-around. If the table tag is removed the events work????
		    // Also, some fields (like "TitleCompany") work.

		    if (G_isNetscape) {
			    document.forms[0].Consideration.onkeypress=validateNS;
			    document.forms[0].ConsiderationPercentage.onkeypress=validateNS;
			    document.forms[0].AmountNotTaxed.onkeypress=validateNS;
			    document.forms[0].OriginalBook.onkeypress=validateNS;
			    document.forms[0].OriginalPage.onkeypress=validateNS;
			    document.forms[0].TitleCase.onkeypress=validateNS;

			    document.forms[0].DEMNumber1.onkeypress=validateNS;
			    document.forms[0].DEMNumber2.onkeypress=validateNS;
			    document.forms[0].DEMNumber3.onkeypress=validateNS;
			    document.forms[0].DEMNumber4.onkeypress=validateNS;
			    document.forms[0].DEMNumber5.onkeypress=validateNS;

			    document.forms[0].Names.onchange=upperCaseNS;
			    document.forms[0].TitleCompany.onchange=upperCaseNS;
			    document.forms[0].TitleCase.onchange=upperCaseNS;
			    document.forms[0].PropertyDescription.onchange=upperCaseNS;
			    document.forms[0].DEMNumber2.onchange=upperCaseNS;
			    document.forms[0].DEMNumber4.onchange=upperCaseNS;
			    document.forms[0].CodeSection.onchange=upperCaseNS;
		    }
			</script>
			<hr size="3" noshade="noshade" />
			<table border="0" width="100%" cellspacing="5" cellpadding="5">
				<tr>
					<td align="left" width="34%" style="white-space:nowrap;"><span style="font-weight:bold;"># of Pages </span><input type="text" name="Pages" size="3" maxlength="3" value="1" onkeypress='return validate(event,"###", this)' onpaste='validatePastedValue("###", this);' onblur="if(validateValue('###', this))setDefaultValue(this);" onchange='updateRange();' /><br/>
						<span style="font-size:0.63em">(8 1/2 x 14 or Smaller)</span></td>
					<td align="left" width="35%" style="white-space:nowrap;"><span style="font-weight:bold;">Oversized Plats </span><input type="text" name="Plats" size="2" maxlength="2" value="0" onkeypress='return validate(event,"##", this)' onchange='updateRange()'  onpaste='validatePastedValue("##", this);' onblur="if(validateValue('##', this))setDefaultValue(this)" /><br/>
						<span style="font-size:0.63em">(Larger than 8 1/2 x 14)</span>
                    </td>
					<td align="left" colspan="2" style="white-space:nowrap;"><span style="font-weight:bold;"># of Cover Sheet Pages </span><input type="text" name="Covers" size="2" maxlength="2" value="1" onkeypress='return validate(event,"##", this)' onchange='updateRange()' onpaste='validatePastedValue("##", this);' onblur="if(validateValue('##', this))setDefaultValue(this)" /><br/>&nbsp;
                    </td>
				</tr>
				<tr>
					<td align="left" valign="bottom" style="white-space:nowrap;"><span style="font-weight:bold;"># of Copies </span><input type="text" name="Copies" size="2" maxlength="2" value="0" onkeypress='return validate(event,"##", this)' onchange='updateRange(2)' onpaste='validatePastedValue("##", this);' onblur="if(validateValue('##', this))setDefaultValue(this)" /><br/>&nbsp;
                    </td>
					<td align="left" valign="bottom" style="white-space:nowrap;"><span style="font-weight:bold;"># of Oversized Plats Copies </span><input type="text" name="PlatsCopies" size="2" maxlength="2" value="0" onkeypress='return validate(event,"##", this)' onpaste='validatePastedValue("##", this);' onblur="if(validateValue('##', this))setDefaultValue(this)" /><br/>&nbsp;
                    </td>
					<td align="left" valign="bottom" style="white-space:nowrap;"><span style="font-weight:bold;">Certified</span> <input type="checkbox" name="Certified" onKeyUp='validateCertified(event)' onclick='validateCertified()' /><br/>
						&nbsp;
                    </td>
					<td align="left" valign="bottom" style="white-space:nowrap;"><span style="font-weight:bold;">Page Ranges </span><input type="text" name="PageRanges" size="11" maxlength="22" onkeypress='validateRange(event,"NNNNNNNNNNNNNNNNNNNN", this)'  onpaste='validatePastedValue("NNNNNNNNNNNNNNNNNNNN", this);' onblur="return validateValue('NNNNNNNNNNNNNNNNNNNN', this);" onchange='updateRange(1)' /><br/>&nbsp;
                    </td>
				</tr>
			</table>
			<table width="100%">
				<tr>
					<td align="center" width="50%">
						<input type="hidden" name="FeeFlag" id="FeeFlag" value="0" />
                        <input type="button" class="button" name="CreateBarCode" value="Create Barcode Page" onclick="createBarcode();" />
						<input type="button" class="button" name="CreateFee" value="Calculate Fee" onclick="calculateFee();" />
						<input type="reset" class="button" value="Clear" onclick="finishClear()" accesskey="c" />
                       
					</td>
				</tr>
			</table>
			<table width="100%">
				<tr>
					<td align="center">
						<FONT size="1">Disclaimer<br/>
							These fee calculations are intended to cover the typical cases. Call the Land 
							Records office for assistance with special cases. Fees subject to change 
							without notice.</FONT>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
