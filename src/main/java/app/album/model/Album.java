package app.album.model;

import app.favourite.model.FavouriteAlbum;
import app.review.model.Review;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String albumName;

    @Column(nullable = false)
    private String artistName;

    @Column
    private String description;

    @Column
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column
    private String albumCover;

    @Column
    private String releaseDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlbumStatus albumStatus;

    @Column(nullable = false)
    private LocalDateTime createdOn;


    @OneToMany(mappedBy = "album", fetch = FetchType.EAGER)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "album", fetch = FetchType.EAGER)
    private List<FavouriteAlbum> favouriteAlbums = new ArrayList<>();

    @ManyToOne
    private User user;



}
