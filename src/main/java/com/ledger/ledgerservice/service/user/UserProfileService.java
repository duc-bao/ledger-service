package com.ledger.ledgerservice.service.user;

import com.ledger.ledgerservice.model.dto.request.ChangePasswordRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateTwoFactorRequest;
import com.ledger.ledgerservice.model.dto.request.UpdateMyProfileRequest;
import com.ledger.ledgerservice.model.dto.response.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getMyProfile();
    UserProfileResponse updateMyProfile(UpdateMyProfileRequest request);
    void changeMyPassword(ChangePasswordRequest request);
    UserProfileResponse updateTwoFactor(UpdateTwoFactorRequest request);
}
