DECLARE @NODE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_NODE
WHERE NAME = 'RECORDSMANAGEMENT')
INSERT INTO ECOMM_SITE (NAME, DESCRIPTION, ACTIVE, COUNTY, STATE, AUTOACTIVATE, TIMEZONE, NODE_ID, DATE_TIME_CREATED, DATE_TIME_MOD,
MOD_USER_ID, SUBSCRIPTION_VALIDATION_TEXT, ENABLE_MICRO_TX_OTC, ENABLE_MICRO_TX_WEB, NAME_ON_CHECK, CREATED_BY, CHECK_HOLD_PERIOD, SEARCH_DAY_THRESHOLD,
USER_RETENTION_DAYS, ACH_HOLD_PERIOD, IS_FIRM_NUMBER_REQUIRED, IS_BAR_NUMBER_REQUIRED, IS_FREE_SITE)
	VALUES ('ARLINGTON', 'Arlington County', 'Y', 'Arlington', 'VA', 'Y', 'America/New_York', @NODE_ID, GETDATE(), GETDATE(), 'SYSTEM',
	'You can choose only one of the available subscriptions', 'Y', 'Y', 'Arlington County', 'SYSTEM', 3, 30, 30, 3,  'N', 'N', 'N')
GO

DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO SITE_CONFIGURATION (SITE_ID, EMAIL_TEMPLATE_FOLDER, FROM_EMAIL_ADDRESS, PAYMENT_CONF_SUB, CHANGE_SUBSCRIPTION_SUB, CANCEL_SUBSCRIPTION_SUB,
REACTIVATE_SUBSCRIPTION_SUB, RECURRING_PAYMENT_SUCCESS_SUB, RECURRING_PAYMENT_UNSUCCESSFUL_SUB, PAYMENT_CONF_TEMPLATE, CHANGE_SUBSCRIPTION_TEMPLATE,
CANCEL_SUBSCRIPTION_TEMPLATE, REACTIVATE_CANCELLED_SUBSCRIPTION_TEMPLATE, RECURRING_PAYMENT_SUCCESS_TEMPLATE, RECURRING_PAYMENT_UNSUCCESSFUL_TEMPLATE,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, WEB_PAYMENT_CONFIRMATION_SUB, WEB_PAYMENT_CONFIRMATION_TEMPLATE, REMOVE_SUBSCRIPTION_SUB,
REMOVE_SUBSCRIPTION_TEMPLATE, CREATED_BY, ACCESS_AUTHORIZATION_SUB, ACCESS_AUTHORIZATION_TEMPLATE, PAYASUGO_PAYMENT_CONFIRMATION_SUB, PAYASUGO_PAYMENT_CONFIRMATION_TEMPLATE, ADD_SUBSCRIPTION_SUB)
	VALUES (@SITE_ID, 'RecordsManagement/Arlington/', 'noReply@amcad.com', 'Payment Confirmation - Roam Arlington Records Management', 'Subscription Change Notification - Roam Arlington Records Management',
	'Cancelled Subscription Notification - Roam Arlington Records Management', 'Subscription Reactivated - Roam Arlington Records Management', 'Recurring Payment Posted Successfully - Roam Arlington Records Management',
	'Recurring Payment Failed - Roam Arlington Records Management', 'paymentConfirmation.stl', 'changeConfirmation.stl', 'cancelConfirmation.stl', 'cancelConfirmation.stl', 'recurringPaymentSuccessfulTemplate.stl',
	'recurringPaymentUnsuccessfulTemplate.stl', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'Web Purchase Confirmation - Roam Arlington Records Management', 'webPaymentConfirmation.stl', 'Subscription Removed - Roam Arlington Records Management',
	'removeSubscription.stl', 'SYSTEM', 'Access Authorization - Roam Arlington Records Management', 'accessAuthorization.stl', 'Pay As You Go Purchase Confirmation - ROAM Arlington Case Management', 'payAsYouGoPaymentConfirmation.stl',
	'Subscription Add Notification - Arlington Records Management')
GO

DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO ECOMM_MERCHANTINFO (SITE_ID, USERNAME, PASSWORD, VENDORNAME, ACTIVE, PARTNER, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, IS_MICROPAYMENT_ACCOUNT, CREATED_BY, TRAN_FEE_PERCENTAGE, TRAN_FEE_FLAT, TRAN_FEE_PERCENTAGE_AMEX, TRAN_FEE_FLAT_AMEX)
	VALUES (@SITE_ID, 'EcomArlingtonAPI', '7407F3A3ED787EC6A28E4A41606D6A2B559F9E58A37D53B3F08A57A95177A4E8A8152788B3ADA8B9622A8C267C9DB196', 'EcomArlington', 'Y', 'paypal', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'SYSTEM', '2.2', '0.30', '3.50', '0.00')
GO

DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO ECOMM_MERCHANTINFO (SITE_ID, USERNAME, PASSWORD, VENDORNAME, ACTIVE, PARTNER, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, IS_MICROPAYMENT_ACCOUNT, CREATED_BY, TRAN_FEE_PERCENTAGE, TRAN_FEE_FLAT, TRAN_FEE_PERCENTAGE_AMEX, TRAN_FEE_FLAT_AMEX)
	VALUES (@SITE_ID, 'EcomArlingtonMicroAPI', '036C6B4E3A77C2C59FB0807A7ECD1BD60DAAE6587D92302BB73E3A6B188429C22713241D37624A3B3DC48B0EA85C7126', 'EcomArlingtonMicro', 'Y', 'paypal', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM', '5.00', '0.05', '5.00', '0.05')
GO

--- INSERT MAGENSA INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO ECOMM_MAGENSAINFO (SITE_ID, HOST_ID, HOST_PASSWORD, REGISTEREDBY, ENCRYPTION_BLOCK_TYPE, CARD_TYPE, OUTPUT_FORMAT_CODE, ACTIVE,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
	VALUES (@SITE_ID, 'MAG834109743', '1E8796D56A57F1C89EE956E75E700500AAAE14AFA4EDE6999105B0F53A86AA18', 'AMCAD', 1, 1, 101, 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

--- INSERT CREDIT USAGE FEE INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO ECOMM_CREDITUSAGE_FEE (SITE_ID, TX_FEE_FLAT, TX_FLAT_FEE_CUTOFF_AMT, TX_FEE_PERCENT, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID,
ACTIVE, DOWNGRADE_FEE, TX_FEE_ADDITIONAL, MICRO_TX_CUT_OFF, CREATED_BY)
	VALUES (@SITE_ID, '5.00', '60.00', '5.00', GETDATE(), GETDATE(), 'SYSTEM', 'Y', '5.00', '0.00', '8.33', 'SYSTEM')
GO

--- INSERT WEB PAYMENT FEE INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO ECOMM_WEBPAYMENT_FEE (SITE_ID, TX_FEE_FLAT, TX_FLAT_FEE_CUTOFF_AMT, TX_FEE_PERCENT, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID,
ACTIVE, TX_FEE_ADDITIONAL, MICRO_TX_CUT_OFF, CREATED_BY)
	VALUES (@SITE_ID, '5.00', '60.00', '5.00', GETDATE(), GETDATE(), 'SYSTEM', 'Y', '0.00', '8.33', 'SYSTEM')
GO

--- INSERT TERM INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
DECLARE @TERM_TYPE_ID AS INT = ( SELECT TOP 1
	ID
FROM AUTH_TERM_TYP
WHERE TERM_TYP_CD = 'R')
INSERT INTO AUTH_TERMS (TERM_DESC, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, SITE_ID, TERM_TYP_ID, ACTIVE, CREATED_BY)
	VALUES ('<b>Terms and Conditions</b><br/><br/><ol><li><u>Access to and use of the eAccess website (hereinafter "Services") provided for the Arlington County Circuit
	Court by Granicus, is subject to the terms and conditions of this User Agreement, the Subscriber Agreement for secure Remote Access for Arlington County Land Records and all applicable laws and regulations.</u>
	This includes the laws and regulations governing copyright, trademark and other intellectual property as it may pertain to the property licensed to the Court
	by Granicus. For the purposes of this User Agreement, the Arlington County Circuit Court shall be referred to as the "Court," and you, the User, will be referred
	to as "You" (including the possessive "Your"), and the "User."</li><br/><li><u>By reading this document and accessing the services, you accept without
	limitation or qualification, all the terms and conditions in this user agreement.</u> You agree that your acceptance obligates you to pay for access to
	these Services where applicable. This includes an obligation to pay the charges incurred by third parties, whether they are your agents or otherwise,
	who access this Service through your account. The Court reserves the right to change these terms and conditions and the prices charged for Services at
	any time. Changes to the terms of this Agreement and charges for Services will only apply to future use of the Services (i.e. a change to the charge for
	an individual image will apply to charges occurring after the change, while a change to the charge for subscription access will apply to the next billing
	cycle following the change). You may be required to affirmatively accept the revised Terms of Use in order to continue using the Services
	OR your continued use of this Site and these Services after the posting of updates to the Terms of Use or updates to the charges for
	Services will constitute your agreement to those terms or charges, as modified. The Court additionally reserves the right to modify or
	 discontinue, at any time, any Services, without notice or liability. You agree that Granicus or the Court may provide notice of changes to
	 Terms or charges to you by posting it on the Site or by emailing it to the email address provided in the user profile.</li><br/><li><u>LIABILITY FOR
	 CHARGES INCURRED FOR ACCESS TO SERVICES:</u> By checking the "I agree to the Terms of Use" box and accessing the Services, you agree to pay
	 the charges established for these Services. The current charges applicable to the use of these Services can be seen by clicking on the Subscription menu.
	 Payment for charges incurred for accessing Products and Services must be paid with a valid credit card or by check. Acceptable types of credit card payment are Visa®,
	 MasterCard®, American Express® and Discover®. All payments are due prior to activation of a paid account. Time of purchase is deemed as "at checkout" for a Pay As You Go subscription.
	 For recurring subscriptions, payment is due at time of subscription purchase and on each subsequent monthly anniversary date.<br/><br/></li><li><u>SUBSCRIPTION CANCELLATION: Recurring subscriptions auto renew on the monthly
	 anniversary date. The user can cancel the subscription at any point. The cancellation will take effect at the close of the current subscription month.
	 Cancellation will not prorate account payment or lead  to payment refund. The Court may cancel your subscription at any time due to failure to comply with these Terms and Conditions.</u></li><br/><li><u>PRIVACY:</u> By checking the "I agree to Terms of Use" box, the user agrees to provide personal information at account registration. The Court confirms that the sole purpose for collecting personal information is for
	 user verification. Personal Information will not be disseminated to third parties. Personal information includes:<ul><li>User First Name</li><li>User Last Name</li><li>Email Address</li><li>Bar Number (where applicable)</li><li>Firm Name (where applicable)</li><li>User Phone Number</li><li>Credit Card Information</li></ul><br/></li><li><u>OWNERSHIP AND PROPRIETARY RIGHTS:</u> All the Products and Services, including but not limited to text, data, maps, images, graphics, trademarks, logos and service marks (collectively, the "Content"), are owned by the Court or licensed to the Court by Granicus, the owners of the Content. The Court and Granicus reserve their copyright, trademark or other intellectual property interests in their property that is part of the Content. In connection with those products and Services, you agree to the following: text, data, maps, images, graphics, trademarks, logos and service marks (collectively, the "Content"), are owned by the Court or licensed to the Court by third-parties who own the Content and the third party licensors property interests are protected by copyright, trademark and other intellectual property laws. In connection WITH those products and Services, you agree to the following:<br/><ul><li>When accessing the Content, you may PRINT a copy. If a printout and/or download is made, applicable third parties shall retain all rights in this material, and such a printout and/or download shall retain any copyright or other notices contained in that Content.</li><li>You will abide by restrictions SET forth on the Site with respect to any of the Content.</li><li>You will not in any way violate the intellectual property laws protecting the third party licensors property interests in the Content.</li><li>You agree that the material you are accessing contains the trade secrets and intellectual property of Granicus and you will cause irreparable harm to Granicus if this material is used in violation of this agreement.</li><li>You will not reuse, republish or otherwise distribute the trade secrets and intellectual property of Granicus or any modified or altered versions of it, whether over the Internet or otherwise, and whether or not for payment in competition to the Services provided by Granicus or the Court without the express written permission of the copyright holder.</li><li>You will cooperate promptly and completely with any reasonable request by the Court related to an investigation of infringement of copyright or other proprietary right of the third party licensor.</li></ul></li><br/><li><u> INDEMNIFICATION:</u> You hereby agree to indemnify and hold harmless the Court, and its respective officials, agencies, officers, subsidiaries, employees, licensors and agents, from and against any and all liability, loss, claims, damages, costs and/or actions (including attorneys fees) based upon or arising out of any breach by you or any third party of the obligations under this Agreement. Notwithstanding your indemnification obligation, the Court reserves the right to defend any such claim and you agree to provide the Court with such reasonable cooperation and information as the Court may request.</li><br/><li><u>LIMITATIONS OF LIABILITY:</u>You hereby relieve and release the Court, including but not limited to the Clerk of the Court, his deputy clerks, employees and agents from liability for any and all damages resulting from:</br></br></br><ol style="type:a;"><li>use of Secure Remote Access;</li><li>interrupted service of any kind;</li><li>incorrect data;</li><li>missing or misfiled documents; or</li><li>any other information or misinformation accessed by Subscriber through Secure Remote Access.</li></ol></br>You further agree that the Court, including but not limited to the Clerk of the Court, his deputy clerks, employees and agents shall not be liable for negligence or lost profits resulting from any claim or demand against the Subscriber or any other party, or for any consequential damages even if advised of the possibility of such damages.</br>You further relieve and release the County of Arlington, Virginia, its County Board, County Manager, employees and agents, and the Office of the Executive Secretary, Supreme Court of Virginia and its employees and agents from liability for any and all damages resulting from:</br><ol style="type:a;"><li>use of Secure Remote Access;</li><li>interrupted service of any kind;</li><li>incorrect data;</li><li>missing or misfiled documents; or</li><li>any other information or misinformation accessed by Subscriber through Secure Remote Access.</li></ol></br>You further agree that the County of Arlington, Virginia, its County Board, County Manager, employees and agents, and the Office of the Executive Secretary, Supreme Court of Virginia, shall not be liable for negligence or lost profits resulting from any claim or demand against the Subscriber or any other party.</br></br>The information or data accessed by you may or may not be the official government record required by law.  In order to ensure the accuracy of the data or information, you should consult the official government record.</br></br>Nothing in these Terms and Conditions shall be construed as waiving the sovereign or governmental immunity of the Clerk of the Arlington County Circuit Court, its employees, or agents, or that of the Office of the Executive Secretary, Supreme Court of Virginia.</li><br/><li><u>MISCELLANEOUS:</u><br/><br/><b>Continuity of Service</b><br/>The Court has the right at any time to change or discontinue any aspect or feature of the services, including, but not limited to, content, hours of availability, and equipment needed for access or USE.<br/><br/><b>Obligation to USE Services Within 30 Days</b><br/>User accounts that are "inactive", meaning an account that has been OPEN more than 30 days AND that has never had any account transactions, will be placed in "archived" status. Archived accounts cannot be removed from archived status and will require the user to create a new user account.<br/><br/><b>Unlawful USE</b><br/>You must USE the services for lawful purposes only. Any user conduct that restricts or inhibits any other person from using or enjoying the services will not be permitted. Uses such as data mining, screen scraping and the  USE of electronic BOTS for image download are prohibited. Users that are found to be employing the previously mentioned electronic methods may be restricted on this website.<br/><br/>The USE of deception to access Products or Services could result in legal action by the Court and/or the immediate cancellation of the account without refund.<br/><br/><b>Resale of Data</b><br/>The resale of the Courts BULK data is prohibited unless made in compliance with the Arlington Code of Judicial Administration section 1-605. Any unauthorized reselling of information may result in legal action by the Court and/or immediate cancellation of your account without refund.<br/><br/><b>Applicable Law</b><br/>These terms and conditions shall be governed by and construed according to the laws of the State of Arlington, USA, and you agree to submit to the personal jurisdiction of the courts of the State of Arlington. If any portion of these terms and conditions is deemed by a court to be invalid, the remaining provisions shall remain in full force and effect. You agree that any claim or cause of action arising out of or related to the USE of these Services must be presented to the Administrative Director of the Administrative Office of the Courts as a Notice of Claim with in accordance with A.R.S. §12-821.01. within one hundred and eighty days after such claim or cause of action arose.<br/><br/><b>Entire Agreement</b><br/>This is an offer to provide Services, and acceptance is expressly conditioned upon your acceptance of these terms and only these terms. Your acceptance of this Agreement is demonstrated by checking the box of "I agree to the Terms of Use" in the Registration form. This Agreement represents the entire agreement between you (the user) and the Court.',	
	GETDATE(), GETDATE(), 'SYSTEM', @SITE_ID, @TERM_TYPE_ID, 'Y', 'SYSTEM')
GO


INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED
,IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('ARLINGTON_IMAGE_ACCESS', 'Arlington Image Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 'Access to Arlington Land Record along with Image access. The subscription fee is $50 per month until cancelled, but non refundable for the paid subscription period.', 'N', '1', 'SYSTEM', 'Y', 'Y', '1', 'N','0' ,'N')
INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED,
IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('ARLINGTON_FREE_ACCESS', 'Arlington Free Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 'Access to Arlington Records Management. This does not include access to the images. Please choose the Premium subscription to get access to the images.', 'N', '2', 'SYSTEM', 'Y', 'Y', '1', 'N','0' ,'N')
INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED,
IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('ARLINGTONGOV_FREE_ACCESS', 'Arlington County Government Free Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 'Access to Arlington Records Management.  Only Arlington County Government users should be able to sign up for this access', 'N', '3', 'SYSTEM', 'Y', 'Y', '1', 'N','0' ,'N')
GO

DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTON_IMAGE_ACCESS')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTON_FREE_ACCESS')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTONGOV_FREE_ACCESS')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTON_IMAGE_ACCESS'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '50.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO 

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'FREE')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTON_FREE_ACCESS'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0', -1, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')	
SET @SITEACCESS_ID = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTONGOV_FREE_ACCESS'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0', -1, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @HIGHESTSTARTCHECKNUM AS INT = ( SELECT
	MAX(START_CHECK_NUM)
FROM ECOMM_BANK_DETAILS) DECLARE @HIGHESTENDCHECKNUM AS INT = ( SELECT
	MAX(END_CHECK_NUM)
FROM ECOMM_BANK_DETAILS) SET @HIGHESTSTARTCHECKNUM = @HIGHESTSTARTCHECKNUM + 1000000
SET @HIGHESTENDCHECKNUM = @HIGHESTENDCHECKNUM + 1000000
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'ARLINGTON')
INSERT INTO ECOMM_BANK_DETAILS (SITE_ID, FROM_FNAME, FROM_LNAME, FROM_MINITIAL, FROM_ADDRLINE1, FROM_ADDRLINE2, FROM_CITY, FROM_STATE, FROM_ZIPCODE, FROM_PHONENUM,
BANK_NAME, BANK_CODE, ROUTING_NUM, ACCOUNT_NUM, LAST_ISSUED_CHECK_NUM, START_CHECK_NUM, END_CHECK_NUM, BANK_ADDRLINE1, BANK_ADDRLINE2, BANK_CITY, BANK_STATE, BANK_ZIPCODE,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SITE_ID, 'AMCAD', '', '', '15867 N Mountain Road', '', 'Broadway', 'VA', '22815', '', 'Capital Bank', '65-334/550', '055003340', '111989111',
	 @HIGHESTSTARTCHECKNUM, @HIGHESTSTARTCHECKNUM, @HIGHESTENDCHECKNUM, NULL, NULL, NULL, NULL, NULL, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'ARLINGTON_IMAGE_ACCESS')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
            VALUES(@ACCESS_ID, 0.65, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')	
GO


DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON')
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

UPDATE ECOMM_MERCHANTINFO SET VENDORNAME = 'EcomArlingtonMicro', PASSWORD = 'CB79D1A5452C8876B9938208B574795B0131BCBCD063A2644709D6F168FA4683959B984F5658FD3D2A27A45C73729B62'
WHERE SITE_ID IN (SELECT ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON') AND IS_MICROPAYMENT_ACCOUNT = 'Y'
GO

UPDATE ECOMM_MERCHANTINFO SET VENDORNAME = 'Ecomarlington', PASSWORD = '353F2E265D8E73CC56DE37D8BB8BE8A03EEC61A4413B5B0797BEFF5C3703696229C7BC3EF989AA38EFE66ED75C8F078B'
WHERE SITE_ID IN (SELECT ID FROM ECOMM_SITE WHERE NAME = 'ARLINGTON') AND IS_MICROPAYMENT_ACCOUNT = 'N'
GO