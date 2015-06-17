function validateKeyPushPin()
{
	if (document.getElementById("selKeyCategoryType").value == "value")
	{		
		document.getElementById("txtCategory1_1").disabled = false;
		document.getElementById("txtCategory1_2").disabled = true;
		document.getElementById("txtCategory2_1").disabled = false;
		document.getElementById("txtCategory2_2").disabled = true;
		document.getElementById("txtCategory3_1").disabled = false;
		document.getElementById("txtCategory3_2").disabled = true;
		document.getElementById("txtCategory4_1").disabled = false;
		document.getElementById("txtCategory4_2").disabled = true;
		document.getElementById("txtCategory5_1").disabled = false;
		document.getElementById("txtCategory5_2").disabled = true;
		document.getElementById("selCustomPP1Icon").disabled = false;
		document.getElementById("selCustomPP2Icon").disabled = false;
		document.getElementById("selCustomPP3Icon").disabled = false;
		document.getElementById("selCustomPP4Icon").disabled = false;
		document.getElementById("selCustomPP5Icon").disabled = false;		
	} else if (document.getElementById("selKeyCategoryType").value == "") {
		document.getElementById("txtCategory1_1").disabled = true;
		document.getElementById("txtCategory1_2").disabled = true;
		document.getElementById("txtCategory2_1").disabled = true;
		document.getElementById("txtCategory2_2").disabled = true;
		document.getElementById("txtCategory3_1").disabled = true;
		document.getElementById("txtCategory3_2").disabled = true;
		document.getElementById("txtCategory4_1").disabled = true;
		document.getElementById("txtCategory4_2").disabled = true;
		document.getElementById("txtCategory5_1").disabled = true;
		document.getElementById("txtCategory5_2").disabled = true;
		document.getElementById("selCustomPP1Icon").disabled = true;
		document.getElementById("selCustomPP2Icon").disabled = true;
		document.getElementById("selCustomPP3Icon").disabled = true;
		document.getElementById("selCustomPP4Icon").disabled = true;
		document.getElementById("selCustomPP5Icon").disabled = true;
	} else if (document.getElementById("selKeyCategoryType").value == "range"){
		document.getElementById("txtCategory1_1").disabled = false;
		document.getElementById("txtCategory1_2").disabled = false;
		document.getElementById("txtCategory2_1").disabled = false;
		document.getElementById("txtCategory2_2").disabled = false;
		document.getElementById("txtCategory3_1").disabled = false;
		document.getElementById("txtCategory3_2").disabled = false;
		document.getElementById("txtCategory4_1").disabled = false;
		document.getElementById("txtCategory4_2").disabled = false;
		document.getElementById("txtCategory5_1").disabled = false;
		document.getElementById("txtCategory5_2").disabled = false;
		document.getElementById("selCustomPP1Icon").disabled = false;
		document.getElementById("selCustomPP2Icon").disabled = false;
		document.getElementById("selCustomPP3Icon").disabled = false;
		document.getElementById("selCustomPP4Icon").disabled = false;
		document.getElementById("selCustomPP5Icon").disabled = false;
	}
}
function validateLocationSource()
{
	if (document.getElementById("selLocationSource").value == "LatLng") {
		document.getElementById("selLatitude").disabled = false;
		document.getElementById("selLongitude").disabled = false;
		document.getElementById("selAddressLine1").disabled = true;
		document.getElementById("selAddressLine2").disabled = true;
		document.getElementById("selCity").disabled = true;
		document.getElementById("selState").disabled = true;
		document.getElementById("selZip").disabled = true;
	} else if (document.getElementById("selLocationSource").value == "Address") {
		document.getElementById("selLatitude").disabled = true;
		document.getElementById("selLongitude").disabled = true;
		document.getElementById("selAddressLine1").disabled = false;
		document.getElementById("selAddressLine2").disabled = false;
		document.getElementById("selCity").disabled = false;
		document.getElementById("selState").disabled = false;
		document.getElementById("selZip").disabled = false;
	} else if (document.getElementById("selLocationSource").value == "Both") {
		document.getElementById("selLatitude").disabled = false;
		document.getElementById("selLongitude").disabled = false;
		document.getElementById("selAddressLine1").disabled = false;
		document.getElementById("selAddressLine2").disabled = false;
		document.getElementById("selCity").disabled = false;
		document.getElementById("selState").disabled = false;
		document.getElementById("selZip").disabled = false;
	} else if (document.getElementById("selLocationSource").value == "") {
		document.getElementById("selLatitude").disabled = true;
		document.getElementById("selLongitude").disabled = true;
		document.getElementById("selAddressLine1").disabled = true;
		document.getElementById("selAddressLine2").disabled = true;
		document.getElementById("selCity").disabled = true;
		document.getElementById("selState").disabled = true;
		document.getElementById("selZip").disabled = true;
	}
}
function validatePushPinTextSource()
{
	if (document.getElementById("selPushPinTextSource").value == "IndexedFields") {
		document.getElementById("selContentLine1").disabled = false;
		document.getElementById("txtContentLine1").disabled = false;
		document.getElementById("selContentLine2").disabled = false;
		document.getElementById("txtContentLine2").disabled = false;
		document.getElementById("selContentLine3").disabled = false;
		document.getElementById("txtContentLine3").disabled = false;		
		document.getElementById("selContentLine4").disabled = false;
		document.getElementById("txtContentLine4").disabled = false;
		document.getElementById("selContentLine5").disabled = false;
		document.getElementById("txtContentLine5").disabled = false;
		document.getElementById("selContentLine6").disabled = false;
		document.getElementById("txtContentLine6").disabled = false;
		document.getElementById("selContentLine7").disabled = false;
		document.getElementById("txtContentLine7").disabled = false;
		document.getElementById("selContentLine8").disabled = false;
		document.getElementById("txtContentLine8").disabled = false;
		document.getElementById("selContentLine9").disabled = false;
		document.getElementById("txtContentLine9").disabled = false;
		document.getElementById("selContentLine10").disabled = false;
		document.getElementById("txtContentLine10").disabled = false;
		document.getElementById("txtCustomContent").disabled = true;
		document.getElementById("txtContentUrl").disabled = true;
	} else if (document.getElementById("selPushPinTextSource").value == "CustomMessage") {
		document.getElementById("selContentLine1").disabled = true;
		document.getElementById("txtContentLine1").disabled = true;
		document.getElementById("selContentLine2").disabled = true;
		document.getElementById("txtContentLine2").disabled = true;
		document.getElementById("selContentLine3").disabled = true;
		document.getElementById("txtContentLine3").disabled = true;		
		document.getElementById("selContentLine4").disabled = true;
		document.getElementById("txtContentLine4").disabled = true;
		document.getElementById("selContentLine5").disabled = true;
		document.getElementById("txtContentLine5").disabled = true;
		document.getElementById("selContentLine6").disabled = true;
		document.getElementById("txtContentLine6").disabled = true;
		document.getElementById("selContentLine7").disabled = true;
		document.getElementById("txtContentLine7").disabled = true;
		document.getElementById("selContentLine8").disabled = true;
		document.getElementById("txtContentLine8").disabled = true;
		document.getElementById("selContentLine9").disabled = true;
		document.getElementById("txtContentLine9").disabled = true;
		document.getElementById("selContentLine10").disabled = true;
		document.getElementById("txtContentLine10").disabled = true;
		document.getElementById("txtCustomContent").disabled = false;
		document.getElementById("txtContentUrl").disabled = true;
	} else if (document.getElementById("selPushPinTextSource").value == "ExternalURL") {
		document.getElementById("selContentLine1").disabled = true;
		document.getElementById("txtContentLine1").disabled = true;
		document.getElementById("selContentLine2").disabled = true;
		document.getElementById("txtContentLine2").disabled = true;
		document.getElementById("selContentLine3").disabled = true;
		document.getElementById("txtContentLine3").disabled = true;		
		document.getElementById("selContentLine4").disabled = true;
		document.getElementById("txtContentLine4").disabled = true;
		document.getElementById("selContentLine5").disabled = true;
		document.getElementById("txtContentLine5").disabled = true;
		document.getElementById("selContentLine6").disabled = true;
		document.getElementById("txtContentLine6").disabled = true;
		document.getElementById("selContentLine7").disabled = true;
		document.getElementById("txtContentLine7").disabled = true;
		document.getElementById("selContentLine8").disabled = true;
		document.getElementById("txtContentLine8").disabled = true;
		document.getElementById("selContentLine9").disabled = true;
		document.getElementById("txtContentLine9").disabled = true;
		document.getElementById("selContentLine10").disabled = true;
		document.getElementById("txtContentLine10").disabled = true;
		document.getElementById("txtCustomContent").disabled = true;
		document.getElementById("txtContentUrl").disabled = false;
	} else if (document.getElementById("selPushPinTextSource").value == "") {
		document.getElementById("selContentLine1").disabled = true;
		document.getElementById("txtContentLine1").disabled = true;
		document.getElementById("selContentLine2").disabled = true;
		document.getElementById("txtContentLine2").disabled = true;
		document.getElementById("selContentLine3").disabled = true;
		document.getElementById("txtContentLine3").disabled = true;		
		document.getElementById("selContentLine4").disabled = true;
		document.getElementById("txtContentLine4").disabled = true;
		document.getElementById("selContentLine5").disabled = true;
		document.getElementById("txtContentLine5").disabled = true;
		document.getElementById("selContentLine6").disabled = true;
		document.getElementById("txtContentLine6").disabled = true;
		document.getElementById("selContentLine7").disabled = true;
		document.getElementById("txtContentLine7").disabled = true;
		document.getElementById("selContentLine8").disabled = true;
		document.getElementById("txtContentLine8").disabled = true;
		document.getElementById("selContentLine9").disabled = true;
		document.getElementById("txtContentLine9").disabled = true;
		document.getElementById("selContentLine10").disabled = true;
		document.getElementById("txtContentLine10").disabled = true;
		document.getElementById("txtCustomContent").disabled = true;
		document.getElementById("txtContentUrl").disabled = true;
	}
}
	
