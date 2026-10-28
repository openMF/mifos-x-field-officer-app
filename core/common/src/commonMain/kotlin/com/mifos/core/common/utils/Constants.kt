/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.common.utils

/**
 * Created by satya on 13/04/14.
 */
object Constants {
    const val DATABASE_NAME = "mifos_database"

    const val INSTANCE_URL_KEY = "instanceURL"
    const val INSTANCE_DOMAIN_KEY = "instanceDomain"
    const val INSTANCE_PORT_KEY = "instancePort"
    const val TENANT_IDENTIFIER_KEY = "tenant identifier"
    const val PROTOCOL_HTTP = "http://"
    const val PROTOCOL_HTTPS = "https://"
    const val API_PATH = "/mifosng-provider/api/v1"

    /**
     * Entity Type, Like Clients, Groups, Staff, Loans, Savings and Client Identifiers
     */
    const val ENTITY_TYPE_CLIENTS = "clients"
    const val ENTITY_TYPE_GROUPS = "groups"
    const val ENTITY_TYPE_LOANS = "loans"
    const val ENTITY_TYPE_SAVINGS = "savings"
    const val ENTITY_TYPE_STAFF = "staff"
    const val ENTITY_TYPE_CLIENT_IDENTIFIERS = "client_identifiers"

    // Search Entities
    const val SEARCH_ENTITY_CLIENT = "CLIENT"
    const val SEARCH_ENTITY_GROUP = "GROUP"
    const val SEARCH_ENTITY_LOAN = "LOAN"
    const val SEARCH_ENTITY_SAVING = "SAVING"
    const val SEARCH_ENTITY_CENTER = "CENTER"
    const val CLIENT_NAME = "clientName"
    const val CLIENT_ID = "clientId"
    const val CLIENT_SURVEY = "clientSurvey"
    const val ID = "id"
    const val CLIENT = "Client"
    const val CLIENTS = "clients"
    const val LOAN_ACCOUNT_NUMBER = "loanAccountNumber"
    const val LOAN_PAYMENT_TYPE_OPTIONS = "LoanPaymentTypeOptions"
    const val LOAN_SUMMARY = "loanWithAssociation"
    const val SAVINGS_ACCOUNT_NUMBER = "savingsAccountNumber"
    const val SAVINGS_ACCOUNT_ID = "savingsAccountId"
    const val SAVINGS_ACCOUNT_TYPE = "savingsAccountType"
    const val SAVINGS_ACCOUNT_TRANSACTION_TYPE = "transactionType"
    const val SAVINGS_ACCOUNT_TRANSACTION_DEPOSIT = "Deposit"
    const val SAVINGS_ACCOUNT_TRANSACTION_WITHDRAWAL = "Withdrawal"
    const val DATA_TABLE_REGISTERED_NAME = "dataTableRegisteredName"
    const val CENTER_ID = "centerId"
    const val GROUP_ID = "groupId"
    const val GROUP_NAME = "groupName"
    const val GROUPS = "groups"
    const val GROUP_ACCOUNT = "isGroupAccount"
    const val CENTER = "center"
    const val ENTITY_TYPE = "entityType"
    const val ENTITY_ID = "entityId"
    const val DOCUMENT_ACTIONS = "document_actions"
    const val DOCUMENT = "document_title"
    const val DOCUMENT_DESCRIPTION = "document_description"
    const val CHARGE_ID = "chargeId"
    const val DATE_OF_COLLECTION = "dateOfCollection"
    const val REPAYMENT_DATE = "repaymentDate"
    const val DATE_FORMAT = "dateFormat"
    const val CALENDAR_INSTANCE_ID = "calendarInstanceId"
    const val LOCALE = "locale"
    const val LOCALE_EN = "en"
    const val DATE_FORMAT_LONG = "dd MMMM yyyy"
    const val TRANSACTIONS = "transactions"
    const val SURVEYS = "surveys"
    const val ANSWERS = "answers"
    const val QUESTION_DATA = "question data"
    const val IS_A_PARENT_FRAGMENT = "isAParentFragment"
    const val STOP_TRACKING = "stop_tracking"
    const val SERVICE_STATUS = "service_status"
    const val DATA_TABLE_NAME = "data_table_name"
    const val DIALOG_FRAGMENT = 1
    const val ACTIVATE_CLIENT = "activate_client"
    const val ACTIVATE_CENTER = "activate_center"
    const val ACTIVATE_GROUP = "activate_group"
    const val ACTIVATE_TYPE = "activation_type"
    const val ACTIVATE_ID = "activation_id"
    const val INTIAL_LOGIN = "initial_login"
    const val INDIVIDUAL_SHEET = "collection_sheet"
    const val DISBURSEMENT_DATE = "disbursement_date"
    const val TRANSACTION_DATE = "transaction_date"
    const val ADAPTER_POSITION = "adapter_position"
    const val PAYLOAD = "payload"
    const val PAYMENT_LIST = "payment_list"
    const val LOAN_AND_CLIENT = "loan_and_client_item"
    const val PAYMENT_OPTIONS = "payment_options"
    const val MEMBERS = "members"
    const val NEW = "NEW"
    const val SAVED = "SAVED"
    const val FILLNOW = "FillNow"

