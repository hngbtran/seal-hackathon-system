package com.minhtung.hackathon.service;


import com.minhtung.hackathon.entity.User;
import com.minhtung.hackathon.repository.UserRepository;
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
            return "Token khong hop le" ;

        }
        if (user.isExpired()){
            userRepository.delete(user);
            return "Link da het han . Vui long dang ki lai" ;

        }
        User users = new User() ;
        users.setActive(true);
        users.setToken(null);
        users.setExpiredAt(null);
        userRepository.save(user) ;

        return "Tai khoan da duoc kich hoat .Ban co the dang nhap ngay " ;
    }
    private String hashPassword(String password) {
        return password;
    }

}
