package com.example.productvalidation.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/products";
    }

    @ResponseBody
    @GetMapping("/home")
    public String securedHome(Principal principal) {
        return "Hello, " + principal.getName();
    }

    @ResponseBody
    @GetMapping("/order")
    public String order() {
        return "Order page for USER role";
    }
}
