package fr.uga.l3miage.spring.tp2.exo1.services;


import fr.uga.l3miage.exo1.requests.PlaylistCreationRequest;
import fr.uga.l3miage.exo1.response.PlaylistResponseDTO;
import fr.uga.l3miage.spring.tp2.exo1.components.PlaylistComponent;
import fr.uga.l3miage.spring.tp2.exo1.components.SongComponent;
import fr.uga.l3miage.spring.tp2.exo1.exceptions.rest.AddingSongRestException;
import fr.uga.l3miage.spring.tp2.exo1.exceptions.rest.NotFoundEntityRestException;
import fr.uga.l3miage.spring.tp2.exo1.exceptions.technical.NotFoundPlaylistEntityException;
import fr.uga.l3miage.spring.tp2.exo1.exceptions.technical.NotFoundSongEntityException;
import fr.uga.l3miage.spring.tp2.exo1.mappers.PlaylistMapper;
import fr.uga.l3miage.spring.tp2.exo1.models.PlaylistEntity;
import fr.uga.l3miage.spring.tp2.exo1.models.SongEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.DURATION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PlaylistServiceTest {
    @Autowired
    private PlaylistService playlistService;

    @MockBean
    private PlaylistComponent playlistComponent;

    @MockBean
    private SongComponent songComponent;

    @SpyBean
    private PlaylistMapper playlistMapper;
    @Test
    void createPlaylist(){
        //given
        PlaylistCreationRequest playlistCreationRequest = PlaylistCreationRequest
                .builder()
                .name("test")
                .description("Une description de test")
                .songsIds(Set.of())
                .build();

        PlaylistEntity playlistEntity = playlistMapper.toEntity(playlistCreationRequest);
        playlistEntity.setSongEntities(Set.of());

        when(songComponent.getSetSongEntity(same(Set.of()))).thenReturn(Set.of());
        when(playlistComponent.createPlaylistEntity(any(PlaylistEntity.class))).thenReturn(playlistEntity);

        PlaylistResponseDTO responseExpected = playlistMapper.toResponse(playlistEntity);

        //when
        PlaylistResponseDTO response = playlistService.createPlaylist(playlistCreationRequest);

        // then
        assertThat(response).usingRecursiveComparison().isEqualTo(responseExpected);
        verify(playlistMapper,times(2)).toEntity(playlistCreationRequest);
        verify(playlistMapper,times(2)).toResponse(same(playlistEntity));
        verify(playlistComponent,times(1)).createPlaylistEntity(any(PlaylistEntity.class));
        verify(songComponent,times(1)).getSetSongEntity(Set.of());
    }
    @Test
    void getPlaylistFound() throws NotFoundPlaylistEntityException {
        //given
        PlaylistEntity playlist= PlaylistEntity.builder().name("HitList").songEntities(Set.of()).build();

        //when
        when(playlistComponent.getPlaylist(anyString())).thenReturn(playlist);

        PlaylistResponseDTO expectedResponse= playlistMapper.toResponse(playlist);
        PlaylistResponseDTO response =playlistService.getPlaylist("HitList");

        //then
        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
        verify(playlistMapper, times(2)).toResponse(same(playlist));
        verify(playlistComponent, times(1)).getPlaylist(anyString());





    }
    @Test
    void getPlaylistNotFound() throws NotFoundPlaylistEntityException {
        //when
        when(playlistComponent.getPlaylist(anyString())).thenThrow(new NotFoundPlaylistEntityException("Playlist not found"));

    //then
        assertThrows(NotFoundEntityRestException.class,()->playlistService.getPlaylist("Hit"));





    }

    @Test
    void  addSongInPlaylistPossible() throws NotFoundPlaylistEntityException, NotFoundSongEntityException {
        //given
        PlaylistEntity playlistEntity = PlaylistEntity.builder().name("HitList").songEntities(new HashSet<>()).build();
        SongEntity son= SongEntity.builder().title("felecita").build();
        playlistEntity.getSongEntities().add(son);
        playlistComponent.createPlaylistEntity(playlistEntity);


        //when
        when(songComponent.getSetSongEntity(anySet())).thenReturn(Set.of(son));
        when(songComponent.getSongEntityById(anyString())).thenReturn(son);
        when(playlistComponent.addSong(anyString(),any(SongEntity.class))).thenReturn(playlistEntity);

        PlaylistResponseDTO expectedResponse = playlistMapper.toResponse(playlistEntity);

        PlaylistResponseDTO response=playlistService.addSongInPlaylist("HitList","felecita");

        assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
        verify(playlistMapper,times(2)).toResponse(same(playlistEntity));
        verify(playlistComponent,times(1)).addSong(anyString(),any(SongEntity.class));





    }

    //Quand ce n'est pas possible, c'est parce que la playlist ou le son n'est pas trouvé
   @Test
    void  addSongInPlaylistNotPossibleBeacuseOfNotFoundSong() throws NotFoundSongEntityException, NotFoundPlaylistEntityException {

       //given
       PlaylistEntity playlistEntity = PlaylistEntity.builder().name("HitList").songEntities(new HashSet<>()).build();

       //when
       when(songComponent.getSetSongEntity(anySet())).thenReturn(Set.of());
       when(songComponent.getSongEntityById(anyString())).thenThrow(new NotFoundSongEntityException ("Song not found with given id"));
       when(playlistComponent.addSong(anyString(),any(SongEntity.class))).thenThrow(new AddingSongRestException("Adding not possible, song not found !"));



       assertThrows(AddingSongRestException.class,()-> playlistService.addSongInPlaylist("HitList","felecita"));
       verify(playlistMapper,times(0)).toResponse(same(playlistEntity));
       verify(playlistComponent,times(0)).addSong(anyString(),any(SongEntity.class));
       verify(songComponent,times(1)).getSongEntityById(anyString());





   }

    @Test
    void  addSongInPlaylistNotPossibleBeacuseOfNotFoundPlaylist() throws NotFoundSongEntityException, NotFoundPlaylistEntityException {

        //given
        SongEntity son= SongEntity.builder().title("felecita").build();

        //when
        when(songComponent.getSetSongEntity(anySet())).thenReturn(Set.of(son));
        when(songComponent.getSongEntityById(anyString())).thenReturn(son);
        when(playlistComponent.getPlaylist(anyString())).thenThrow(new NotFoundPlaylistEntityException("playlist not found with given id"));
        when(playlistComponent.addSong(anyString(),any(SongEntity.class))).thenThrow(new AddingSongRestException("Adding not possible, playlist not found !"));



        assertThrows(AddingSongRestException.class,()-> playlistService.addSongInPlaylist("HitList","felecita"));
        verify(playlistComponent,times(1)).addSong(anyString(),any(SongEntity.class));
        verify(playlistComponent,times(0)).getPlaylist(anyString());
        verify(songComponent,times(1)).getSongEntityById(anyString());




    }






}
