package com.wpss.wordpresssass.publish.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.publish.application.PublishApplicationService;
import com.wpss.wordpresssass.publish.application.command.PublishPostCommand;
import com.wpss.wordpresssass.publish.application.dto.PublishPostResultDto;
import com.wpss.wordpresssass.publish.application.dto.PublishRecordDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/publish")
public class PublishController {

    private final PublishApplicationService publishApplicationService;

    public PublishController(PublishApplicationService publishApplicationService) {
        this.publishApplicationService = publishApplicationService;
    }

    @PostMapping
    public ApiResponse<PublishPostResultDto> publish(@Valid @RequestBody PublishPostCommand command) {
        return ApiResponse.success(publishApplicationService.publish(command));
    }

    @GetMapping("/list")
    public ApiResponse<List<PublishRecordDto>> list() {
        return ApiResponse.success(publishApplicationService.listRecords());
    }
}
