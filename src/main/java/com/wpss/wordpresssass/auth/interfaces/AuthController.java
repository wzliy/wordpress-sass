package com.wpss.wordpresssass.auth.interfaces;

import com.wpss.wordpresssass.auth.interfaces.request.LoginRequest;
import com.wpss.wordpresssass.auth.service.AuthApplicationService;
import com.wpss.wordpresssass.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthApplicationService.AuthResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authApplicationService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public ApiResponse<AuthApplicationService.AuthResult> me() {
        return ApiResponse.success(authApplicationService.currentUser());
    }
}
