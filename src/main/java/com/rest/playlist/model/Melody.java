package com.rest.playlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rest.playlist.enums.MelodyType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "melody")
public class Melody extends AbstractAuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MELODY_SEQ")
    @SequenceGenerator(name = "MELODY_SEQ", sequenceName = "melody_seq", allocationSize = 1)
    private Long id;

    @Column(name = "pitch")
    @NotBlank(message = "pitch ne doit pas être null ou vide")
    @Size(min = 3, max = 50, message = "titre doit être compris entre 3 et 50 caractères")
    private String pitch;

    @Column(name = "duration")
    @NotBlank(message = "duration ne doit pas être nulle ou vide")
    private String duration;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "type melody<COLOR, BLENDS, DIRECTION> ne doit pas être null")
    private MelodyType type;

    @OneToOne(mappedBy = "melody", orphanRemoval = true)
    @JsonIgnore
    private Song song;
}
