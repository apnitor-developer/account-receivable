package com.example.account.receivable.WriteOff.Dto;

import lombok.Data;

@Data
public class CreateWriteOffRequest {

    private String reason;
    private Long arCodeId;
}
