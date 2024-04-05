package fr.uga.l3miage.spring.tp2.exo1.controllers;

import fr.uga.l3miage.exo1.errors.NotFoundErrorResponse;
import fr.uga.l3miage.exo1.requests.PlaylistCreationRequest;
import fr.uga.l3miage.exo1.response.PlaylistResponseDTO;
import fr.uga.l3miage.exo1.response.SongResponseDTO;
import fr.uga.l3miage.spring.tp2.exo1.components.PlaylistComponent;
import fr.uga.l3miage.spring.tp2.exo1.components.SongComponent;
import fr.uga.l3miage.spring.tp2.exo1.models.PlaylistEntity;
import fr.uga.l3miage.spring.tp2.exo1.models.SongEntity;
import fr.uga.l3miage.spring.tp2.exo1.repositories.PlaylistRepository;
import fr.uga.l3miage.spring.tp2.exo1.repositories.SongRepository;
import fr.uga.l3miage.spring.tp2.exo1.services.PlaylistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@AutoConfigureTestDatabase
@AutoConfigureWebClient
public class PlaylistControllerTest {
    @SpyBean
    private PlaylistService playlistService;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PlaylistRepository playlistRepository;

    @SpyBean
    private PlaylistComponent playlistComponent;

    @SpyBean
    private SongComponent songComponent;

    @Autowired
    private SongRepository songRepository;

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


    @Test
    void getNotFoundPlaylist() {
        //Given
        final HttpHeaders headers = new HttpHeaders();

        final Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("idPlaylist", "ma playlist qui n'existe pas");

        NotFoundErrorResponse notFoundErrorResponseExpected = NotFoundErrorResponse
                .builder()
                .uri("/api/playlist/ma%20playlist%20qui%20n%27existe%20pas")
                .errorMessage("La playlist [ma playlist qui n'existe pas] n'a pas été trouvé")
                .build();

        //when
        ResponseEntity<NotFoundErrorResponse> response = testRestTemplate.exchange("/api/playlist/{idPlaylist}", HttpMethod.GET, new HttpEntity<>(null, headers), NotFoundErrorResponse.class, urlParams);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).usingRecursiveComparison()
                .isEqualTo(notFoundErrorResponseExpected);
    }

    @Test
    void getPlaylistFound() {
        //Given
        final PlaylistCreationRequest request = PlaylistCreationRequest
                .builder()
                .name("Summer vibes")
                .description("une playlist de test")
                .songsIds(Set.of())
                .build();

        final HttpHeaders headers = new HttpHeaders();

        final Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("idPlaylist", "Summer vibes");



        //when
        //ici soit je crée ma plalylist via son service ou sinon je vise une autre fois l'endpoint de création testé précédement pour la créer.
        PlaylistResponseDTO expectedResponse= playlistService.createPlaylist(request);
        ResponseEntity<PlaylistResponseDTO> response = testRestTemplate.exchange("/api/playlist/{idPlaylist}", HttpMethod.GET, new HttpEntity<>(null, headers), PlaylistResponseDTO.class, urlParams);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(playlistService, times(1)).createPlaylist(ArgumentMatchers.any(PlaylistCreationRequest.class));
        verify(playlistComponent, times(1)).createPlaylistEntity(ArgumentMatchers.any(PlaylistEntity.class));



    }

    @Transactional // j'ai rajouté transactional parce que par défaut spring fait un LAZY LOADING pour les collections d'une relation [chargement lorsque il y a un besoin], ce qui provoque une erreur lors de l'exécution car on cherche dans une collection qui n'est pas encore chargée.
    @Test
    void addSongInPlaylistPossible(){
        //Given      -- create both playlist and song entities

        final PlaylistCreationRequest playlist= PlaylistCreationRequest.builder()
                .name("HitList")
                .description("La playliste de l'été ")
                .songsIds(Set.of())
                .build();
        final SongEntity song=SongEntity.builder().title("last dance").build();

        final HttpHeaders headers = new HttpHeaders();
        final Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("idPlaylist", "HitList");
        urlParams.put("idSong", "last dance");

        //When

        //save our playslit and song firstly
        PlaylistResponseDTO registeredPlaylist = playlistService.createPlaylist(playlist);
        SongEntity registeredSong= songRepository.save(song);

        //Add song to playlist through its service
        PlaylistResponseDTO expectedResponse = playlistService.addSongInPlaylist(registeredPlaylist.getName(),registeredSong.getTitle());

        //targeting the endpoint
        ResponseEntity <PlaylistResponseDTO> response  = testRestTemplate.exchange("/api/playlist/{idPlaylist}/add?idSong={idSong}",HttpMethod.PATCH,new HttpEntity<>(null),PlaylistResponseDTO.class,urlParams);

        //Then  -- assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(playlistService,times(2)).addSongInPlaylist(anyString(),anyString());









    }

}