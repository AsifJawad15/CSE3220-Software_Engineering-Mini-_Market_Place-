package com.asif.minimarketplace.auth.controller;
import com.asif.minimarketplace.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Login page loads successfully")
    void loginPage_Loads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Buyer register page loads successfully")
    void showBuyerRegisterForm_Loads() throws Exception {
        mockMvc.perform(get("/register/buyer"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("Seller register page loads successfully")
    void showSellerRegisterForm_Loads() throws Exception {
        mockMvc.perform(get("/register/seller"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seller"))
                .andExpect(model().attributeExists("registerRequest"));
    }
}