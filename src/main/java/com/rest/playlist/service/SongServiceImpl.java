package com.rest.playlist.service;

import com.rest.playlist.enums.MelodyType;
import com.rest.playlist.enums.SongCategory;
import com.rest.playlist.web.exception.FormatNotValidException;
import com.rest.playlist.web.exception.ResourceNotFoundException;
import com.rest.playlist.model.Song;
import com.rest.playlist.repository.SongRepository;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class SongServiceImpl implements ISongService {
    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    private final SongRepository songRepository;

    public SongServiceImpl(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByCategory(String category) {
        SongCategory searchedCategory = EnumUtils.getEnumIgnoreCase(SongCategory.class, category);
        if (searchedCategory == null) {
            throw new ResourceNotFoundException("Not found Category with value = " + category);
        }
        return songRepository.findSongsByCategory(searchedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByArtistName(String artistName) {
        return songRepository.findSongsByArtistName(artistName);
    }

    @Override
    @Transactional(readOnly = true)
    public Song getSongsByMelodyId(Long id) {
        return songRepository.findSongByMelody_Id(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with melody id = " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByMelodyType(String type) {
        MelodyType searchedType = EnumUtils.getEnumIgnoreCase(MelodyType.class, type);
        if (searchedType == null) {
            throw new ResourceNotFoundException("Not found type Melody with value = " + type);
        }
        return songRepository.findSongsByMelody_Type(searchedType);
    }

    @Override
    @Transactional(readOnly = true)
    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + id));
    }

    @Override
    public Song createSong(Song song) {
        if(!StringUtils.hasText(song.getMelody().getPitch())){
            throw new FormatNotValidException("Melody : pitch ne doit pas être null ou vide");
        }
        if(!StringUtils.hasText(song.getMelody().getDuration())){
            throw new FormatNotValidException("Melody : duration ne doit pas être null ou vide");
        }
        if(song.getMelody().getType() == null){
            throw new FormatNotValidException("Melody : type ne doit pas être null");
        }
        return songRepository.save(song);
    }

    @Override
    public Song updateSong(Song song) {

        Song searchedSong = songRepository.findById(song.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + song.getId()));

        searchedSong.setTitle(song.getTitle());
        searchedSong.setDescription(song.getDescription());
        searchedSong.setArtistName(song.getArtistName());
        searchedSong.setCategory(song.getCategory());
        searchedSong.setDuration(song.getDuration());

        return songRepository.saveAndFlush(song);
    }

    @Override
    public void deleteSongById(Long id) {
        songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + id));

        songRepository.deleteById(id);
    }
}
