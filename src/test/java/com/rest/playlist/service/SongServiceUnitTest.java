package com.rest.playlist.service;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.enums.MelodyType;
import com.rest.playlist.exception.ResourceNotFoundException;
import com.rest.playlist.model.Melody;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.SongRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class SongServiceUnitTest {

    private final static Logger log = LoggerFactory.getLogger(SongServiceUnitTest.class);

    @MockBean
    private SongRepository songRepository;

    private SongServiceImpl songService;

    private Song mySong;
    private List<Song> songList = new ArrayList<>();


    @Before
    public void setup() {
        songService = new SongServiceImpl(songRepository);

        Melody myMelody = new Melody();
        myMelody.setPitch("Melody Pitch");
        myMelody.setDuration("03:56");
        myMelody.setType(MelodyType.COLOR);

        mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(myMelody);
    }

    @Test
    public void testGetAllSongs() {
        songRepository.save(mySong);
        when(songRepository.findAll()).thenReturn(songList);

        //test
        List<Song> songs = songService.getAllSongs();
        assertEquals(songs, songList);
        verify(songRepository, times(1)).save(mySong);
        verify(songRepository, times(1)).findAll();
    }

    @Test
    public void testGetSongsByCategory() {
        songList.add(mySong);
        when(songRepository.findSongsByCategory(SongCategory.POP)).thenReturn(songList);

        //test
        List<Song> songs = songService.getSongsByCategory("POP");
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByCategory(SongCategory.POP);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistCategory() {
        List<Song> songs = songService.getSongsByCategory("Popy");
        assertTrue(songs.isEmpty());
    }

    @Test
    public void testGetSongsByArtistName() {
        songList.add(mySong);
        when(songRepository.findSongsByArtistName(mySong.getArtistName())).thenReturn(songList);
        List<Song> songs = songService.getSongsByArtistName(mySong.getArtistName());

        //test
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByArtistName(mySong.getArtistName());
    }

    @Test
    public void testGetSongsByMelodyId() {
        when(songRepository.findSongByMelody_Id(mySong.getMelody().getId())).thenReturn(Optional.of(mySong));
        Song song = songService.getSongsByMelodyId(mySong.getMelody().getId());

        //test
        assertThat(song).isNotNull();
        assertThat(song).isEqualTo(mySong);
        assertThat(song.getId()).isNotNull();
        assertThat(song.getId()).isEqualTo(mySong.getId());
        assertThat(song.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(song.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(song.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(song.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(song.getArtistName()).isEqualTo(mySong.getArtistName());
        assertThat(song.getMelody().getPitch()).isEqualTo(mySong.getMelody().getPitch());
        assertThat(song.getMelody().getDuration()).isEqualTo(mySong.getMelody().getDuration());
        assertThat(song.getMelody().getType()).isEqualTo(mySong.getMelody().getType());
        verify(songRepository, times(1)).findSongByMelody_Id(mySong.getMelody().getId());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingMelodyId() {
        when(songRepository.findSongByMelody_Id(2000L)).thenReturn(Optional.empty());
        songService.getSongsByMelodyId(2000L);
    }

    @Test
    public void testGetSongsByMelodyType() {
        songList.add(mySong);
        when(songRepository.findSongsByMelody_Type(MelodyType.COLOR)).thenReturn(songList);

        //test
        List<Song> songs = songService.getSongsByMelodyType("COLOR");
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByMelody_Type(MelodyType.COLOR);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistMelodyType() {
        List<Song> songs = songService.getSongsByMelodyType("COLORRRR");
        assertTrue(songs.isEmpty());
    }


    @Test
    public void testCreateSong() {
        when(songRepository.save(any(Song.class))).thenReturn(mySong);
        songService.createSong(mySong);
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    public void testUpdateSong() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.of(mySong));

        mySong.setTitle("Power");
        mySong.setDescription("power album");
        mySong.setArtistName("Isak Danielson");

        given(songRepository.saveAndFlush(mySong)).willReturn(mySong);

        Song updatedSong = songService.updateSong(mySong);

        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong).isEqualTo(mySong);
        assertThat(updatedSong.getId()).isNotNull();
        assertThat(updatedSong.getId()).isEqualTo(mySong.getId());
        assertThat(updatedSong.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(updatedSong.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(updatedSong.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(updatedSong.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(updatedSong.getArtistName()).isEqualTo(mySong.getArtistName());
        assertThat(updatedSong.getMelody().getPitch()).isEqualTo(mySong.getMelody().getPitch());
        assertThat(updatedSong.getMelody().getDuration()).isEqualTo(mySong.getMelody().getDuration());
        assertThat(updatedSong.getMelody().getType()).isEqualTo(mySong.getMelody().getType());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateSongWithNonExistingId() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.empty());
        songService.updateSong(mySong);

    }

    @Test
    public void testGetSongsById() {
        // when
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.ofNullable(mySong));
        Song foundSong = songService.getSongById(mySong.getId());

        //test - then
        assertThat(foundSong).isNotNull();
        assertThat(foundSong).isEqualTo(mySong);
        assertThat(foundSong.getId()).isNotNull();
        assertThat(foundSong.getId()).isEqualTo(1000L);
        assertThat(foundSong.getId()).isEqualTo(mySong.getId());
        assertThat(foundSong.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(foundSong.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(foundSong.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(foundSong.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(foundSong.getArtistName()).isEqualTo(mySong.getArtistName());
        assertThat(foundSong.getMelody().getPitch()).isEqualTo(mySong.getMelody().getPitch());
        assertThat(foundSong.getMelody().getDuration()).isEqualTo(mySong.getMelody().getDuration());
        assertThat(foundSong.getMelody().getType()).isEqualTo(mySong.getMelody().getType());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingId() {

        // when
        when(songRepository.findById(4000L)).thenReturn(Optional.empty());
        songService.getSongById(4000L);
    }

    @Test
    public void testGetSongsWithNonExistingIdV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> songService.getSongById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found song with id = 4000");
    }

    @Test
    public void testDeleteSongById() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.of(mySong));
        songService.deleteSongById(mySong.getId());
        verify(songRepository, times(1)).deleteById(mySong.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeleteSongWithNonExistingId() {
        when(songRepository.findById(4000L)).thenReturn(Optional.empty());
        songService.deleteSongById(4000L);
    }
}
