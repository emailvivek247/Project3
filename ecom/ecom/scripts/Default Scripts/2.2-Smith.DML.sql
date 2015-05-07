------------*************************COUNTY SPECIFIC SCRIPTS *******************************************---------------
/* TO BE RUN FOR NEW COUNTY INSTALLATION */
--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
--- INSERT SITE INFORMATION ---
DECLARE @NODE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_NODE
WHERE NAME = 'RECORDSMANAGEMENT')
INSERT INTO ECOMM_SITE (NAME, DESCRIPTION, ACTIVE, COUNTY, STATE, AUTOACTIVATE, TIMEZONE, NODE_ID, DATE_TIME_CREATED, DATE_TIME_MOD,
MOD_USER_ID, SUBSCRIPTION_VALIDATION_TEXT, ENABLE_MICRO_TX_OTC, ENABLE_MICRO_TX_WEB, NAME_ON_CHECK, CREATED_BY, CHECK_HOLD_PERIOD, SEARCH_DAY_THRESHOLD,
USER_RETENTION_DAYS, IS_FIRM_NUMBER_REQUIRED, IS_BAR_NUMBER_REQUIRED, IS_FREE_SITE)
	VALUES ('SMITH', 'Smith County', 'Y', '', 'TX', 'Y', 'America/Chicago', @NODE_ID, GETDATE(), GETDATE(), 'SYSTEM',
	'You can choose only one of the available subscriptions', 'Y', 'Y', 'Smith County', 'SYSTEM', 3, 30, 30, 'Y', 'N', 'N')
GO

--- INSERT SITE CONFIGURATION INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO SITE_CONFIGURATION (SITE_ID, EMAIL_TEMPLATE_FOLDER, FROM_EMAIL_ADDRESS, PAYMENT_CONF_SUB, CHANGE_SUBSCRIPTION_SUB, CANCEL_SUBSCRIPTION_SUB,
REACTIVATE_SUBSCRIPTION_SUB, RECURRING_PAYMENT_SUCCESS_SUB, RECURRING_PAYMENT_UNSUCCESSFUL_SUB, PAYMENT_CONF_TEMPLATE, CHANGE_SUBSCRIPTION_TEMPLATE,
CANCEL_SUBSCRIPTION_TEMPLATE, REACTIVATE_CANCELLED_SUBSCRIPTION_TEMPLATE, RECURRING_PAYMENT_SUCCESS_TEMPLATE, RECURRING_PAYMENT_UNSUCCESSFUL_TEMPLATE,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, WEB_PAYMENT_CONFIRMATION_SUB, WEB_PAYMENT_CONFIRMATION_TEMPLATE, REMOVE_SUBSCRIPTION_SUB,
REMOVE_SUBSCRIPTION_TEMPLATE, CREATED_BY, ACCESS_AUTHORIZATION_SUB, ACCESS_AUTHORIZATION_TEMPLATE, PAYASUGO_PAYMENT_CONFIRMATION_SUB, PAYASUGO_PAYMENT_CONFIRMATION_TEMPLATE, ADD_SUBSCRIPTION_SUB)
	VALUES (@SITE_ID, 'RecordsManagement/Smith/', 'noReplyTest@amcad.com', 'Payment Confirmation - Roam Smith Records Management', 'Subscription Change Notification - Roam Smith Records Management',
	'Cancelled Subscription Notification - Roam Smith Records Management', 'Subscription Reactivated - Roam Smith Records Management', 'Recurring Payment Posted Successfully - Roam Smith Records Management',
	'Recurring Payment Failed - Roam Smith Records Management', 'paymentConfirmation.stl', 'changeConfirmation.stl', 'cancelConfirmation.stl', 'cancelConfirmation.stl', 'recurringPaymentSuccessfulTemplate.stl',
	'recurringPaymentUnsuccessfulTemplate.stl', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'Web Purchase Confirmation - Roam Smith Records Management', 'webPaymentConfirmation.stl', 'Subscription Removed - Roam Smith Records Management',
	'removeSubscription.stl', 'SYSTEM', 'Access Authorization - Roam Smith Records Management', 'accessAuthorization.stl', 'Pay As You Go Purchase Confirmation - ROAM Smith Case Management', 'payAsYouGoPaymentConfirmation.stl',
	'Subscription Add Notification - Smith Records Management')
