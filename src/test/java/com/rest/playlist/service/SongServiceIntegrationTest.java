package com.rest.playlist.service;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.enums.MelodyType;
import com.rest.playlist.web.exception.ResourceNotFoundException;
import com.rest.playlist.model.Melody;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.SongRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest
@Transactional
@RunWith(SpringRunner.class)
public class SongServiceIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(SongServiceIntegrationTest.class);

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongServiceImpl songService;

    private Song defaultSong;

    @Before
    public void setup() {

        Melody melody = new Melody();
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);

        Song mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(melody);

        defaultSong = songRepository.saveAndFlush(mySong);

    }

    @Test
    public void testGetAllSongs() {
        List<Song> songs = songService.getAllSongs();
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetSongsByCategory() {
        List<Song> songs = songService.getSongsByCategory("POP");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingCategory() {
        songService.getSongsByCategory("Popy");
    }

    @Test
    public void testGetSongsWithNonExistingCategoryV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> songService.getSongById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found song with id = 4000");
    }

    @Test
    public void testGetSongsByArtistName() {
        List<Song> songs = songService.getSongsByArtistName("Sam Smith");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetSongsByMelodyId() {
        Song song = songService.getSongsByMelodyId(defaultSong.getMelody().getId());
        assertThat(song).isNotNull();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongWithNonExistingMelodyId() {
        songService.getSongsByMelodyId(2000L);
    }

    @Test
    public void testGetSongsByMelodyType() {
        List<Song> songs = songService.getSongsByMelodyType("COLOR");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingMelodyType() {
        songService.getSongsByMelodyType("COLORRS");
    }

    @Test
    public void testGetSongById() {
        Song song = songService.getSongById(defaultSong.getId());
        assertThat(song).isNotNull();
        assertThat(song.getId()).isNotNull();
        assertThat(song.getId()).isEqualTo(defaultSong.getId());
        assertThat(song.getTitle()).isEqualTo(defaultSong.getTitle());
        assertThat(song.getDescription()).isEqualTo(defaultSong.getDescription());
        assertThat(song.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(song.getArtistName()).isEqualTo(defaultSong.getArtistName());
        assertThat(song.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(song.getMelody().getPitch()).isEqualTo(defaultSong.getMelody().getPitch());
        assertThat(song.getMelody().getDuration()).isEqualTo(defaultSong.getMelody().getDuration());
        assertThat(song.getMelody().getType()).isEqualTo(defaultSong.getMelody().getType());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongWithNonExistingId() {
        songService.getSongById(4000L);
    }

    @Test
    public void testCreateSong() {
        Song savedSong = songService.createSong(defaultSong);
        assertThat(savedSong).isNotNull();
        assertThat(savedSong.getId()).isNotNull();
        assertThat(savedSong.getTitle()).isEqualTo(defaultSong.getTitle());
        assertThat(savedSong.getDescription()).isEqualTo(defaultSong.getDescription());
        assertThat(savedSong.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(savedSong.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(savedSong.getArtistName()).isEqualTo(defaultSong.getArtistName());
        assertThat(savedSong.getMelody().getPitch()).isEqualTo(defaultSong.getMelody().getPitch());
        assertThat(savedSong.getMelody().getDuration()).isEqualTo(defaultSong.getMelody().getDuration());
        assertThat(savedSong.getMelody().getType()).isEqualTo(defaultSong.getMelody().getType());
    }

    @Test
    public void testUpdateSong() {
        defaultSong.setTitle("Broken");
        defaultSong.setDescription("Isak Album");
        defaultSong.setArtistName("Isak Danielson");
        defaultSong.getMelody().setType(MelodyType.BLENDS);

        Song updatedSong = songService.updateSong(defaultSong);
        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong.getId()).isNotNull();
        assertThat(updatedSong.getTitle()).isEqualTo("Broken");
        assertThat(updatedSong.getDescription()).isEqualTo("Isak Album");
        assertThat(updatedSong.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(updatedSong.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(updatedSong.getArtistName()).isEqualTo("Isak Danielson");
        assertThat(updatedSong.getMelody().getPitch()).isEqualTo("Melody Pitch");
        assertThat(updatedSong.getMelody().getDuration()).isEqualTo("03:56");
        assertThat(updatedSong.getMelody().getType()).isEqualTo(defaultSong.getMelody().getType());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateSongWithNonExistingId() {
        defaultSong.setId(4000L);
        songService.updateSong(defaultSong);

    }

    @Test
    public void testDeleteSongById() {
        songService.deleteSongById(defaultSong.getId());
        Optional<Song> deletedSong = songRepository.findById(defaultSong.getId());
        assertThat(deletedSong.isPresent()).isFalse();

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeleteSongWithNonExistingId() {
        songService.deleteSongById(4000L);

    }

}
