package com.minhtung.hackathon.service;


import com.minhtung.hackathon.dto.LoginRequest;
import com.minhtung.hackathon.dto.LoginResponse;
import com.minhtung.hackathon.entity.User;
import com.minhtung.hackathon.enums.Role;
import com.minhtung.hackathon.repository.UserRepository;
import com.minhtung.hackathon.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder ;
    private final EmailService emailService ;
    private final JwtUtil jwtUtil;



    // nếu không verify thi sau .... xoa
    public String register(String username , String email , String password){
        if(userRepository.existsByEmail(email)){
            return "Email nay da duoc su dung " ;
        }
        //xoa pending cu neu co (Dang ki lai)
        User user = new User() ;
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setActive(false);//chua an xac nhan
        user.setToken(UUID.randomUUID().toString());
        user.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        user.setRole(Role.USER);
        userRepository.save(user);

        boolean sent = emailService.sendVerificationEmail(email,user.getToken()) ;
        return sent
                ? "Dang ky thanh cong! Kiem tra Gmail de xac nhan."
                : "Gui email that bai, thu lai sau.";

    }

    //buoc 2 thuc hien o day gui gmail va an xac nhan
    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByToken(token).orElse(null);

        if(user == null){
            return "Token khong hop le " ;

        }
        if (user.isExpired()){
            userRepository.delete(user);
            return "Link da het han . Vui long dang ki lai" ;
        }

        user.setActive(true);
        user.setToken(null);
        user.setExpiredAt(null);
        userRepository.save(user) ;

        return "Tai khoan da duoc kich hoat .Ban co the dang nhap ngay " ;
    }
    public LoginResponse login(LoginRequest req){
        User user = userRepository.findByUsername((req.getUsername())).orElse(null) ;

        if(user == null){
            return new LoginResponse(null,null,null,"tai khoan khong ton tai ");

        }
        if(!user.isActive()){
            return new LoginResponse(null,null,null,"tai khoan chua duoc kich hoat email ");
        }
        if (!req.getPassword().equals(user.getPassword())) {
            return new LoginResponse(null, null, null, "Mat khau khong chinh xac");
        }
        String jwt = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(
                jwt,
                user.getRole().name(),
                user.getUsername(),
                "Dang nhap thanh cong"
        );
    }

}
