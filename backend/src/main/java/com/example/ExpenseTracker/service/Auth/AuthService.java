package com.example.ExpenseTracker.service.Auth;
import com.example.ExpenseTracker.dto.*;


public interface AuthService {
    RegisterResDTO saveUser(RegisterRequestDTO registerRequestDTO);
    WrapperLoginResDTO userLogin(LoginRequestDTO loginRequestDTO);
    UpdateAccountResDTO  changeAccountInfo(Long userId, UpdateAccountReqDTO updateAccount);
    UpdatePasswordResDTO changePassword(Long userId, UpdatePasswordReqDTO updatePassword);
}