function checkListIcons() {
	if (document.getElementById("selDefaultIcon").value == "numberedIcons") {
		document.getElementById("selKeyColumn").disabled = true;
		document.getElementById("selKeyCategoryType").disabled = true;
		document.getElementById("txtCategory1_1").disabled = true;
		document.getElementById("txtCategory1_2").disabled = true;
		document.getElementById("txtCategory2_1").disabled = true;
		document.getElementById("txtCategory2_2").disabled = true;
		document.getElementById("txtCategory3_1").disabled = true;
		document.getElementById("txtCategory3_2").disabled = true;
		document.getElementById("txtCategory4_1").disabled = true;
		document.getElementById("txtCategory4_2").disabled = true;
		document.getElementById("txtCategory5_1").disabled = true;
		document.getElementById("txtCategory5_2").disabled = true;
		document.getElementById("selCustomPP1Icon").disabled = true;
		document.getElementById("selCustomPP2Icon").disabled = true;
		document.getElementById("selCustomPP3Icon").disabled = true;
		document.getElementById("selCustomPP4Icon").disabled = true;
		document.getElementById("selCustomPP5Icon").disabled = true;		
	}
	else{
		document.getElementById("selKeyColumn").disabled = false;
		document.getElementById("selKeyCategoryType").disabled = false;
		document.getElementById("txtCategory1_1").disabled = false;
		document.getElementById("txtCategory1_2").disabled = false;
		document.getElementById("txtCategory2_1").disabled = false;
		document.getElementById("txtCategory2_2").disabled = false;
		document.getElementById("txtCategory3_1").disabled = false;
		document.getElementById("txtCategory3_2").disabled = false;
		document.getElementById("txtCategory4_1").disabled = false;
		document.getElementById("txtCategory4_2").disabled = false;
		document.getElementById("txtCategory5_1").disabled = false;
		document.getElementById("txtCategory5_2").disabled = false;
		document.getElementById("selCustomPP1Icon").disabled = false;
		document.getElementById("selCustomPP2Icon").disabled = false;
		document.getElementById("selCustomPP3Icon").disabled = false;
		document.getElementById("selCustomPP4Icon").disabled = false;
		document.getElementById("selCustomPP5Icon").disabled = false;
	}	
}