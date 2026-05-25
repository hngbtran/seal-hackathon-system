package com.minhtung.hackathon.controller;


import com.minhtung.hackathon.dto.RegisterRequest;
import com.minhtung.hackathon.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Dang ky, xac nhan email")
public class AuthController {
    private final AuthService authService ;
    @Operation(
            summary = "Dang ky tai khoan",
            description = "Gui email xac nhan toi Gmail sau khi dang ky"
    )
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest req) {
        String result = authService.register(
                req.getUsername(), req.getEmail(), req.getPassword()
        );
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Xac nhan email",
            description = "User click link trong Gmail de kich hoat tai khoan"
    )
    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam String token) {
        String result = authService.verifyEmail(token);
        return ResponseEntity.ok(result);
    }
}