GO

--- INSERT MERCHANT INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_MERCHANTINFO (SITE_ID, USERNAME, PASSWORD, VENDORNAME, ACTIVE, PARTNER, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, IS_MICROPAYMENT_ACCOUNT, CREATED_BY, TRAN_FEE_PERCENTAGE, TRAN_FEE_FLAT, TRAN_FEE_PERCENTAGE_AMEX, TRAN_FEE_FLAT_AMEX)
	VALUES (@SITE_ID, 'vpratti', '239C4BED958C2527627816CD4CE18E8FE3EF87334EAF57A9AA5A5657F8239258', 'amcadepay', 'Y', 'paypal', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'SYSTEM', '2.2', '0.30', '3.50', '0.00')
GO

DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_MERCHANTINFO (SITE_ID, USERNAME, PASSWORD, VENDORNAME, ACTIVE, PARTNER, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, IS_MICROPAYMENT_ACCOUNT, CREATED_BY, TRAN_FEE_PERCENTAGE, TRAN_FEE_FLAT, TRAN_FEE_PERCENTAGE_AMEX, TRAN_FEE_FLAT_AMEX)
	VALUES (@SITE_ID, 'vpratti', '239C4BED958C2527627816CD4CE18E8FE3EF87334EAF57A9AA5A5657F8239258', 'amcadepay', 'Y', 'paypal', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM', '2.2', '0.30', '3.50', '0.00')
GO

--- INSERT MAGENSA INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_MAGENSAINFO (SITE_ID, HOST_ID, HOST_PASSWORD, REGISTEREDBY, ENCRYPTION_BLOCK_TYPE, CARD_TYPE, OUTPUT_FORMAT_CODE, ACTIVE,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
	VALUES (@SITE_ID, 'MAG527997454', 'A36B2CE63B1234A4912C0D933CE63C75E30B9FC6E04AFC1AB70FC678ABC7756E', 'AMCAD', 1, 1, 101, 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

--- INSERT CREDIT USAGE FEE INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_CREDITUSAGE_FEE (SITE_ID, TX_FEE_FLAT, TX_FLAT_FEE_CUTOFF_AMT, TX_FEE_PERCENT, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID,
ACTIVE, DOWNGRADE_FEE, TX_FEE_ADDITIONAL, MICRO_TX_CUT_OFF, CREATED_BY)
	VALUES (@SITE_ID, '5.00', '60.00', '5.00', GETDATE(), GETDATE(), 'SYSTEM', 'Y', '5.00', '0.00', '8.33', 'SYSTEM')
GO

--- INSERT WEB PAYMENT FEE INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_WEBPAYMENT_FEE (SITE_ID, TX_FEE_FLAT, TX_FLAT_FEE_CUTOFF_AMT, TX_FEE_PERCENT, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID,
ACTIVE, TX_FEE_ADDITIONAL, MICRO_TX_CUT_OFF, CREATED_BY)
	VALUES (@SITE_ID, '5.00', '60.00', '5.00', GETDATE(), GETDATE(), 'SYSTEM', 'Y', '0.00', '8.33', 'SYSTEM')
GO

--- INSERT TERM INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
DECLARE @TERM_TYPE_ID AS INT = ( SELECT TOP 1
	ID
FROM AUTH_TERM_TYP
WHERE TERM_TYP_CD = 'R')
INSERT INTO AUTH_TERMS (TERM_DESC, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, SITE_ID, TERM_TYP_ID, ACTIVE, CREATED_BY)
	VALUES (
	'<b>Terms and Conditions</b><br/><br/><ol><li><u>Access to and use of the eAccess website (hereinafter "Services") provided for the Smith Supreme
	Court by American Cadastre, LLC, dba AMCAD, is subject to the terms and conditions of this User Agreement and all applicable laws and regulations.</u>
	This includes the laws and regulations governing copyright, trademark and other intellectual property as it may pertain to the property licensed to the Court
	by AMCAD. For the purposes of this User Agreement, the Smith Supreme Court shall be referred to as the "Court," and you, the User, will be referred
	to as "You" (including the possessive "Your"), and the "User."</li><br/><li><u>By reading this document and accessing the services, you accept without
	limitation or qualification, all the terms and conditions in this user agreement.</u> You agree that your acceptance obligates you to pay for access to
	these Services where applicable. This includes an obligation to pay the charges incurred by third parties, whether they are your agents or otherwise,
	who access this Service through your account. The Court reserves the right to change these terms and conditions and the prices charged for Services at
	any time. Changes to the terms of this Agreement and charges for Services will only apply to future use of the Services (i.e. a change to the charge for
	an individual image will apply to charges occurring after the change, while a change to the charge for subscription access will apply to the next billing
	cycle following the change). You may be required to affirmatively accept the revised Terms of Use in order to continue using the Services
	OR your continued use of this Site and these Services after the posting of updates to the Terms of Use or updates to the charges for
	Services will constitute your agreement to those terms or charges, as modified. The Court additionally reserves the right to modify or
	 discontinue, at any time, any Services, without notice or liability. You agree that AMCAD or the Court may provide notice of changes to
	 Terms or charges to you by posting it on the Site or by emailing it to the email address provided in the user profile.</li><br/><li><u>LIABILITY FOR
	 CHARGES INCURRED FOR ACCESS TO SERVICES:</u> By checking the "I agree to the Terms of Use" box and accessing the Services, you agree to pay
	 the charges established for these Services. The current charges applicable to the use of these Services can be seen by clicking on the Subscription menu.
	 Payment for charges incurred for accessing Products and Services must be paid with a valid credit card. Acceptable types of credit card payment are Visa®,
	 MasterCard®, American Express® and Discover®. All payments are due at time of purchase. Time of purchase is deemed as “at checkout” for a Pay As You Go subscription.
	 For recurring subscriptions, payment is due at time of subscription purchase and on each subsequent monthly anniversary date.<br/><br/><u>FIRM PAYMENT:</u> Firm member
	 users WILL have the ability to pay for Products and Services by utilizing the credit card of record for the firm subscription. However, firm member users WILL NOT
	 have the ability to inquire or gain information (for example, the account number) about a firm credit card, unless that firm member is the
	 firm administrator.<br/><br/><u>SUBSCRIPTION ALLOTMENT:</u>  The user accepts that recurring subscription limits can be reached at any time
	 during a subscription month. After the allotment has been reached, any additional purchases will be based on the Pay As You Go subscription rate for
	 the remainder of the subscription month period unless the user upgrades to a different recurring subscription level. Upon the start of the new month,
	 the recurring subscription allotment will be replenished.<br/><br/><u>SUBSCRIPTION CANCELLATION: Recurring subscriptions auto renew on the monthly
	 anniversary date. The user can cancel the subscription at any point. The cancellation will take effect at the close of the current subscription month.
	 Cancellation will not prorate account payment or lead  to payment refund.</u></li><br/><li><u>PRIVACY:</u> By checking the “I agree to Terms of Use” box,
	 the user agrees to provide personal information at account registration. The Court confirms that the sole purpose for collecting personal information is for
	 user verification. Personal Information will not be disseminated to third parties. Personal information includes:<ul><li>User First Name</li><li>User Last Name</li><li>Email Address</li><li>Bar Number (where applicable)</li><li>Firm Name (where applicable)</li><li>User Phone Number</li><li>Credit Card Information</li></ul><br/></li><li><u>OWNERSHIP AND PROPRIETARY RIGHTS:</u> All the Products and Services, including but not limited to text, data, maps, images, graphics, trademarks, logos and service marks (collectively, the "Content"), are owned by the Court or licensed to the Court by AMCAD, the owners of the Content. The Court and AMCAD reserve their copyright, trademark or other intellectual property interests in their property that is part of the Content. In connection with those products and Services, you agree to the following: text, data, maps, images, graphics, trademarks, logos and service marks (collectively, the "Content"), are owned by the Court or licensed to the Court by third-parties who own the Content and the third party licensors'' property interests are protected by copyright, trademark and other intellectual property laws. In connection WITH those products and Services, you agree to the following:<br/><ul><li>When accessing the Content, you may PRINT a copy. If a printout and/or download is made, applicable third parties shall retain all rights in this material, and such a printout and/or download shall retain any copyright or other notices contained in that Content.</li><li>You will abide by restrictions SET forth on the Site with respect to any of the Content.</li><li>You will not in any way violate the intellectual property laws protecting the third party licensors'' property interests in the Content.</li><li>You agree that the material you are accessing contains the trade secrets and intellectual property of AMCAD and you will cause irreparable harm to AMCAD if this material is used in violation of this agreement.</li><li>You will not reuse, republish or otherwise distribute the trade secrets and intellectual property of AMCAD or any modified or altered versions of it, whether over the Internet or otherwise, and whether or not for payment in competition to the Services provided by AMCAD or the Court without the express written permission of the copyright holder.</li><li>You will cooperate promptly and completely with any reasonable request by the Court related to an investigation of infringement of copyright or other proprietary right of the third party licensor.</li></ul></li><br/><li><u> INDEMNIFICATION:</u> You hereby agree to indemnify and hold harmless the Court, and its respective officials, agencies, officers, subsidiaries, employees, licensors and agents, from and against any and all liability, loss, claims, damages, costs and/or actions (including attorneys'' fees) based upon or arising out of any breach by you or any third party of the obligations under this Agreement. Notwithstanding your indemnification obligation, the Court reserves the right to defend any such claim and you agree to provide the Court with such reasonable cooperation and information as the Court may request.</li><br/><li><u>DISCLAIMER OF WARRANTY AND LIMITATION OF LIABILITY:</u>  The Court makes every effort to UPDATE this information on a daily basis. Because case information is continually changing, the Court makes no expressed or implied warranty concerning the accuracy of this information. The information and services and products available to you may contain errors and are subject to periods of interruption. The Court will do its best to maintain the information and services it offers. You agree that all USE of these services is at your own risk, and that the Court will not be held liable for any errors or omissions contained in the content of its services.<br/><br/>The services are provided as is and the Court expressly disclaims any and all warranties, express and implied, including but not limited to any warranties of accuracy, reliability, title, merchantability, non-infringement, fitness for a particular purpose or any other warranty, condition, guarantee or representation whether oral, in writing or in electronic form, including but not limited to the accuracy or completeness of any information contained therein or provided by the services. The Court does not represent or warrant that access to the service will be interrupted or that there will be no failures, errors or omissions or loss of transmitted information. The information, documents and related graphics published on this server could include technical inaccuracies or typographical errors. Changes are periodically made to the information herein. The Court may  make improvements and/or changes in the services provided and/or the content described herein at any time.<br/><br/>This disclaimer of liability applies to any damages or injury caused by any failure of performance error, omission, interruption, deletion, defect, delay in operation or transmission, computer viruses, communication line failure, theft or destruction or unauthorized access to alteration of or USE of record, whether for breach of contract, tortious behavior, negligence or any other cause of action. No advice or information, whether oral or written, obtained by you from the Court or through or from the service shall create any warranty not expressly stated in this agreement. If you are dissatisfied with the service, or any portion thereof, your exclusive remedy shall be to stop using the service.</li><br/><li><u>MISCELLANEOUS:</u><br/><br/><b>Continuity of Service</b><br/>The Court has the right at any time to change or discontinue any aspect or feature of the services, including, but not limited to, content, hours of availability, and equipment needed for access or USE.<br/><br/><b>Obligation to USE Services Within 30 Days</b><br/>User accounts that are “inactive”, meaning an account that has been OPEN more than 30 days AND that has never had any account transactions, will be placed in “archived” status. Archived accounts cannot be removed from archived status and will require the user to create a new user account.<br/><br/><b>Unlawful USE</b><br/>You must USE the services for lawful purposes only. Any user conduct that restricts or inhibits any other person from using or enjoying the services will not be permitted. Uses such as data mining, screen scraping and the  USE of electronic BOTS for image download are prohibited. Users that are found to be employing the previously mentioned electronic methods may be restricted on this website.<br/><br/>The USE of deception to access Products or Services could result in legal action by the Court and/or the immediate cancellation of the account without refund.<br/><br/><b>Resale of Data</b><br/>The resale of the Court’s BULK data is prohibited unless made in compliance with the Smith Code of Judicial Administration section 1-605. Any unauthorized reselling of information may result in legal action by the Court and/or immediate cancellation of your account without refund.<br/><br/><b>Applicable Law</b><br/>These terms and conditions shall be governed by and construed according to the laws of the State of Smith, USA, and you agree to submit to the personal jurisdiction of the courts of the State of Smith. If any portion of these terms and conditions is deemed by a court to be invalid, the remaining provisions shall remain in full force and effect. You agree that any claim or cause of action arising out of or related to the USE of these Services must be presented to the Administrative Director of the Administrative Office of the Courts as a Notice of Claim with in accordance with A.R.S. §12-821.01. within one hundred and eighty days after such claim or cause of action arose.<br/><br/><b>Entire Agreement</b><br/>This is an offer to provide Services, and acceptance is expressly conditioned upon your acceptance of these terms and only these terms. Your acceptance of this Agreement is demonstrated by checking the box of "I agree to the Terms of Use" in the Registration form. This Agreement represents the entire agreement between you (the user) and the Court.',
	GETDATE(), GETDATE(), 'SYSTEM', @SITE_ID, @TERM_TYPE_ID, 'Y', 'SYSTEM')
GO


--- INSERT ACCESS INFORMATION ---
INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED,
IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_ADMIN', 'Smith County ROAM ADMIN', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 'ACCESS TO THE ROAM DESKTOP.', 'N', NULL, 'SYSTEM', 'N',
'N',1,'N',0,'N')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED,
IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_PERPAGE_ACCESS', 'Pay as You Go ($1.50 a page, minimum $2 per document) with Image Preview (free)', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', '<b>Subscription cost:</b> $1.50 Per Page Printed with a minimum of $2 per document<br /><b>Access to Smith County Official Public Records</b><br /><b>Data Access:</b> Index Access wtih free Image Preview (with Watermark) and ability to purchase images without watermarks for printing<br /><b>Additional Features Include:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality, Up to 100 records Per Page', 'N', '1', 'SYSTEM', 'N',
'Y',1,'N',0,'N')
GO


DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_ADMIN')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

-- INSERT SITE ACCESS INFORMATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = ( SELECT TOP 1
	ID
FROM AUTH_ACCESS
WHERE ACCESS_CD = 'SMITH_PERPAGE_ACCESS')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

--- INSERT SUBSCRIPTIONFEE INFORMATION ---
DECLARE @SUBSCRIPTION_TYP_ID AS INT = ( SELECT TOP 1
	ID
FROM CODELOOKUP
WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE_ACCESS
WHERE ACCESS_ID = (SELECT TOP 1
	ID
FROM AUTH_ACCESS
WHERE ACCESS_CD = 'SMITH_PERPAGE_ACCESS'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
	VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0', -1, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO


--- INSERT NON RECURRING FEE INFORMATION ---

DECLARE @SUBSCRIPTION_TYP_ID AS INT = ( SELECT TOP 1
	ID
FROM CODELOOKUP
WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE_ACCESS
WHERE ACCESS_ID IN ((SELECT TOP 1
	ID
FROM AUTH_ACCESS
WHERE ACCESS_CD = 'SMITH_PERPAGE_ACCESS')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '2.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '1.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_1', '(Single User) Monthly Unofficial Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $100 Per Month<br />Unlimited searches<br />Available Records:1963 - Present<br />Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page: 100<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality', 'N', '2', 'SYSTEM', 'N', 'Y',
'1', 'N', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_1')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_1'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '100.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_1')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO


INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_5', '(2-5 Users) Monthly Unofficial Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $250 Per Month<br />Discount: 50%<br />Features Same as (Single User) Monthly Unofficial Unlimited Record Access', 'N', '3', 'SYSTEM', 'N', 'Y',
'5', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_5')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_5'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '250.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_5')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO


INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_10', '(6-10 Users) Monthly Unofficial Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $450 Per Month<br />Discount: 55%<br />Features Same as (Single User) Monthly Unofficial Unlimited Record Access', 'N', '4', 'SYSTEM', 'N', 'Y',
'10', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_10')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_10'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '450.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_10')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO


INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_25', '(11-25 Users) Monthly Unofficial Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $800 Per Month<br />Discount: 68%<br />Features Same as (Single User) Monthly Unofficial Unlimited Record Access', 'N', '5', 'SYSTEM', 'N', 'Y',
'25', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_25')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_25'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '800.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_25')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO



/** ADDED NEW SUBSCRIPTIONS FOR THE MONTHLY IMAGE PREVIEW UNDISCOUNTED**/
INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_DISC_1', '(Single User) Yearly Unofficial Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $300 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records:1963 - Present<br />Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page: 100<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality', 'N', '10', 'SYSTEM', 'N', 'N',
'1', 'N', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_1')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_1'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '300.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_1')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
----------------------
INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_DISC_5', '(2-5 Users) Yearly Unofficial Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $350.00 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records:1963 - Present<br />Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page: 100<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality', 'N', '11', 'SYSTEM', 'N', 'N',
'5', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_5')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_5'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '350.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_5')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_DISC_10', '(6-10 Users) Yearly Unofficial Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $400 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records:1963 - Present<br />Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page: 100<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality', 'N', '12', 'SYSTEM', 'N', 'N',
'10', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_10')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_10'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '400.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_10')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
----------------------
INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_UNOFFICIAL_DISC_25', '(11-25 Users) Yearly Unofficial Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $450 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records:1963 - Present<br />Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page: 100<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality', 'N', '13', 'SYSTEM', 'N', 'N',
'25', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_25')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_25'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '450.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID IN ((SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_25')))
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 1, '1.00', 'N', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PP')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1.00', 2, '0.00', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @SUBSCRIPTION_TYP_ID = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'PPV')
INSERT INTO ECOMM_NON_RECURRING_FEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE_UNDER_PAGE_THRESHOLD, PAGE_THRESHOLD, FEE_OVER_PAGE_THRESHOLD, IS_SERVICE_FEE, CURRENCY, 
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '0.00', 2, '0.50', 'Y', 'USD', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO



INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_1', '(Single User ) Monthly Official Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $250 Per Month<br />Unlimited searches<br />Available Records: Sovereignty - Present<br />Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page: 500<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br />Premium Features: Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining', 'N', '7', 'SYSTEM', 'N', 'Y',
'1', 'N', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_1')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_1'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '250.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_5', '(2-5 Users) Monthly Official Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $625 Per Month<br />Discount: 50%<br />Features Same as (Single User) Monthly Official Unlimited Record Access', 'N', '7', 'SYSTEM', 'N', 'Y',
'5', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_5')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_5'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '625.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_10', '(6-10 Users) Monthly Official Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $1,100 Per Month<br />Discount: 56%<br />Features Same as (Single User) Monthly Official Unlimited Record Access', 'N', '8', 'SYSTEM', 'N', 'Y',
'10', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_10')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_10'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '1100.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_25', '(11-25 Users) Monthly Official Unlimited Record Access', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $2,000 Per Month<br />Discount: 68%<br />Features Same as (Single User) Monthly Official Unlimited Record Access', 'N', '9', 'SYSTEM', 'N', 'Y',
'25', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_25')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'MONT')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_25'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '2000.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

----------------------------------------

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_DISC_1', '(Single User) Yearly Official Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $600 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records: Sovereignty - Present<br />Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page: 500<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br />Premium Features: Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining', 'N', '14', 'SYSTEM', 'N', 'N',
'1', 'N', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_1')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_1'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '600.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO


INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_DISC_5', '(2-5 Users) Yearly Official Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $650 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records: Sovereignty - Present<br />Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page: 500<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br />Premium Features: Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining', 'N', '15', 'SYSTEM', 'N', 'N',
'5', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_5')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_5'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '650.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_DISC_10', '(6-10 Users) Yearly Official Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $700 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records: Sovereignty - Present<br />Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page: 500<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br />Premium Features: Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining', 'N', '16', 'SYSTEM', 'N', 'N',
'10', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_10')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_10'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '700.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO

INSERT INTO AUTH_ACCESS (ACCESS_CD, ACCESS_DESCR, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, GUEST_FLG, ACTIVE, ACCESS_FEATURES, DEFAULT_ACCESS_FLG, ACCESS_ORDER, CREATED_BY, IS_AUTHORIZATION_REQUIRED, IS_VISIBLE, MAX_USERS_ALLOWED, IS_FIRM_LEVEL_ACCESS, MAX_DOCUMENTS_ALLOWED, IS_GOVERNMENT_ACCESS)
VALUES ('SMITH_OFFICIAL_DISC_25', '(11-25 Users) Yearly Official Unlimited Record Access for Bar\Realtor Assoc', GETDATE(), GETDATE(), 'SYSTEM', 'N', 'Y', 
'<br />Subscription Cost: $750 Per Year<br />Discounted Subscription<br />Unlimited searches<br />Available Records: Sovereignty - Present<br />Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page: 500<br />Additional Features: Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br />Premium Features: Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining', 'N', '17', 'SYSTEM', 'N', 'N',
'25', 'Y', '0', 'N' )
GO
DECLARE @SITE_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE WHERE NAME = 'SMITH')
DECLARE @ACCESS_ID AS INT = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_25')
INSERT INTO ECOMM_SITE_ACCESS (SITE_ID, ACCESS_ID, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
VALUES (@SITE_ID, @ACCESS_ID, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO
DECLARE @SUBSCRIPTION_TYP_ID AS INT = (SELECT TOP 1 ID FROM CODELOOKUP WHERE CODE = 'YEAR')
DECLARE @SITEACCESS_ID AS INT = (SELECT TOP 1 ID FROM ECOMM_SITE_ACCESS WHERE ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_25'))
INSERT INTO ECOMM_SUBSCRIPTIONFEE (SUBSCRIPTION_TYP_ID, SITEACCESS_ID, FEE, TERM, CURRENCY, ACTIVE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, CREATED_BY)
VALUES (@SUBSCRIPTION_TYP_ID, @SITEACCESS_ID, '750.00', 0, 'USD', 'Y', GETDATE(), GETDATE(), 'SYSTEM', 'SYSTEM')
GO




UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription cost:</b> $1.50 Per Page Printed with a minimum of $2 per document<br /><b>Access to Collin County Official Public Records</b><br /><b>Data Access:</b> Index Access wtih free Image Preview (with Watermark) and ability to purchase images without watermarks for printing<br /><b>Additional Features Include:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality, Up to 100 records Per Page' WHERE ACCESS_CD = 'SMITH_PERPAGE_ACCESS'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost: </b>$100<br /><b>Unlimited searches<br />Available Records:</b>1963 - Present<br /><b>Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page:</b> 100<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_1'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $250<br /><b>Discount: </b>50%<br />Features Same as (Single User) Monthly Unofficial Unlimited Record Access' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_5'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $450<br /><b>Discount:</b> 55%<br />Features Same as (Single User) Monthly Unofficial Unlimited Record Access' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_10'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost: </b>$800<br /><b>Discount:</b> 68%<br />Features Same as (Single User) Monthly Unofficial Unlimited Record Access' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_25'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $25<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b>1963 - Present<br /><b>Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page:</b> 100<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_1'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $33.33<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b>1963 - Present<br /><b>Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page:</b> 100<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_5'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $37.50<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b>1963 - Present<br /><b>Full Document Preview w/ Watermark<br />Unlimited Document Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page:</b> 100<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_10'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $41.67<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b>1963 - Present<br /><b>Full Document Preview w/ Watermark<br />Unlimited Document and Saving and Printing<br />$1 Per Page to Print + $.50 Per Page Convenience Fee<br />Max Results Per Page:</b> 100<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality' WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_25'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $250<br /><b>Unlimited searches</b><br /><b>Available Records:</b> Sovereignty - Present<br /><b>Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page:</b> 500<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br /><b>Premium Features:</b> Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining' WHERE ACCESS_CD = 'SMITH_OFFICIAL_1'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $625<br /><b>Discount:</b> 50%<br />Features Same as (Single User) Monthly Official Unlimited Record Access' WHERE ACCESS_CD = 'SMITH_OFFICIAL_5'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $1,100<br /><b>Discount:</b> 56%<br />Features Same as (Single User) Monthly Official Unlimited Record Access' WHERE ACCESS_CD = 'SMITH_OFFICIAL_10'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $2,000<br /><b>Discount:</b> 68%<br />Features Same as (Single User) Monthly Official Unlimited Record Access' WHERE ACCESS_CD = 'SMITH_OFFICIAL_25'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $50<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b> Sovereignty - Present<br /><b>Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page:</b> 500<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br /><b>Premium Features:</b> Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining' WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_1'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $58.33<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b> Sovereignty - Present<br /><b>Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page:</b> 500<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br /><b>Premium Features:</b> Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining' WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_5'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $62.50<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b> Sovereignty - Present<br /><b>Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page:</b> 500<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br /><b>Premium Features:</b> Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining' WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_10'
UPDATE AUTH_ACCESS SET ACCESS_FEATURES = '<b>Subscription Cost:</b> $66.67<br /><b>Discounted Subscription<br />Unlimited searches<br />Available Records:</b> Sovereignty - Present<br /><b>Unlimited Document Access<br />Unlimited Document Saving<br />Unlimited Document Printing<br />No Document Watermark<br />Max Results Per Page:</b> 500<br /><b>Additional Features:</b> Full Field Text Searching, Faceting, Smart Navigation, User Defined Search Sorting, Shopping Cart Functionality<br /><b>Premium Features:</b> Phonetic Name Searching, Dashboards,Mobile Device Template, Document Chaining' WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_25'
GO





  --- INSERT BANK DETAILS ---
DECLARE @HIGHESTSTARTCHECKNUM AS INT = ( SELECT
	MAX(START_CHECK_NUM)
FROM ECOMM_BANK_DETAILS) DECLARE @HIGHESTENDCHECKNUM AS INT = ( SELECT
	MAX(END_CHECK_NUM)
FROM ECOMM_BANK_DETAILS) SET @HIGHESTSTARTCHECKNUM = @HIGHESTSTARTCHECKNUM + 10000
SET @HIGHESTENDCHECKNUM = @HIGHESTENDCHECKNUM + 10000
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_BANK_DETAILS (SITE_ID, FROM_FNAME, FROM_LNAME, FROM_MINITIAL, FROM_ADDRLINE1, FROM_ADDRLINE2, FROM_CITY, FROM_STATE, FROM_ZIPCODE, FROM_PHONENUM,
BANK_NAME, BANK_CODE, ROUTING_NUM, ACCOUNT_NUM, LAST_ISSUED_CHECK_NUM, START_CHECK_NUM, END_CHECK_NUM, BANK_ADDRLINE1, BANK_ADDRLINE2, BANK_CITY, BANK_STATE, BANK_ZIPCODE,
DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SITE_ID, 'AMCAD', '', '', '15867 N Mountain Road', '', 'Broadway', 'VA', '22815', '', 'BB&T', '111-20/121', '056005318', '0005132069613',
	 @HIGHESTSTARTCHECKNUM, @HIGHESTSTARTCHECKNUM, @HIGHESTENDCHECKNUM, NULL, NULL, NULL, NULL, NULL, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO



--- INSERT RECEIPT CONFIGURATION ---
DECLARE @SITE_ID AS INT = ( SELECT TOP 1
	ID
FROM ECOMM_SITE
WHERE NAME = 'SMITH')
INSERT INTO ECOMM_RECEIPT_CONFIGURATION (SITE_ID, BUSINESSNAME, ADDRESS_LINE_1, ADDRESS_LINE_2, CITY, STATE, ZIP, PHONE, COMMENTS_1, COMMENTS_2, TYPE, DATE_TIME_CREATED,
DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY)
	VALUES (@SITE_ID, 'AMCAD', '220 Spring St', 'Suite 150', 'Herndon', 'VA', '20170', '7037377775', '', '', 'OTC', GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO


--- INSERT PROFIT SHARE ---
DECLARE @ACCESS_ID AS INT = ( SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_PERPAGE_ACCESS') 
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) 
	VALUES (@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_1')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_5')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')     
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_10')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_25')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_1')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_5')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_10')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_25')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_1')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_5')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_10')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_UNOFFICIAL_DISC_25')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_1')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_5')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_10')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
SET @ACCESS_ID = (SELECT TOP 1 ID FROM AUTH_ACCESS WHERE ACCESS_CD = 'SMITH_OFFICIAL_DISC_25')
INSERT INTO ECOMM_PROFIT_SHARE (ACCESS_ID, CLIENT_SHARE, DATE_TIME_CREATED, DATE_TIME_MOD, MOD_USER_ID, ACTIVE, CREATED_BY) VALUES(@ACCESS_ID, 0.50, GETDATE(), GETDATE(), 'SYSTEM', 'Y', 'SYSTEM')
GO

