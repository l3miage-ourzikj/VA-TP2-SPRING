package fr.uga.l3miage.spring.tp2.exo1.components;

import fr.uga.l3miage.spring.tp2.exo1.exceptions.technical.NotFoundSongEntityException;
import fr.uga.l3miage.spring.tp2.exo1.models.SongEntity;
import fr.uga.l3miage.spring.tp2.exo1.repositories.SongRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SongComponentTest {

    @Autowired
    private SongComponent songComponent;
    @MockBean
    private SongRepository songRepository;

    @Test
    void getSongEntityByIdNotFound(){
        //given
        when(songRepository.findById(anyString())).thenReturn(Optional.empty());

        //when - then qui s'exécutent au même temps
        assertThrows(NotFoundSongEntityException.class, ()-> songComponent.getSongEntityById("yesterday") );

    }
    @Test
    void getSongEntityByIdFound(){
        //given
        SongEntity son = SongEntity.builder()
                        .title("felecita")
                        .duration(Duration.ZERO)
                        .build();


        when(songRepository.findById(anyString())).thenReturn(Optional.of(son));

        //when - then qui s'exécutent au même temps
        assertDoesNotThrow(()->songComponent.getSongEntityById("felecita") );

    }
    @Test
    void getSetSongEntityNotFound(){

        //given
        when(songRepository.findAllByTitleIsIn(anySet())).thenReturn(Collections.emptySet());

        //when
        Set<SongEntity> response= songComponent.getSetSongEntity(anySet());

        //then
        assertThat(response).isEmpty();
    }
    @Test
    void getSetSongEntityFound(){
        //given
        SongEntity song1 = SongEntity.builder().title("Ti amo").build();
        Set<String> ids=new HashSet<>();
        ids.add("Cantare");


        when(songRepository.findAllByTitleIsIn(anySet())).thenReturn(Set.of(song1));


        //when
        Set<SongEntity> response= songComponent.getSetSongEntity(ids);

        //then
        assertThat(response).isNotEmpty();
    }



}
