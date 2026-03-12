package com.example.account.receivable.Collections.Reminder.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Collections.Reminder.Service.ReminderService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/invoice/{companyId}/{invoiceId}")
    public ResponseEntity<ApiResponse<String>>
    sendInvoiceReminder(@PathVariable Long invoiceId , @PathVariable Long companyId , @RequestBody int level) {

        reminderService.sendInvoiceReminder(invoiceId , companyId , level);

        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Reminder email sent successfully",
                "OK"
            )
        );
    }
}

