function countSelects() {
  var selects = document.getElementsByName('indexSelect');
  var count = 0;
  for (i = 0; i < selects.length; i++) {
    if (selects[i].checked) {
      count++;
      if (count == 1) {
        document.zs.indexName.value = selects[i].value;
      }else{
        document.zs.indexName.value = document.zs.indexName.value + "," + selects[i].value;
      }
    }
  }
  return count;
}

function clickDelete() {
  if (countSelects() == 0){
	alert("Please select at least one index and try again");	
	return;
  }
  if (countSelects() < 1 || !confirm("Are you sure you want to delete the selected index(es)?")) {
    return;
  }  
  document.frmIndexAction.action.value = "delete";
  document.frmIndexAction.submit();
}
function clickCopy() {	
  if (countSelects() == 0){
	alert("Please select at least one index and try again");	
	return;
  } else if (countSelects() > 1){
    alert("You can only copy one index at a time!");
    return;
  } else {
	  var txt = 'New Index Name:<input type="text" id=\"alertName\";name=\"myname\" value=\"\" />';
	  $.prompt(txt,{submit: mysubmitfunc,	buttons: { Ok: true }});
  }
}


function mysubmitfunc(e,v,m,f){
	an = m.children('#alertName');	
	document.frmIndexAction.indexNameToCopy.value = $(an).val();
	if(document.frmIndexAction.indexNameToCopy.value!=null){
	  document.frmIndexAction.action.value = "copy";
	  document.frmIndexAction.submit();
	}
}

function clickDownload() {
  if (countSelects() < 1 || !confirm("Are you sure you want to download the selected index(es) and related template files?")) {
    return;
  }
  document.downloadForm.indexesToDownload.value = document.zs.indexName.value;
  document.downloadForm.submit();
  //document.f.action = "goDashboard.do";
}

function clickSearch() {
  var count = countSelects();
  if (count == 1) {
    return true;
  } else if (count == 0) {
    alert("Please select an index to search.");
  } else if (count > 1) {
    document.zs.action = "multiSearch.do";
    //change to this if you want to use the Velocity version
    //document.zs.action = "multiSearchVm.do";
    return true;
  }
  return false;
}

function jumptolink(what){	
	var temp = "#" + what + " option:selected";
	var selectedOptVal = $(temp).val();
	var selectedOptText = $(temp).text();
	var target = $(temp).attr("title");
	if(selectedOptVal==null || selectedOptVal==''){ alert('Empty Selection'); return; }
	if(! confirm('Please Confirm To :'+selectedOptText)) return;
	if (document.getElementById && target=="newwin")
	window.open(selectedOptVal);
	else
	window.location=selectedOptVal;
}