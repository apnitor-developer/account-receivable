package com.example.account.receivable.Common.Premission;

public enum Permission {

    // =========================
    // DASHBOARD
    // =========================
    VIEW_DASHBOARD,


    // =========================
    // CUSTOMERS (TAB + ACTIONS)
    // =========================
    VIEW_CUSTOMERS,
    VIEW_CUSTOMER_DETAILS,
    CREATE_CUSTOMER,
    EDIT_CUSTOMER,
    DELETE_CUSTOMER,


    // =========================
    // INVOICES
    // =========================
    VIEW_INVOICES,
    VIEW_INVOICE_DETAILS,
    CREATE_INVOICE,
    EDIT_INVOICE,
    DELETE_INVOICE,
    SEND_INVOICE,


    // =========================
    // PAYMENTS
    // =========================
    VIEW_PAYMENTS,
    VIEW_PAYMENT_DETAILS,
    CREATE_PAYMENT,
    APPLY_PAYMENT,


    // =========================
    // AGING & REPORTS
    // =========================
    VIEW_AGING_REPORTS,
    EXPORT_AGING_REPORT,
    EXPORT_REPORTS,


    // =========================
    // COLLECTIONS & PROMISE TO PAY
    // =========================
    VIEW_COLLECTIONS,
    VIEW_PROMISE_TO_PAY,
    CREATE_PROMISE_TO_PAY,
    UPDATE_PROMISE_TO_PAY,


    // =========================
    // DISPUTES
    // =========================
    VIEW_DISPUTES,
    CREATE_DISPUTE,
    RESOLVE_DISPUTE,


    // =========================
    // SETUP / ADMIN
    // =========================
    VIEW_SETUP_ADMIN,
    MANAGE_COMPANY_SETTINGS,
    MANAGE_USERS,
    MANAGE_ROLES
}

