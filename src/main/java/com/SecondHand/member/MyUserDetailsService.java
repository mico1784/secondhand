package com.SecondHand.member;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        // DB에서 username을 가진 유저를 찾아서

        // return new User(유저아이디, 비밀번호, 권한)

        Optional<User> result = userRepository.findByUsername(username);

        if(result.isEmpty()) {

            throw new UsernameNotFoundException("아이디가 존재하지 않습니다.");
        }
        var user = result.get();

        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("일반유저"));

        var a = new CustomUser(user.getUsername(),user.getPassword(),authorities);
        a.username = user.getUsername();
        a.id = user.getId();
        return a;
    }



}