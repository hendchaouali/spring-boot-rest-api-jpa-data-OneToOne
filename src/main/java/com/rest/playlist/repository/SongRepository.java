package com.rest.playlist.repository;

import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.enums.MelodyType;
import com.rest.playlist.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaAuditing
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findSongsByCategory(SongCategory category);
    List<Song> findSongsByArtistName(String artistName);
    Optional<Song> findSongByMelody_Id(Long id);
    List<Song> findSongsByMelody_Type(MelodyType type);
}
