package fr.uga.l3miage.spring.tp2.exo1.controllers;

import fr.uga.l3miage.exo1.requests.PlaylistCreationRequest;
import fr.uga.l3miage.exo1.response.PlaylistResponseDTO;
import fr.uga.l3miage.spring.tp2.exo1.components.PlaylistComponent;
import fr.uga.l3miage.spring.tp2.exo1.components.SongComponent;
import fr.uga.l3miage.spring.tp2.exo1.models.PlaylistEntity;
import fr.uga.l3miage.spring.tp2.exo1.repositories.PlaylistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@AutoConfigureTestDatabase
@AutoConfigureWebClient
public class PlaylistControllertest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PlaylistRepository playlistRepository;

    @SpyBean
    private PlaylistComponent playlistComponent;

    @SpyBean
    private SongComponent songComponent;

    //méthode pour supprimer les données en base qui ne sont pas supprimées entre deux tests. [car ici on ne s'isole pas de la BD comme on fait un test d'intégration ]
    @AfterEach
    public void clear() {
        playlistRepository.deleteAll();

    }

    @Test
    void canCreatePlaylistWithoutSong() {
        //given
        final HttpHeaders headers = new HttpHeaders();

        final PlaylistCreationRequest request = PlaylistCreationRequest
                .builder()
                .name("Test playlist")
                .description("une playlist de test")
                .songsIds(Set.of())
                .build();


        // when
        ResponseEntity<PlaylistResponseDTO> response = testRestTemplate.exchange("/api/playlist/create", HttpMethod.POST, new HttpEntity<>(request, headers), PlaylistResponseDTO.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(playlistRepository.count()).isEqualTo(1);
        verify(playlistComponent, times(1)).createPlaylistEntity(ArgumentMatchers.any(PlaylistEntity.class));
        verify(songComponent, times(1)).getSetSongEntity(Set.of());
    }




}