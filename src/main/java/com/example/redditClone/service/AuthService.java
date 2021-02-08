package com.example.redditClone.service;

import com.example.redditClone.dto.RegisterRequest;
import com.example.redditClone.exception.SpringRedditException;
import com.example.redditClone.model.NotificationEmail;
import com.example.redditClone.model.User;
import com.example.redditClone.model.VerificationToken;
import com.example.redditClone.repository.UserRepository;
import com.example.redditClone.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;

    @Transactional
    public void signup(RegisterRequest registerRequest){

        //create new user and set its properties
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);//save the user in the repository

        //generate a token
        String token = generateVerificationToken(user);

        //send the email and verify
        mailService.sendMail(new NotificationEmail("Please activate your account", user.getEmail(),"Thank you for " +
                "signing up with reddit. Please click on the below url to activate your account"+ "http://localhost:8080/api/auth/accountVerification/"+ token));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken= verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(()->new SpringRedditException("Invalid Token"));
        fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user= userRepository.findByUsername(username).orElseThrow(()->new SpringRedditException("User not found with name "+ username));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
