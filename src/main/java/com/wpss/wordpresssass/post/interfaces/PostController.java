package com.wpss.wordpresssass.post.interfaces;

import com.wpss.wordpresssass.common.api.ApiResponse;
import com.wpss.wordpresssass.post.application.PostApplicationService;
import com.wpss.wordpresssass.post.application.command.CreatePostCommand;
import com.wpss.wordpresssass.post.application.dto.PostDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    private final PostApplicationService postApplicationService;

    public PostController(PostApplicationService postApplicationService) {
        this.postApplicationService = postApplicationService;
    }

    @PostMapping("/create")
    public ApiResponse<PostDto> create(@Valid @RequestBody CreatePostCommand command) {
        return ApiResponse.success(postApplicationService.createPost(command));
    }

    @GetMapping("/list")
    public ApiResponse<List<PostDto>> list() {
        return ApiResponse.success(postApplicationService.listPosts());
    }
}
