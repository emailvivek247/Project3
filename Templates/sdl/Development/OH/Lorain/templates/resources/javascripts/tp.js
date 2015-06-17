var tp = [];
var tpl = [];
var activeTab;
function tpSet(i, c) {	
if (document.createElement) {
	var e = document.getElementById(i);
	var l = document.createElement('ul');
	var p = document.createElement('div');
	e.className = l.className = p.className = c;

	var a, j, t;
	for (j = 2; j < arguments.length; j++) {
		c = document.getElementById(t = arguments[j]);
		tp[t] = c.parentNode.removeChild(c);

		a = l.appendChild(document.createElement('li'));
		a.className = c.className;
		tpl[t] = a = a.appendChild(document.createElement('a'));
		a.setAttribute('href', 'javascript:tpShow(\''+i+'\', \''+t+'\');');
		a.appendChild(document.createTextNode(c.getAttribute('title')));
	}

	p.appendChild(tp[arguments[2]]);
	tpl[arguments[2]].className = 'active';

	while (e.firstChild !=null ) e.removeChild(e.firstChild);
	e.appendChild(l);
	e.appendChild(p);
	var cookiename= window.location.pathname;
    var tabcookie = get_cookie(cookiename);
    if (tabcookie){
    	if(!window.opera)
        tpShow(i,tabcookie);
    }
}}


function tpShow(e, p) {
	e = document.getElementById(e).lastChild;
	if(e && p!=null && "undefined" != p){
		var inActiveNode = e.replaceChild(tp[p], e.firstChild);
		if(inActiveNode){
			tpl[inActiveNode.getAttribute('id')].className = null;
		}
    	tpl[p].className = 'active';	
    	activeTab = p;
	}
}
function get_cookie(Name) { 
  var search = Name + "="
  var returnvalue = "";
  if (document.cookie.length > 0) {
	offset = document.cookie.indexOf(search)
	if (offset != -1) { 
		offset += search.length
		end = document.cookie.indexOf(";", offset);
		if (end == -1) end = document.cookie.length;
		returnvalue=unescape(document.cookie.substring(offset, end))
    }
  }
  return returnvalue;
}
function saveTabState(){	
	var cookiename= window.location.pathname
	var cookievalue= activeTab
	document.cookie=cookiename+"="+cookievalue
}
window.onunload=saveTabState