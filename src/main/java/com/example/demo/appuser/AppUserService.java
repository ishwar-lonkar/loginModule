package com.example.demo.appuser;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.registration.token.ConfirmationToken;

import com.example.demo.registration.token.ConfirmationTokenService;


@Service
public class AppUserService implements UserDetailsService {
	
	private final static String USER_NOT_FOUND_MSG = "User with email %s not found";
	
	private final AppUserRepository appUserRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final ConfirmationTokenService confirmationTokenService;
	
	
	
	public AppUserService(AppUserRepository appUserRepository, BCryptPasswordEncoder bCryptPasswordEncoder,ConfirmationTokenService confirmationTokenService) {
		this.appUserRepository = appUserRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.confirmationTokenService = confirmationTokenService;
	}



	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return appUserRepository.findByEmail(email)
				.orElseThrow(()-> new UsernameNotFoundException(
						String.format(USER_NOT_FOUND_MSG, email)));
	}
	
	
	public String signUpUser(AppUser appUser) {
		boolean userExists = appUserRepository
				.findByEmail(appUser.getUsername())
				.isPresent();
		if(userExists) {
			throw new IllegalStateException("Email Already Taken");
		}
		
		String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());
	
		appUser.setPassword(encodedPassword);
		
		appUserRepository.save(appUser);
		
		
		String token = UUID.randomUUID().toString();
		
		ConfirmationToken confirmationToken = new ConfirmationToken(
				token,
				LocalDateTime.now(),
				LocalDateTime.now().plusMinutes(15),
				appUser
		);
		
		confirmationTokenService.saveConfirmationToken(confirmationToken);
		
		//TODO: SEND EMAIL
		
		return token;
	}
	
	public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }

}
