package com.wpss.wordpresssass.user.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.user.application.UserManagementApplicationService;
import com.wpss.wordpresssass.user.application.command.ChangePasswordCommand;
import com.wpss.wordpresssass.user.application.command.CreateUserCommand;
import com.wpss.wordpresssass.user.application.command.ToggleUserStatusCommand;
import com.wpss.wordpresssass.user.application.command.UpdateUserProfileCommand;
import com.wpss.wordpresssass.user.application.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserManagementApplicationService userManagementApplicationService;

    public UserController(UserManagementApplicationService userManagementApplicationService) {
        this.userManagementApplicationService = userManagementApplicationService;
    }

    @GetMapping("/list")
    public ApiResponse<List<UserDto>> list() {
        return ApiResponse.success(userManagementApplicationService.listUsers());
    }

    @GetMapping("/detail")
    public ApiResponse<UserDto> detail(@RequestParam("id") Long id) {
        return ApiResponse.success(userManagementApplicationService.getUserDetail(id));
    }

    @PostMapping("/create")
    public ApiResponse<UserDto> create(@Valid @RequestBody CreateUserCommand command) {
        return ApiResponse.success(userManagementApplicationService.createUser(command));
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordCommand command) {
        userManagementApplicationService.changePassword(command);
        return ApiResponse.ok();
    }

    @PostMapping("/disable")
    public ApiResponse<Void> disable(@Valid @RequestBody ToggleUserStatusCommand command) {
        userManagementApplicationService.disableUser(command);
        return ApiResponse.ok();
    }

    @PostMapping("/enable")
    public ApiResponse<Void> enable(@Valid @RequestBody ToggleUserStatusCommand command) {
        userManagementApplicationService.enableUser(command);
        return ApiResponse.ok();
    }

    @PostMapping("/update")
    public ApiResponse<UserDto> update(@Valid @RequestBody UpdateUserProfileCommand command) {
        return ApiResponse.success(userManagementApplicationService.updateUserProfile(command));
    }
}
