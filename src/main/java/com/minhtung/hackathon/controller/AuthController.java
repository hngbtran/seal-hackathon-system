package com.minhtung.hackathon.controller;


import com.minhtung.hackathon.dto.LoginRequest;
import com.minhtung.hackathon.dto.LoginResponse;
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

   //login
    @Operation(
            summary = "Dang nhap",
            description = "Tra ve JWT token va role (USER / LECTURER / ADMIN). " +
                    "Dung token nay trong header: Authorization: Bearer <token>"
    )

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req){
        LoginResponse resp = authService.login(req);
        if(resp.getToken() == null){
            return ResponseEntity.status(401).body(resp);

        }
        return ResponseEntity.ok(resp);
    }
}



