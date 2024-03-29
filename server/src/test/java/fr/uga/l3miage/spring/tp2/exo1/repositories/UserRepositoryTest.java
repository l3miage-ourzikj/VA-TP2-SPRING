package fr.uga.l3miage.spring.tp2.exo1.repositories;

import fr.uga.l3miage.spring.tp2.exo1.models.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Test
    void testRequestFindAllByMailContaining(){
        //given
        UserEntity userEntity = UserEntity
                .builder()
                .mail("test@gmail.com")
                .name("google 1")
                .build();

        UserEntity userEntity2 = UserEntity
                .builder()
                .mail("test2@gmail.com")
                .name("non google")
                .build();

        userRepository.save(userEntity);
        userRepository.save(userEntity2);
        // when
        Set<UserEntity> userEntitiesResponses = userRepository.findAllByMailContaining("gmail");
        //then
        assertThat(userEntitiesResponses).hasSize(2);
        assertThat(userEntitiesResponses.stream().findFirst().get().getMail()).isEqualTo("test@gmail.com");
    }


}


