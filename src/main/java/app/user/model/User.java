package app.user.model;


import app.album.model.Album;
import app.favorite.model.FavouriteAlbum;
import app.review.model.Review;
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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role; //ADMIN, FAN, ARTIST;

    @Column
    private String firstName;

    @Column
    private String lastName;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    @OrderBy("createdOn DESC")
    private List<Album> albums = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<FavouriteAlbum> favouriteAlbums = new ArrayList<>();



}
