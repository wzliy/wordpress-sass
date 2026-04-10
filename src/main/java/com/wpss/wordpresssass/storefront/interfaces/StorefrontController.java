package com.wpss.wordpresssass.storefront.interfaces;

import com.wpss.wordpresssass.storefront.application.StorefrontPageApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StorefrontController {

    private final StorefrontPageApplicationService storefrontPageApplicationService;

    public StorefrontController(StorefrontPageApplicationService storefrontPageApplicationService) {
        this.storefrontPageApplicationService = storefrontPageApplicationService;
    }

    @GetMapping("/")
    public ModelAndView home(@RequestHeader(value = "Host", required = false) String hostHeader) {
        return storefrontPageApplicationService.resolveHome(hostHeader)
                .map(page -> new ModelAndView("storefront/home")
                        .addObject("page", page))
                .orElseGet(() -> {
                    ModelAndView modelAndView = new ModelAndView("storefront/fallback");
                    modelAndView.setStatus(HttpStatus.NOT_FOUND);
                    modelAndView.addObject("requestedHost", hostHeader == null ? "" : hostHeader);
                    return modelAndView;
                });
    }
}
