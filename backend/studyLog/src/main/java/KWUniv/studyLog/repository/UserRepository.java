package KWUniv.studyLog.repository;

import KWUniv.studyLog.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    @Autowired
    EntityManager em;

    public User findUserById(String userId){
        return em.find(User.class,userId);
    }

    @Transactional
    public void save(User user) {
        em.persist(user);
    }
}
