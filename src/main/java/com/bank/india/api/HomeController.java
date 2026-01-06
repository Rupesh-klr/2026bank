package com.bank.india.api;
//package com.bank.india;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String comingSoon() {

        return "<h1>Coming Soon!</h1><p>Our Digital Wallet system is currently under development. Stay tuned!</p>";
    }
}