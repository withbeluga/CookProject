package com.example.cook.follow.service;

import com.example.cook.follow.Follow;
import com.example.cook.follow.repository.FollowRepository;
import com.example.cook.user.User;
import com.example.cook.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowService {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

  public void follow(Long userIdToFollow) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    String followerEmail = userDetails.getUsername(); // 현재 로그인한 사용자의 이메일

    // 현재 로그인한 사용자 정보 가져오기
    User follower = userRepository.findByEmail(followerEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 팔로우 대상 사용자 정보 가져오기
    User following = userRepository.findById(userIdToFollow)
        .orElseThrow(() -> new RuntimeException("팔로우 대상 사용자를 찾을 수 없습니다."));

    // 팔로우 대상이 자기 자신인지 확인
    if (follower.getId().equals(userIdToFollow)) {
      throw new RuntimeException("자기 자신을 팔로우할 수 없습니다.");
    }

    Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(follower, following);

    // 이미 팔로우한 경우 팔로우 취소
    if (existingFollow.isPresent()) {
      followRepository.delete(existingFollow.get()); // 팔로우 관계 삭제
      followRepository.flush();
    } else {
      // 팔로우 관계가 없는 경우 새로운 팔로우 관계 생성
      Follow follow = new Follow();
      follow.setFollower(follower);
      follow.setFollowing(following);
      followRepository.save(follow);
    }
  }

}