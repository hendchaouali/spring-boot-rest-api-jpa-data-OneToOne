package com.rest.playlist.repository;

import com.rest.playlist.enums.MelodyType;
import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.model.Melody;
import com.rest.playlist.model.Song;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SongRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(SongRepositoryTest.class);

    @Autowired
    SongRepository songRepository;
    private Song savedSong;

    @Before
    public void setupCreateSong() {

        Melody melody = new Melody();
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);

        Song song =  new Song();
        song.setTitle("For The Lover That I Lost");
        song.setDescription("Live At Abbey Road Studios");
        song.setCategory(SongCategory.POP);
        song.setArtistName("Sam Smith");
        song.setDuration("3:01");
        song.setMelody(melody);

        savedSong = songRepository.save(song);
        assertThat(savedSong).isNotNull();
        assertThat(savedSong).hasFieldOrPropertyWithValue("title", "For The Lover That I Lost");
        assertThat(savedSong).hasFieldOrPropertyWithValue("description", "Live At Abbey Road Studios");
        assertThat(savedSong).hasFieldOrPropertyWithValue("category", SongCategory.POP);
        assertThat(savedSong).hasFieldOrPropertyWithValue("duration", "3:01");
        assertThat(savedSong).hasFieldOrPropertyWithValue("artistName", "Sam Smith");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.pitch", "Melody Pitch");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.duration", "03:56");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.type", MelodyType.COLOR);
    }

    @Test
    public void shouldFindAllSongs() {
        List<Song> songs = songRepository.findAll();
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(songs.get(songs.size() - 1));
        assertThat(songs.get(songs.size() - 1).getId()).isNotNull();
    }

    @Test
    public void shouldFindSongsByCategory() {
        List<Song> songs = songRepository.findSongsByCategory(savedSong.getCategory());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongsByArtistName() {
        List<Song> songs = songRepository.findSongsByArtistName(savedSong.getArtistName());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongsByMelodyId() {
        Song song = songRepository.findSongByMelody_Id(savedSong.getMelody().getId()).orElse(null);
        assertThat(song).isNotNull();
        assertThat(song).isEqualTo(savedSong);
        assertThat(song).hasFieldOrPropertyWithValue("title", savedSong.getTitle());
        assertThat(song).hasFieldOrPropertyWithValue("description", savedSong.getDescription());
        assertThat(song).hasFieldOrPropertyWithValue("category", savedSong.getCategory());
        assertThat(song).hasFieldOrPropertyWithValue("duration", savedSong.getDuration());
        assertThat(song).hasFieldOrPropertyWithValue("artistName", savedSong.getArtistName());
        assertThat(song).hasFieldOrPropertyWithValue("melody.pitch", savedSong.getMelody().getPitch());
        assertThat(song).hasFieldOrPropertyWithValue("melody.duration", savedSong.getMelody().getDuration());
        assertThat(song).hasFieldOrPropertyWithValue("melody.type", savedSong.getMelody().getType());
    }

    @Test
    public void shouldFindSongsByMelodyType() {
        List<Song> songs = songRepository.findSongsByMelody_Type(savedSong.getMelody().getType());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongById() {
        Song foundSong = songRepository.findById(savedSong.getId()).orElse(null);
        assertThat(foundSong).isNotNull();
        assertThat(foundSong).isEqualTo(savedSong);
        assertThat(foundSong).hasFieldOrPropertyWithValue("title", savedSong.getTitle());
        assertThat(foundSong).hasFieldOrPropertyWithValue("description", savedSong.getDescription());
        assertThat(foundSong).hasFieldOrPropertyWithValue("category", savedSong.getCategory());
        assertThat(foundSong).hasFieldOrPropertyWithValue("duration", savedSong.getDuration());
        assertThat(foundSong).hasFieldOrPropertyWithValue("artistName", savedSong.getArtistName());
        assertThat(foundSong).hasFieldOrPropertyWithValue("melody.pitch", savedSong.getMelody().getPitch());
        assertThat(foundSong).hasFieldOrPropertyWithValue("melody.duration", savedSong.getMelody().getDuration());
        assertThat(foundSong).hasFieldOrPropertyWithValue("melody.type", savedSong.getMelody().getType());
    }

    @Test
    public void shoulCreateSong() {

        int sizeBeforeCreate = songRepository.findAll().size();

        Melody melody = new Melody();
        melody.setPitch("Melody Pitch test");
        melody.setDuration("07:56");
        melody.setType(MelodyType.DIRECTION);

        Song songToSave = new Song();
        songToSave.setTitle("The Falls");
        songToSave.setDescription("Album musical d'Ennio Morricone");
        songToSave.setCategory(SongCategory.CLASSICAL);
        songToSave.setArtistName("Morricone");
        songToSave.setDuration("7:10");
        songToSave.setMelody(melody);

        Song song = songRepository.save(songToSave);

        int sizeAfterCreate = songRepository.findAll().size();
        assertThat(sizeAfterCreate).isEqualTo(sizeBeforeCreate + 1);
        assertThat(song).isNotNull();
        assertThat(song).hasFieldOrPropertyWithValue("title", "The Falls");
        assertThat(song).hasFieldOrPropertyWithValue("description", "Album musical d'Ennio Morricone");
        assertThat(song).hasFieldOrPropertyWithValue("category", SongCategory.CLASSICAL);
        assertThat(song).hasFieldOrPropertyWithValue("duration", "7:10");
        assertThat(song).hasFieldOrPropertyWithValue("artistName", "Morricone");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.pitch", "Melody Pitch");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.duration", "03:56");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.type", MelodyType.COLOR);

    }

    @Test
    public void shouldUpdateSong() {

        Song foundSong = songRepository.getById(savedSong.getId());
        assertThat(foundSong).isNotNull();

        foundSong.setTitle("Power");
        foundSong.setDescription("power album");
        foundSong.setArtistName("Isak Danielson");
        foundSong.getMelody().setType(MelodyType.BLENDS);
        Song updatedSong = songRepository.save(foundSong);

        Song checkSong = songRepository.getById(updatedSong.getId());

        assertThat(checkSong.getId()).isNotNull();
        assertThat(checkSong.getId()).isEqualTo(updatedSong.getId());
        assertThat(checkSong.getTitle()).isEqualTo(updatedSong.getTitle());
        assertThat(checkSong.getDescription()).isEqualTo(updatedSong.getDescription());
        assertThat(checkSong.getCategory()).isEqualTo(updatedSong.getCategory());
        assertThat(checkSong.getDuration()).isEqualTo(updatedSong.getDuration());
        assertThat(checkSong.getArtistName()).isEqualTo(updatedSong.getArtistName());
    }

    @Test
    public void shouldDeleteSonById() {
        int sizeBeforeDelete = songRepository.findAll().size();

        Song foundSong = songRepository.getById(savedSong.getId());
        assertThat(foundSong).isNotNull();

        songRepository.deleteById(foundSong.getId());

        int sizeAfterDelete = songRepository.findAll().size();

        assertThat(sizeAfterDelete).isEqualTo(sizeBeforeDelete - 1);
    }

}
