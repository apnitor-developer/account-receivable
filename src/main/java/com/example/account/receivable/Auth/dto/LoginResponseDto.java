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
    private final Long passwordDaysRemaining;

    @JsonProperty("mfa_required")
    private final Boolean mfaRequired;

    @JsonProperty("mfa_token")
    private final String mfaToken;

    // ✅ Normal login without MFA
    public LoginResponseDto(String token, Users user, Long passwordDaysRemaining) {
        this.token = token;
        this.user = user;
        this.passwordDaysRemaining = passwordDaysRemaining;
        this.mfaRequired = false;
        this.mfaToken = null;
    }

    // ✅ MFA response
    public LoginResponseDto(
            String token,
            Users user,
            Boolean mfaRequired,
            String mfaToken,
            Long passwordDaysRemaining
    ) {
        this.token = token;
        this.user = user;
        this.mfaRequired = mfaRequired;
        this.mfaToken = mfaToken;
        this.passwordDaysRemaining = passwordDaysRemaining;
    }

    // ✅ Optional factory (if needed)
    public static LoginResponseDto forMfaChallenge(Users user, String mfaToken, Long daysRemaining) {
        return new LoginResponseDto(null, user, true, mfaToken, daysRemaining);
    }
}