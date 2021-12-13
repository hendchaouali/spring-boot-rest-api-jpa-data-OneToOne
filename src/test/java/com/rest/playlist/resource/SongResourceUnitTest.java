package com.rest.playlist.resource;

import com.rest.playlist.enums.MelodyType;
import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.exception.ResourceNotFoundException;
import com.rest.playlist.model.Melody;
import com.rest.playlist.model.Song;
import com.rest.playlist.service.ISongService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.rest.playlist.TestUtils.asJsonString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SongResource.class)
public class SongResourceUnitTest {

    private static final Logger log = LoggerFactory.getLogger(SongResourceUnitTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISongService songService;

    private Song mySong;
    private List<Song> songList = new ArrayList<>();


    @Before
    public void setup() {
        Melody melody = new Melody();
        melody.setId(2000L);
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);


        mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(melody);
    }

    @Test
    public void testGetAllSongs() throws Exception {
        songList.add(mySong);
        when(songService.getAllSongs()).thenReturn(songList);

        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
        verify(songService).getAllSongs();
        verify(songService, times(1)).getAllSongs();
    }

    @Test
    public void testGetNoContentSongs() throws Exception {
        when(songService.getAllSongs()).thenReturn(songList);

        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetSongsByCategory() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByCategory("POP")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/category/" + mySong.getCategory())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
    }

    @Test
    public void testGetNoContentSongsByCategory() throws Exception {
        when(songService.getSongsByCategory("CLASSICAL")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/category/CLASSICAL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test
    public void testGetSongsWithNonExistingCategory() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Category with value = popy")).when(songService).getSongsByCategory("popy");
        mockMvc.perform(get("/api/songs/category/popy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Category with value = popy"));
    }


    @Test
    public void testGetSongsByArtistName() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByArtistName(mySong.getArtistName())).thenReturn(songList);

        mockMvc.perform(get("/api/songs/artist/" + mySong.getArtistName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
    }

    @Test
    public void testGetNoContentSongsByArtistName() throws Exception {
        when(songService.getSongsByArtistName("Isak")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/artist/Isak")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetSongsByMelodyId() throws Exception {
        when(songService.getSongsByMelodyId(mySong.getMelody().getId())).thenReturn(mySong);

        mockMvc.perform(get("/api/songs/melody/id/" + mySong.getMelody().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(mySong.getTitle()))
                .andExpect(jsonPath("$.description").value(mySong.getDescription()))
                .andExpect(jsonPath("$.category").value(mySong.getCategory().toString()))
                .andExpect(jsonPath("$.artistName").value(mySong.getArtistName()))
                .andExpect(jsonPath("$.duration").value(mySong.getDuration()))
                .andExpect(jsonPath("$.melody.pitch").value(mySong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(mySong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.pitch").value(mySong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(mySong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.type").value(mySong.getMelody().getType().toString()));
    }


    @Test
    public void testGetSongByNonExistingMelodyId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found song with melody id = 2000")).when(songService).getSongsByMelodyId(2000L);
        mockMvc.perform(get("/api/songs/melody/id/2000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found song with melody id = 2000"));
    }


    @Test
    public void testGetSongsByMelodyType() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByMelodyType("COLOR")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/melody/type/" + mySong.getMelody().getType())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
    }

    @Test
    public void testGetNoContentSongsByMelodyType() throws Exception {
        when(songService.getSongsByMelodyType("DIRECTION")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/melody/type/DIRECTION")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test
    public void testGetSongsWithNonExistingMelodyType() throws Exception {
        doThrow(new ResourceNotFoundException("Not found type Melody with value = DIRECTyyy")).when(songService).getSongsByMelodyType("DIRECTyyy");
        mockMvc.perform(get("/api/songs/melody/type/DIRECTyyy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found type Melody with value = DIRECTyyy"));
    }


    @Test
    public void testGetSongById() throws Exception {
        mySong.setId(1000L);
        when(songService.getSongById(mySong.getId())).thenReturn(mySong);

        mockMvc.perform(get("/api/songs/" + mySong.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(mySong.getTitle()))
                .andExpect(jsonPath("$.description").value(mySong.getDescription()))
                .andExpect(jsonPath("$.category").value(mySong.getCategory().toString()))
                .andExpect(jsonPath("$.artistName").value(mySong.getArtistName()))
                .andExpect(jsonPath("$.duration").value(mySong.getDuration()))
                .andExpect(jsonPath("$.melody.pitch").value(mySong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(mySong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.type").value(mySong.getMelody().getType().toString()));
    }


    @Test
    public void testGetSongByNonExistingId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Song with id = 1000")).when(songService).getSongById(1000L);
        mockMvc.perform(get("/api/songs/1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Song with id = 1000"));
    }


    @Test
    public void testCreateSong() throws Exception {
        when(songService.createSong(any(Song.class))).thenReturn(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().isCreated());
        verify(songService, times(1)).createSong(any());
    }

    @Test
    public void testCreateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithTitleNull() throws Exception {
        mySong.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testUpdateSong() throws Exception {
        mySong.setId(1000L);
        when(songService.updateSong(mySong)).thenReturn(mySong);
        mockMvc.perform(put("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setId(1000L);
        mySong.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setId(1000L);
        mySong.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithTitleNull() throws Exception {
        mySong.setId(1000L);
        mySong.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }

    @Test
    public void testDeleteSongById() throws Exception {
        mySong.setId(1000L);
        doNothing().when(songService).deleteSongById(mySong.getId());
        mockMvc.perform(delete("/api/songs/" + mySong.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNotFoundSong() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Song with id = 1000")).when(songService).deleteSongById(1000L);
        mockMvc.perform(delete("/api/songs/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Song with id = 1000"));
    }


}
