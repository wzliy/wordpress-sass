package com.wpss.wordpresssass.order.interfaces;

import com.wpss.wordpresssass.order.application.StorefrontCheckoutApplicationService;
import com.wpss.wordpresssass.order.application.StorefrontCartApplicationService;
import com.wpss.wordpresssass.order.application.command.CheckoutFormCommand;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StorefrontCartController {

    private final StorefrontCartApplicationService storefrontCartApplicationService;
    private final StorefrontCheckoutApplicationService storefrontCheckoutApplicationService;

    public StorefrontCartController(StorefrontCartApplicationService storefrontCartApplicationService,
                                   StorefrontCheckoutApplicationService storefrontCheckoutApplicationService) {
        this.storefrontCartApplicationService = storefrontCartApplicationService;
        this.storefrontCheckoutApplicationService = storefrontCheckoutApplicationService;
    }

    @GetMapping("/cart")
    public ModelAndView cart(@RequestHeader(value = "Host", required = false) String hostHeader,
                             HttpSession session) {
        return storefrontCartApplicationService.resolveCart(hostHeader, session)
                .map(page -> new ModelAndView("storefront/cart")
                        .addObject("page", page))
                .orElseGet(() -> fallback(hostHeader));
    }

    @GetMapping("/checkout")
    public ModelAndView checkout(@RequestHeader(value = "Host", required = false) String hostHeader,
                                 HttpSession session) {
        return storefrontCartApplicationService.resolveCart(hostHeader, session)
                .map(page -> new ModelAndView("storefront/checkout")
                        .addObject("page", page))
                .orElseGet(() -> fallback(hostHeader));
    }

    @PostMapping("/checkout")
    public ModelAndView submitCheckout(@RequestHeader(value = "Host", required = false) String hostHeader,
                                       @ModelAttribute CheckoutFormCommand command,
                                       HttpSession session) {
        return storefrontCheckoutApplicationService.checkout(hostHeader, command, session)
                .map(orderNo -> new ModelAndView("redirect:/order/" + orderNo + "/success"))
                .orElseGet(() -> fallback(hostHeader));
    }

    @PostMapping("/cart/items")
    public ModelAndView addItem(@RequestHeader(value = "Host", required = false) String hostHeader,
                                @RequestParam("productId") Long productId,
                                @RequestParam(value = "quantity", required = false) Integer quantity,
                                HttpSession session) {
        return storefrontCartApplicationService.addItem(hostHeader, productId, quantity, session)
                ? redirectCart()
                : fallback(hostHeader);
    }

    @PostMapping("/cart/items/{productId}/quantity")
    public ModelAndView updateQuantity(@RequestHeader(value = "Host", required = false) String hostHeader,
                                       @PathVariable("productId") Long productId,
                                       @RequestParam(value = "quantity", required = false) Integer quantity,
                                       HttpSession session) {
        return storefrontCartApplicationService.updateQuantity(hostHeader, productId, quantity, session)
                ? redirectCart()
                : fallback(hostHeader);
    }

    @PostMapping("/cart/items/{productId}/remove")
    public ModelAndView removeItem(@RequestHeader(value = "Host", required = false) String hostHeader,
                                   @PathVariable("productId") Long productId,
                                   HttpSession session) {
        return storefrontCartApplicationService.removeItem(hostHeader, productId, session)
                ? redirectCart()
                : fallback(hostHeader);
    }

    @GetMapping("/order/{orderNo}/success")
    public ModelAndView success(@RequestHeader(value = "Host", required = false) String hostHeader,
                                @PathVariable("orderNo") String orderNo) {
        return storefrontCheckoutApplicationService.resolveSuccess(hostHeader, orderNo)
                .map(page -> new ModelAndView("storefront/success")
                        .addObject("page", page))
                .orElseGet(() -> fallback(hostHeader));
    }

    private ModelAndView redirectCart() {
        return new ModelAndView("redirect:/cart");
    }

    private ModelAndView fallback(String hostHeader) {
        ModelAndView modelAndView = new ModelAndView("storefront/fallback");
        modelAndView.setStatus(org.springframework.http.HttpStatus.NOT_FOUND);
        modelAndView.addObject("requestedHost", hostHeader == null ? "" : hostHeader);
        return modelAndView;
    }
}
