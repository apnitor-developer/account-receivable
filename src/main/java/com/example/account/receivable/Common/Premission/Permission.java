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
    APPROVE_INVOICE,


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
    // COLLECTIONS & PROMISE TO PAY & DISPUTE
    // =========================
    VIEW_COLLECTIONS,
    VIEW_PROMISE_TO_PAY,
    CREATE_PROMISE_TO_PAY,
    VIEW_DISPUTE,
    CREATE_DISPUTE,
    VIEW_REMINDER,
    SEND_REMINDER,


    // CREDIT MEMOS
    VIEW_MEMOS,
    CREATE_MEMOS,
    APPROVE_MEMOS,


    // WRITE OFF
    VIEW_WRITE_OFF,
    CREATE_WRITE_OFF,
    APPROVE_WRITE_OFF,


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
    CREATE_ROLES,
    UPDATE_ROLE,
    VIEW_CODE,
    CREATE_CODE,
    UPDATE_CODE,
    DELETE_CODE,

    //GL Codes
    VIEW_GL_CODE,
    CREATE_GL_CODE,
    UPDATE_GL_CODE,

    // GL Transactions
    VIEW_GL_TRANSACTION,
    CREATE_GL_TRANSACTION
}

