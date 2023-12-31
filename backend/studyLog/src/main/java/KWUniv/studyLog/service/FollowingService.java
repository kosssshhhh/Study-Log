package KWUniv.studyLog.service;

import KWUniv.studyLog.DTO.FollowingDTO;
import KWUniv.studyLog.DTO.UserDTO;
import KWUniv.studyLog.entity.Following;
import KWUniv.studyLog.entity.User;
import KWUniv.studyLog.repository.FollowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private final FollowingRepository followingRepository;
    private final UserService userService;

    /*
    userId로 followings 가져오기
     */
    public List<Following> getFollowingsByUserId(String userId) {
        return followingRepository.findBySelfUser_UserId(userId);
    }

    /*
    해당 유저가 팔로잉하는 userId를 가져오기
    - List로 반환
     */
    public List<String> getFollowingUserIds(String userId) {
        List<Following> followings = getFollowingsByUserId(userId);

        return followings.stream()
                .map(following -> following.getFollowingUser().getUserId())
                .collect(Collectors.toList());
    }


    /*
    selfId의 팔로잉 + 1, followingId의 팔로워 + 1
     */
    public void plusFollowingCount(User selfUser, User followingUser) {
        selfUser.plusFollowingCount();
        followingUser.plusFollowerCount();
    }

    /*
    selfId의 팔로잉 -1, followingId의 팔로워 -1
     */
    public void minusFollowingCount(User selfUser, User followingUser) {
        selfUser.minusFollowingCount();
        followingUser.minusFollowerCount();
    }

    /*
    self와 following을 찾고, 만약 둘다 값이 있으면 Following 객체를 만들어 저장
    - 만약 self와 following 둘 중 하나라도 없으면 400 반환
    - 성공적이면 200 반환
     */
    @Transactional
    public void findSaveFollowingAndSelf(FollowingDTO followingDTO) {
        User selfUser = userService.findUserById(followingDTO.getSelfId());
        User followingUser = userService.findUserById(followingDTO.getFollowingId());

        //각자 follower, following + 1
        plusFollowingCount(selfUser, followingUser);

        //Following DB 저장
        Following following = new Following(selfUser, followingUser);
        followingRepository.save(following);
    }

    /*
    유저 언팔로우 하기
     */
    @Transactional
    public void unfollowUser(FollowingDTO followingDTO) {
        User selfUser = userService.findUserById(followingDTO.getSelfId());
        User followingUser = userService.findUserById(followingDTO.getFollowingId());

        minusFollowingCount(selfUser, followingUser);

        Following following = followingRepository.findFollowingBySelfUserAndFollowingUser(selfUser, followingUser);
        followingRepository.delete(following);
    }

    /*
    해당 팔로우가 존재하는지 찾기
     */
    @Transactional
    public boolean isFollowing(String selfId, String followingId) {
        Optional<Following> isFollowing = Optional.ofNullable(followingRepository.findFollowingBySelfUser_UserIdAndFollowingUser_UserId(selfId, followingId));
        if(isFollowing.isPresent()) {
            return true;
        } else{
            return false;
        }
    }

    /*
    팔로잉 리스트 반환 메서드
    - 자신이 팔로잉 하는 사람들 반환
     */
    @Transactional
    public Map<String, Object> getFollowingList(String selfId) {
        List<Following> followingList = followingRepository.findBySelfUser_UserId(selfId);
        Map<String, Object> response = new HashMap<>();

        List<UserDTO> followingIds = followingList.stream()
                .map(following -> new UserDTO(following.getFollowingUser()))
                .collect(Collectors.toList());

        response.put("selfId", selfId);
        response.put("followings", followingIds);
        return response;
    }

    /*
    팔로워 리스트 반환 메서드
    - 자신을 팔로잉 하는 사람들 반환
     */
    @Transactional
    public Map<String, Object> getFollowerList(String followingId) {
        List<Following> followerList = followingRepository.findByFollowingUser_UserId(followingId);
        Map<String, Object> response = new HashMap<>();

        List<UserDTO> followerIds = followerList.stream()
                .map(following -> new UserDTO(following.getSelfUser()))
                .collect(Collectors.toList());

        response.put("followingId", followingId);
        response.put("followers", followerIds);
        return response;
    }

}