    // This needs to be 8 bits because validateRequestPermissionsRequestCode
    // in FragmentActivity requires requestCode to be of 8 bits, meaning the range is from 0 to 255.
    const val REQUEST_PERMISSION_SETTING = 254

    /**
     * PERMISSIONS_........ is an app-defined int constant of RunTime Permission . The callback
     * method gets the result of the request.
     */
    const val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2
    const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3

    /**
     * String Constant of Permissions in SharedPreference
     */
    const val READ_EXTERNAL_STORAGE_STATUS = "read_external_storage_status"
    const val WRITE_EXTERNAL_STORAGE_STATUS = "write_external_storage_status"
    const val ACCESS_FINE_LOCATION_STATUS = "access_fine_location_status"

    /**
     * Constants to identify which Data Tables have to be shown
     */
    const val DATA_TABLE_CLIENTS = 2001
    const val DATA_TABLE_LOANS = 2002
    const val DATA_TABLES_SAVINGS_ACCOUNTS = 2003

    /**
     * Constants to for Data Table Menu Names
     */
    const val DATA_TABLE_CLIENTS_NAME = "More Client Info"
    const val DATA_TABLE_LOAN_NAME = "More Loan Info"
    const val DATA_TABLE_SAVINGS_ACCOUNTS_NAME = "More Savings Account Info"
    const val DATA_TABLE_NAME_CLIENT = "m_client"
    const val DATA_TABLE_NAME_SAVINGS = "m_savings_account"
    const val DATA_TABLE_NAME_LOANS = "m_loan"
    const val DATA_TABLE_NAME_GROUP = "m_group"

    /**
     * User Logged In Status
     * 0 for Online and 1 for Offline
     */
    const val USER_ONLINE = false
    const val USER_OFFLINE = true

    /**
     * Constants to determine in the generic DataTableListFragment, the type of query that
     * has to be forwarded after showing the datatables and adding the values
     * to the corresponding payload.
     */
    const val CREATE_CLIENT = 3001
    const val CLIENT_LOAN = 3002
    const val GROUP_LOAN = 3003

    /**
     * Constant to identify whether Simple Collection Sheet fragment has to be opened
     * or the Individual Collection Sheet.
     */
    const val COLLECTION_TYPE = "collection_type"
    const val EXTRA_COLLECTION_INDIVIDUAL = "individual"
    const val EXTRA_COLLECTION_COLLECTION = "collection"

