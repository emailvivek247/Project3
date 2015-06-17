function validateTopChoiceCounts() {
	if (document.getElementById('getTopChoiceCounts').value =="" || document.getElementById('getTopChoiceCounts').value == 0){
		document.getElementById('getTopChoiceCounts').value = "20";             
	}
}
function renderChart() {
    var Data1 = "<set label='Jan' value='462' /><set label='Feb' value='857' /><set label='Mar' value='671' /><set label='Apr' value='494' /><set label='May' value='761' /><set label='Jun' value='960' /><set label='Jul' value='629' /><set label='Aug' value='622' /><set label='Sep' value='376' /><set label='Oct' value='494' /><set label='Nov' value='761' /><set label='Dec' value='960' />"
    var chartStr = "<chart";
    var chartHeight = "350";
    var chartWidth = "350";
    var contextPath = $('#contextPath').html();
    var chartType = contextPath + "/smartcharts/charts/" + document.getElementById('chartType').value;
    var ChartEnableDebugging = "0";
    var ChartEnableJavaScript = "0";

    if (document.getElementById('chartHeight').value !=""){
       //chartHeight = document.getElementById('chartHeight').value;               
    }
    if (document.getElementById('chartWidth').value !=""){
       //chartWidth = document.getElementById('chartWidth').value;
    }
    if (document.getElementById('ChartEnableDebugging').checked == true) {
        ChartEnableDebugging = "1";
    }
    else {
        ChartEnableDebugging = "0";
    }
    if (document.getElementById('ChartEnableJavaScript').checked == true) {
        ChartEnableJavaScript = "1";
    }
    else {
        ChartEnableJavaScript = "0";
    }

    chartStr = chartStr + " caption='" + document.getElementById('caption').value + "'";
    chartStr = chartStr + " subcaption='" + document.getElementById('subcaption').value + "'";
    chartStr = chartStr + " xAxisName='" + document.getElementById('xAxisName').value + "'";
    chartStr = chartStr + " yAxisName='" + document.getElementById('yAxisName').value + "'";

    chartStr = chartStr + " bgColor='" + document.getElementById('bgColor').value + "'";
    chartStr = chartStr + " bgAlpha='" + document.getElementById('bgAlpha').value + "'";

    chartStr = chartStr + " canvasbgColor='" + document.getElementById('canvasbgColor').value + "'";
    chartStr = chartStr + " canvasbgAlpha='" + document.getElementById('canvasbgAlpha').value + "'";

    if (document.getElementById('borderColor').value != "NONE") {
        chartStr = chartStr + " showBorder='1'";
        chartStr = chartStr + " borderColor='" + document.getElementById('borderColor').value + "'";
        chartStr = chartStr + " borderThickness='" + document.getElementById('borderThickness').value + "'";
        chartStr = chartStr + " borderAlpha='" + document.getElementById('borderAlpha').value + "'";
    }
    else {
        chartStr = chartStr + " showBorder='0'";
    }

    chartStr = chartStr + " baseFont='" + document.getElementById('baseFont').value + "'";
    chartStr = chartStr + " baseFontSize='" + document.getElementById('baseFontSize').value + "'";
    chartStr = chartStr + " baseFontColor='" + document.getElementById('baseFontColor').value + "'";

    if (document.getElementById('use3DLighting').checked == true) {
        chartStr = chartStr + " use3DLighting='1'";
    }
    else {
        chartStr = chartStr + " use3DLighting='0'";
    }

    if (document.getElementById('showPercentageValues').checked == true) {
        chartStr = chartStr + " showPercentageValues='1'";
    }
    else {
        chartStr = chartStr + " showPercentageValues='0'";
    }

    if (document.getElementById('animation').checked == true) {
        chartStr = chartStr + " animation='1'";
    }
    else {
        chartStr = chartStr + " animation='0'";
    }

    if (document.getElementById('showNames').checked == true) {
        chartStr = chartStr + " showNames='1'";
    }
    else {
        chartStr = chartStr + " showNames='0'";
    }

    if (document.getElementById('showValues').checked == true) {
        chartStr = chartStr + " showValues='1'";
    }
    else {
        chartStr = chartStr + " showValues='0'";
    }    

    if (document.getElementById('rotatenames').checked == true) {
        chartStr = chartStr + " rotatenames='1'";
    }
    else {
        chartStr = chartStr + " rotatenames='0'";
    }

    if (document.getElementById('showToolTip').checked == true) {
        chartStr = chartStr + " showToolTip='1'";
    }
    else {
        chartStr = chartStr + " showToolTip='0'";
    }

    if (document.getElementById('toolTipSepChar').value != "") {
        chartStr = chartStr + " toolTipSepChar='" + document.getElementById('toolTipSepChar').value + "'";
    }
    else {
        chartStr = chartStr + " toolTipSepChar=','";
    }

    if (document.getElementById('numberPrefix').value != "") {
        chartStr = chartStr + " numberPrefix='" + document.getElementById('numberPrefix').value + "'";
    }
    else {
        chartStr = chartStr + " numberPrefix=''";
    }
    
    chartStr = chartStr + " decimals='" + document.getElementById('decimals').value + "'";

    if (document.getElementById('forceDecimals').checked == true) {
        chartStr = chartStr + " forceDecimals='1'";
    }
    else {
        chartStr = chartStr + " forceDecimals='0'";
    }
    
    if (document.getElementById('setAdaptiveYMin').checked == true) {
        chartStr = chartStr + " setAdaptiveYMin='1'";
    }
    else {
        chartStr = chartStr + " setAdaptiveYMin='0'";
    }

    if (document.getElementById('logoURL').value != "") {
        chartStr = chartStr + " logoURL='" + document.getElementById('logoURL').value + "'";
        chartStr = chartStr + " logoPosition='" + document.getElementById('logoPosition').value + "'";
        chartStr = chartStr + " logoAlpha='" + document.getElementById('logoAlpha').value + "'";
    }
    else {
        chartStr = chartStr + " logoURL=''";
    }

    if (document.getElementById('logoURL').value != "" && document.getElementById('logoLink').value != "") {
        chartStr = chartStr + " logoLink='" + document.getElementById('logoLink').value + "'";
    }
    else {
        chartStr = chartStr + " logoLink=''";
    }

    chartStr = chartStr + ">" + Data1 + "</chart>";

    var chart = new SmartCharts(chartType, "ChartId", chartWidth, chartHeight, ChartEnableDebugging, ChartEnableJavaScript);
    chart.setDataXML(chartStr);
    chart.render('ChartDiv');
}