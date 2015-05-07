------------*************************ARLINGTON-OTC SPECIFIC SCRIPTS *******************************************---------------
/* TO BE RUN FOR NEW COUNTY INSTALLATION */
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

--- INSERT SITE INFORMATION ---

DECLARE @NODE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_NODE WHERE NAME = 'OTC-NODE')
INSERT INTO ECOMM_SITE (NAME, DESCRIPTION, ACTIVE, COUNTY, STATE, AUTOACTIVATE, TIMEZONE, NODE_ID, DATE_TIME_CREATED, DATE_TIME_MOD, 
	MOD_USER_ID, SUBSCRIPTION_VALIDATION_TEXT, ENABLE_MICRO_TX_OTC, ENABLE_MICRO_TX_WEB, NAME_ON_CHECK, CREATED_BY, CHECK_HOLD_PERIOD, SEARCH_DAY_THRESHOLD, USER_RETENTION_DAYS, ACH_HOLD_PERIOD)
VALUES ('ARLINGTON-OTC', 'Arlington OTC County', 'Y', 'Arlington', 'VA', 'Y', 'America/New_York', @NODE_ID, GETDATE(), GETDATE(), 'SYSTEM', 
	'', 'Y', 'Y', 'Arlington (OTC) County', 'SYSTEM', 3, 30, 30, 3)
GO

--- INSERT MERCHANT INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_MERCHANTINFO (SITE_ID, USERNAME, PASSWORD, VENDORNAME, ACTIVE, PARTNER, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, IS_MICROPAYMENT_ACCOUNT, CREATED_BY, TRAN_FEE_PERCENTAGE, TRAN_FEE_FLAT, TRAN_FEE_PERCENTAGE_AMEX, TRAN_FEE_FLAT_AMEX)
	VALUES (@SITE_ID, 'EcomArlingtonAPI', '7407F3A3ED787EC6A28E4A41606D6A2B559F9E58A37D53B3F08A57A95177A4E8A8152788B3ADA8B9622A8C267C9DB196', 'EcomArlington', 'Y', 'paypal', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'SYSTEM', '2.2', '0.30', '3.50', '0.00')
GO

DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_MERCHANTINFO (SITE_ID, USERNAME, PASSWORD, VENDORNAME, ACTIVE, PARTNER, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, IS_MICROPAYMENT_ACCOUNT, CREATED_BY, TRAN_FEE_PERCENTAGE, TRAN_FEE_FLAT, TRAN_FEE_PERCENTAGE_AMEX, TRAN_FEE_FLAT_AMEX)
	VALUES (@SITE_ID, 'EcomArlingtonMicroAPI', '036C6B4E3A77C2C59FB0807A7ECD1BD60DAAE6587D92302BB73E3A6B188429C22713241D37624A3B3DC48B0EA85C7126', 'EcomArlingtonMicro', 'Y', 'paypal', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM', '5.00', '0.05', '5.00', '0.05')
GO

--- INSERT MAGENSA INFORMATION ---
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_MAGENSAINFO (SITE_ID, HOST_ID, HOST_PASSWORD, REGISTEREDBY, ENCRYPTION_BLOCK_TYPE, CARD_TYPE, OUTPUT_FORMAT_CODE, ACTIVE, 
	DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SITE_ID, 'MAG745509733', '9B1BEC3A719CD0C650B39CF9F1458F651F02F0DB1C3E7C95B46422829BC79F8E2CB74CC9D16A0BECDB082CDFA8E26502', 'AMCAD', 1, 1, 101, 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

--- INSERT CREDIT USAGE FEE INFORMATION ---
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_CREDITUSAGE_FEE (SITE_ID, TX_FEE_FLAT, TX_FLAT_FEE_CUTOFF_AMT, TX_FEE_PERCENT, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, 
	ACTIVE, DOWNGRADE_FEE, TX_FEE_ADDITIONAL, MICRO_TX_CUT_OFF, CREATED_BY)
VALUES (@SITE_ID, '1.00', '25.00', '4.00', GETDATE(), GETDATE(), 'SYSTEM', 'Y', '5.00', '0.00', '8.33', 'SYSTEM')
GO

DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_WEBPAYMENT_FEE (SITE_ID, TX_FEE_FLAT, TX_FLAT_FEE_CUTOFF_AMT, TX_FEE_PERCENT, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, 
	ACTIVE, TX_FEE_ADDITIONAL, MICRO_TX_CUT_OFF, CREATED_BY)
VALUES (@SITE_ID, '1.00', '25.00', '4.00', GETDATE(), GETDATE(), 'SYSTEM', 'Y', '0.00', '8.33', 'SYSTEM')
GO

DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
DECLARE @TERM_TYPE_ID AS INT = (SELECT TOP 1 ID FROM AUTH_TERM_TYP WHERE TERM_TYP_CD = 'R')
INSERT INTO AUTH_TERMS (TERM_DESC, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, SITE_ID, TERM_TYP_ID, ACTIVE, CREATED_BY)
VALUES ('<b>Terms and Conditions</b><br /><br /><ol><li><u>Access to and use of the Arlington County Probate Court Services (hereinafter "Services") provided by the Arlington County Probate Court is subject to the terms and conditions of this User Agreement and all applicable laws and regulations. </u>This includes the laws and regulations governing copyright, trademark and other intellectual property as it may pertain to the property licensed to the County by AMCAD. For the purposes of this User Agreement, the Arlington County Probate shall be referred to as the "County", and you, the User, will be referred to as "You" (including the possessive "Your"), and the "User". </li><li><u>By reading this document and accessing the services, you accept without limitation or qualification, all of the terms and conditions in this user agreement.</u> You agree that your acceptance obligates you to pay for access to these Services. This includes an obligation to pay the charges incurred by third parties, whether they are your agents or otherwise, who access this Service through Your account. The County reserves the right to change these terms and conditions and the prices charged for Services at any time. Changes to the terms of this Agreement and charges for Services will only apply to future uses of the Services (i.e. a change to the charge for an individual image will apply to charges occurring after the change, while a change to the charge for subscription access will apply to the next billing cycle following the change.) Your continued use of this Site and these Services after the posting of updates to this Agreement, or the charges for Services will constitute your agreement to those terms or charges, as modified. The County additionally reserves the right to modify or discontinue, at any time, any Services, without notice or liability.<br /> <br /><u>LIABILITY FOR CHARGES INCURRED FOR ACCESS TO SERVICES.</u> By checking the "I agree to the  Terms of Use" box and accessing the Services, you agree to pay the charges established for these Services. The current charges applicable to the use of these Services can be seen by clicking on the Subscription menu. </li><li><u>OWNERSHIP AND PROPRIETARY RIGHTS.</u> All of the products and Services, including but not limited to text, data, maps, images, graphics, trademarks, logos and service marks (collectively, the "Content"), are owned by the County or licensed to the County by AMCAD, the owner of the Content. Although the County does not claim a copyright, trademark or other intellectual property interest in the Content, AMCAD reserves their copyright, trademark or other intellectual property interests in their property that is part of the Content. In connection with those products and Services, you agree to the following: text, data, maps, images, graphics, , trademarks, logos and service marks (collectively, the "Content"), are owned by the County or licensed to the County by third-parties who own the Content and the third party licensors'' property interests are protected by copyright, trademark and other intellectual property laws. In connection with those products and Services, you agree to the following: <ul><li>When accessing the Content, you may print a copy. If a printout and/or download is made, applicable third parties shall retain all rights in this material, and such a printout and/or download shall retain any copyright or other notices contained in that Content.</li><li>You will abide by restrictions set forth on the Site with respect to any of the Content. </li><li>You will not in any way violate the intellectual property laws protecting the third party licensors'' property interests in the Content. </li><li>You will not reuse, republish or otherwise distribute the Content or any modified or altered versions of it, whether over the Internet or otherwise, and whether or not for payment, without the express written permission of the copyright holder. </li><li>You will cooperate promptly and completely with any reasonable request by the County related to an investigation of infringement of copyright or other proprietary right of the third party licensor. </li><li>You agree that the material you are accessing contains the trade secrets and intellectual property of AMCAD, and you will cause irreparable harm to AMCAD if this material is used in violation of this agreement. </li></ul><li><u>INDEMNIFICATION:</u> You hereby agree to indemnify and hold harmless the County, and its respective officials, agencies, officers, subsidiaries, employees, licensors and agents, from and against any and all liability, loss, claims, damages, costs and/or actions (including attorneys'' fees) based upon or arising out of any breach by you or any third party of the obligations under this Agreement. Notwithstanding your indemnification obligation, the County reserves the right to defend any such claim and you agree to provide us with such reasonable cooperation and information as we may request. <br /> <br /><u>DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY:</u> The County makes every effort to update this information on a daily basis. Because Property information is continually changing, the County makes no expressed or implied warranty concerning the accuracy of this information.<ul><li>The information and services and products available to you may contain errors and are subject to periods of interruption. The County will do its best to maintain the information and services it offers. You agree that all use of these services is at your own risk, and that the County will not be held liable for any errors or omissions contained in the content of its services.<br /><br />The services are provided "as is" and the County expressly disclaims any and all warranties, express and implied, including but not limited to any warranties of accuracy, reliability, title, merchantability, non- infringement, fitness for a particular purpose or any other warranty, condition, guarantee or representation whether oral, in writing or in electronic form, including but not limited to the accuracy or completeness of any information contained therein or provided by the services. The County does not represent or warrant that access to the service will be interrupted or that there will be no failures, errors or omissions or loss of transmitted information. The information, documents and related graphics published on this server could include technical inaccuracies or typographical errors. Changes are periodically made to the information herein. The County may make improvements and/or changes in the services provided and/or the content described herein at any time.<br /><br />This disclaimer of liability applies to any damages or injury caused by any failure of performance error, omission, interruption, deletion, defect, delay in operation or transmission, computer viruses, communication line failure, theft or destruction or unauthorized access to alteration of or use of record, whether for breach of contract, tortious behavior, negligence or any other cause of action. No advice or information, whether oral or written, obtained by you from the County or through or from the service shall create any warranty not expressly stated in this agreement. If you are dissatisfied with the service, or any portion thereof, your exclusive remedy shall be to stop using the service.</li><li><u>MISCELLANEOUS:</u> The County has the right at any time to change or discontinue any aspect or feature of the services, including, but not limited to, content, hours of availability, and equipment needed for access or use. You must use the services for lawful purposes only. <br /><br />Any user conduct that restricts or inhibits any other person from using or enjoying the services will not be permitted. Uses such as data mining, screen scraping and the use of electronic BOTS for image download are prohibited. Users that are found to be employing the previously mentioned electronic methods may be restricted on this website. <br /><br />These terms and conditions shall be governed by and construed according to the laws of the State of Alabama, USA. and you agree to submit to the personal jurisdiction of the courts of the County of Arlington, State of Alabama. If any portion of these terms and conditions is deemed by a court to be invalid, the remaining provisions shall remain in full force and effect. You agree that regardless of any statute or law to the contrary, and claim or cause of action arising out of or related to the use of these services, must be filed within one year after such claim or cause of action arose.<br /><br />This is an offer to provide Services, and acceptance is expressly conditioned upon your acceptance of these terms and only these terms. Your acceptance of this Agreement is demonstrated by checking the box of "I agree to the Terms of Use" in the Registration form. This Agreement represents the entire agreement between you (the user) and the County. </li> </ul></li></ol>',
GETDATE(), GETDATE(), 'SYSTEM', @SITE_ID, @TERM_TYPE_ID, 'Y', 'SYSTEM')
GO

--- INSERT BANK DETAILS FOR ARLINGTON-OTC ---
DECLARE @HIGHESTSTARTCHECKNUM AS INT = ( SELECT
	MAX(START_CHECK_NUM)
FROM ECOMM_BANK_DETAILS) DECLARE @HIGHESTENDCHECKNUM AS INT = ( SELECT
	MAX(END_CHECK_NUM)
FROM ECOMM_BANK_DETAILS) SET @HIGHESTSTARTCHECKNUM = @HIGHESTSTARTCHECKNUM + 1000000
SET @HIGHESTENDCHECKNUM = @HIGHESTENDCHECKNUM + 1000000
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_BANK_DETAILS (SITE_ID, FROM_FNAME, FROM_LNAME, FROM_MINITIAL, FROM_ADDRLINE1, FROM_ADDRLINE2, FROM_CITY, FROM_STATE, FROM_ZIPCODE, FROM_PHONENUM,
								BANK_NAME, BANK_CODE, ROUTING_NUM, ACCOUNT_NUM, LAST_ISSUED_CHECK_NUM, START_CHECK_NUM, END_CHECK_NUM, BANK_ADDRLINE1, BANK_ADDRLINE2, BANK_CITY, BANK_STATE, BANK_ZIPCODE,
								DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY,BANK_BRANCH_NUMBER,BANK_BRANCH_NAME)
						VALUES (@SITE_ID, 'Granicus Inc.', '', '','600 Harrison Street, Suite 120','','San Francisco','CA','94107','','Comerica Bank',
						'90-3752/1211','121137522','1894935988',  @HIGHESTSTARTCHECKNUM, @HIGHESTSTARTCHECKNUM, @HIGHESTENDCHECKNUM, 
								NULL,NULL,NULL,NULL,NULL,GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM','948','San Jose Airport')
GO

DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO ECOMM_RECEIPT_CONFIGURATION (SITE_ID, BUSINESSNAME, ADDRESS_LINE_1, ADDRESS_LINE_2, CITY, STATE, ZIP, PHONE, COMMENTS_1, COMMENTS_2, 
TYPE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) 
VALUES (@SITE_ID, 'Granicus Inc.', '600 Harrison Street, ', 'Suite 120', 'San Francisco', 'CA','94107','7037377775','','','OTC',GETDATE(), GETDATE(), 'SYSTEM','Y', 'SYSTEM')
GO

DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC')
INSERT INTO [dbo].[ECOMM_CUSTOMER_BANK_DETAILS]
           ([SITE_ID]
           ,[ACCOUNTNAME]
           ,[ROUTING_NUM]
           ,[ACCOUNT_NUM]
           ,[BANK_ADDRLINE1]
           ,[BANK_ADDRLINE2]
           ,[BANK_CITY]
           ,[BANK_STATE]
           ,[BANK_ZIPCODE]
           ,[DATE_TIME_CREATED]
           ,[DATE_TIME_MOD]
           ,[MOD_USER_ID]
           ,[ACTIVE]
           ,[CREATED_BY])
     VALUES
           (@SITE_ID,
		   'Arlington County',
		   '051404260',
		   '0005139037741',
		   '223 Nash St W Fl 1',
		   '',
		   'Wilson',
		   'NC',		  
		   '27893',
		   GETDATE(),
		   GETDATE(),
		   'SYSTEM',
		   'Y',
		   'SYSTEM')
GO



UPDATE ECOMM_MERCHANTINFO SET VENDORNAME = 'Ecomarlington', PASSWORD = '353F2E265D8E73CC56DE37D8BB8BE8A03EEC61A4413B5B0797BEFF5C3703696229C7BC3EF989AA38EFE66ED75C8F078B'
WHERE SITE_ID IN (SELECT ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC') AND IS_MICROPAYMENT_ACCOUNT = 'N'
GO

UPDATE ECOMM_MERCHANTINFO SET VENDORNAME = 'EcomArlingtonMicro', PASSWORD = 'CB79D1A5452C8876B9938208B574795B0131BCBCD063A2644709D6F168FA4683959B984F5658FD3D2A27A45C73729B62'
WHERE SITE_ID IN (SELECT ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON-OTC') AND IS_MICROPAYMENT_ACCOUNT = 'Y'
GO