package com.SecondHand.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // 이 클래스가 서비스 컴포넌트임을 나타냄
@Primary // 동일한 타입의 여러 Bean 중 우선적으로 사용됨
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired // UserRepository를 자동 주입받음
    private UserRepository userRepository;

    // UserDetailsService 인터페이스 메서드: username을 기반으로 사용자 정보를 로드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 username을 기반으로 User 객체를 조회
        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 조회된 User 객체를 PrincipalDetails로 감싸서 반환
        return new PrincipalDetails(userEntity);
    }
}