    /**
     * Constants related to RunReports
     */
    const val REPORT_NAME = "report_name"
    const val REPORT_ID = "report_id"
    const val CLIENT_REPORT_ITEM = "client_report_item"
    const val CLIENT_REPORT = "client_report"
    const val REPORT_CATEGORY = "report_category"
    const val LOAN = "Loan"
    const val ACCOUNTING = "Accounting"
    const val FUND = "Fund"
    const val SAVINGS = "Savings"
    const val LOAN_OFFICER_ID_SELECT = "loanOfficerIdSelectAll"
    const val LOAN_PRODUCT_ID_SELECT = "loanProductIdSelectAll"
    const val LOAN_PURPOSE_ID_SELECT = "loanPurposeIdSelectAll"
    const val FUND_ID_SELECT = "fundIdSelectAll"
    const val CURRENCY_ID_SELECT = "currencyIdSelectAll"
    const val OFFICE_ID_SELECT = "OfficeIdSelectOne"
    const val PAR_TYPE_SELECT = "parTypeSelect"
    const val SAVINGS_ACCOUNT_SUB_STATUS = "SavingsAccountSubStatus"
    const val SELECT_GL_ACCOUNT_NO = "SelectGLAccountNO"
    const val OBLIG_DATE_TYPE_SELECT = "obligDateTypeSelect"
    const val R_LOAN_OFFICER_ID = "R_loanOfficerId"
    const val R_LOAN_PRODUCT_ID = "R_loanProductId"
    const val R_LOAN_PURPOSE_ID = "R_loanPurposeId"
    const val R_FUND_ID = "R_fundId"
    const val R_CURRENCY_ID = "R_currencyId"
    const val R_OFFICE_ID = "R_officeId"
    const val R_PAR_TYPE = "R_parType"
    const val R_SUB_STATUS = "R_subStatus"
    const val R_ACCOUNT = "R_account"
    const val R_OBLIG_DATE_TYPE = "R_obligDateType"
    const val START_DATE_SELECT = "startDateSelect"
    const val END_DATE_SELECT = "endDateSelect"
    const val SELECT_ACCOUNT = "selectAccount"
    const val FROM_X_SELECT = "fromXSelect"
    const val TO_Y_SELECT = "toYSelect"
    const val OVERDUE_X_SELECT = "overdueXSelect"
    const val OVERDUE_Y_SELECT = "overdueYSelect"
    const val R_START_DATE = "R_startDate"
    const val R_END_DATE = "R_endDate"
    const val R_ACCOUNT_NO = "R_accountNo"
    const val R_FROM_X = "R_fromX"
    const val R_TO_Y = "R_toY"
    const val R_OVERDUE_X = "R_overdueX"
    const val R_OVERDUE_Y = "R_overdueY"
    const val ACTION_REPORT = "report"
    const val CURR_PASSWORD = "currentPassword"
    const val IS_TO_UPDATE_PASS_CODE = "updatePassCode"
    const val HAS_SETTING_CHANGED = "hasSettingsChanged"

    const val TENANT = "tenant"
    const val BASE_URL = "base_url"
    const val PASSCODE = "preferences_mifos_passcode_string"
    const val THEME = "theme"
    const val LANGUAGE = "language_type"

    // Compose Navigation KEY
    const val REPORT_TYPE_ITEM = "report_type_item"
    const val REPORT_PARAMETER_RESPONSE = "report_parameter_response"
    const val LOAN_WITH_ASSOCIATIONS = "loanWithAssociation"
    const val PASSCODE_INITIAL_LOGIN = "passcode_initial_login"
    const val DATA_TABLE_NAV_DATA = "data_table_nav_data"
    const val DATA_TABLE_DATA_NAV_DATA = "data_table_data_nav_data"
    const val DATA_TABLE_LIST_NAV_DATA = "data_table_list_nav_data"
    const val PAYMENT_DETAILS_ARGS = "paymentDetailsArgs"

    const val ADDRESS_LINE_1 = "addressLine1"
    const val ADDRESS_LINE_2 = "addressLine2"
    const val ADDRESS_LINE_3 = "addressLine3"
    const val CITY = "city"
    const val STATE = "state"
    const val COUNTRY = "country"
    const val POSTAL_CODE = "postalCode"
    const val STATUS = "status"
    const val DOCUMENT_KEY = "documentKey"

    /**
     * Constant values for standing instructions account type
     */
    const val LOAN_ACCOUNT_VAL = 1
    const val SAVINGS_ACCOUNT_VAL = 2
}
