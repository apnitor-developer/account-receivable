package com.example.account.receivable.Auth.dto;

import com.example.account.receivable.User.entity.Users;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDto {
    private final String token;
    private final Users user;

    @JsonProperty("mfa_required")
    private final Boolean mfaRequired;

    @JsonProperty("mfa_token")
    private final String mfaToken;

    public LoginResponseDto(String token, Users user) {
        this(token, user, null, null);
    }

    public LoginResponseDto(String token, Users user, Boolean mfaRequired, String mfaToken) {
        this.token = token;
        this.user = user;
        this.mfaRequired = mfaRequired;
        this.mfaToken = mfaToken;
    }

    public static LoginResponseDto forMfaChallenge(Users user, String mfaToken) {
        return new LoginResponseDto(null, user, true, mfaToken);
    }
}

