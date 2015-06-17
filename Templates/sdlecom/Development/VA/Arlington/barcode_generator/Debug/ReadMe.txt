Config Comments:


	middlware server location used for remoting
	<add key="AiLISMiddlewareServer" value="tcp://NVAPP2:13010/"/>
	---------------------------------------------------------------------------------------------------------
	Special override; folder where the code looks for a trigger file dropped by scanning. 
	This value override what is set in DB 
	Select value from cfg_elements where SUBMODULE = 'GLOBAL' 
	AND CONTROL_NAME = 'COUNTYSETTINGS' AND ELEMENT_NAME = 'AUTOINDEXINGPATH'
    <add key="fsw_override" value="\\vm-app1\DataCache\"/>			
    ---------------------------------------------------------------------------------------------------------
    Will set the legal extract on or off. use 'TRUE' 'FALSE' to set value
    <add key="ExtractLegal" value="TRUE"/>
	---------------------------------------------------------------------------------------------------------



    Following _Override config values will override what is set in code as default.
    ---------------------------------------------------------------------------------------------------------
    Set the trigger file extension
    <add key="TriggerFileExtension_Override" value="AUTO"/> 
    ---------------------------------------------------------------------------------------------------------
    User that shows up in document workflow. AiLIS logon user account used by the service to get document and lock it
    <add key="AiLISExtractUser_Override" value="AUTOINDEX"/>
    ---------------------------------------------------------------------------------------------------------
    Service impersonates this machine name to obtain document and log in workflow when extracting Barcode
    <add key="AiLISExtractMachine_Override" value="amcadjvh2"/>
    ---------------------------------------------------------------------------------------------------------
    Service impersonates this Application name to obtain document and log in workflow when extracting Barcode
    <add key="AiLISExtractApp_Override" value="AI"/>
    ---------------------------------------------------------------------------------------------------------
    Temporarily copies images ot this location for processing. If left empty uses the 'TempStorage' folder under the application folder
    <add key="TempStorage_Override" value=""/>
    ---------------------------------------------------------------------------------------------------------
    Barcode type to be extractor
    <add key="BarcodeType_Override" value="Pdf417"/>
    ---------------------------------------------------------------------------------------------------------
    'A' makes the service lock documents when processing, any other value would use getreadonly document
    <add key="ProcessMode_Override" value="A"/>
    ---------------------------------------------------------------------------------------------------------