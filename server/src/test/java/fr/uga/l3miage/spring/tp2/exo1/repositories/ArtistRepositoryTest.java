package fr.uga.l3miage.spring.tp2.exo1.repositories;

import fr.uga.l3miage.exo1.enums.GenreMusical;
import fr.uga.l3miage.spring.tp2.exo1.models.ArtistEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
public class ArtistRepositoryTest {
    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void countByGenreMusicalEquals(){
        //given
        ArtistEntity artist1= ArtistEntity
                .builder()
                .name("John")
                .genreMusical(GenreMusical.RAP)
                .build();

        ArtistEntity artist2= ArtistEntity
                .builder()
                .name("Dangelo")
                .genreMusical(GenreMusical.CLASSIC)
                .build();

        artistRepository.save(artist1);
        artistRepository.save(artist2);

        //when
        int response= artistRepository.countByGenreMusicalEquals(GenreMusical.CLASSIC);

        //then
        assertThat(response).isEqualTo(1);

    }
}

