function toggleStartStopButton(idx) {
  if (flip == "Start") {
    flip = "Stop";
    $('#zone').html("<button class='dashboard' onclick='stopClient(\""+idx+"\");'>Stop Refreshing Log</button>");
  } else {
    flip = "Start";
    $('#zone').html("<button class='dashboard' onclick='startClient(\""+idx+"\");'>Start Refreshing Log</button>");
  }
}
var TimerID;
function startClient(idx) {
  $.post("showIndexLogOnly.do", { indexName: idx },function(data){$('#response').html(data);});
  TimerID = setTimeout('repeatClient("'+idx+'")', 2*1000);
  bar1.togglePause();
  toggleStartStopButton(idx);
}
function repeatClient(idx) {
  clearTimeout(TimerID);
  $.post("showIndexLogOnly.do", { indexName: idx },function(data){$('#response').html(data);});
  TimerID = setTimeout('repeatClient("'+idx+'")', 2*1000);
}
function stopClient(idx) {
  clearTimeout(TimerID);
  bar1.togglePause()
  toggleStartStopButton(idx);
}
