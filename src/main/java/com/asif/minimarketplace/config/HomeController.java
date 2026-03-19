        return "redirect:/login";

package com.asif.minimarketplace.config;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    public String home() {
        return "redirect:/login";
        return "redirect:/products";
}