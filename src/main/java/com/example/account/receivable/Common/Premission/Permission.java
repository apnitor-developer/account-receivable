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
    CREATE_CUSTOMER,
    EDIT_CUSTOMER,
    DELETE_CUSTOMER,


    // =========================
    // INVOICES
    // =========================
    VIEW_INVOICES,
    CREATE_INVOICE,
    EDIT_INVOICE,
    DELETE_INVOICE,


    // =========================
    // PAYMENTS
    // =========================
    VIEW_PAYMENTS,
    APPLY_PAYMENT,

    // =========================
    // AGING & REPORTS
    // =========================
    VIEW_AGING_REPORTS,


    // =========================
    // COLLECTIONS & PROMISE TO PAY
    // =========================
    VIEW_PROMISE_TO_PAY,
    CREATE_PROMISE_TO_PAY,


    // =========================
    // COMPANY
    // =========================
    VIEW_COMPANY,
    CREATE_COMPANY,
    DELETE_COMPANY,
    UPDATE_COMPANY,


    // USER
    VIEW_USER,
    INVITE_USER,


    // ROLES
    VIEW_ROLES,
    CREATE_ROLES
}

