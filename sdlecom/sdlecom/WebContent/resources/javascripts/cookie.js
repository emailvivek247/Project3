function setCookie(name, value, days) {
  if (days) {
    var expire = new Date();
    expire.setTime(expire.getTime() + 3600000*24*days);
    document.cookie = name + "=" + escape(value) + "; expires=" + expire.toGMTString();
  } else {
    document.cookie = name + "=" + escape(value);
  }
}
function getCookie(name) {
  var dc = document.cookie;
  var prefix = name + "=";
  var begin = dc.indexOf("; " + prefix);
  if (begin == -1) {
    begin = dc.indexOf(prefix);
    if (begin != 0) return null;
  } else {
    begin += 2;
  }
  var end = dc.indexOf(";", begin);
  if (end == -1) end = dc.length;
  return unescape(dc.substring(begin + prefix.length, end));
}
function delCookie(name) {
  if (getCookie(name)) document.cookie = name + "=" + "; expires=Fri, 31-Dec-99 23:59:59 GMT";
}