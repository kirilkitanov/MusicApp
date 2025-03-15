package app.album.model;

import app.favorite.model.Favorite;
import app.review.model.Review;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column
    private String artistName;

    @Column
    @Enumerated(EnumType.STRING)
    private Genre genre; //ROCK, CLASSIC, POP, KPOP;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime releaseDate;

    @OneToMany(mappedBy = "album", fetch = FetchType.EAGER)
    private List<Review> reviews;

    @OneToMany(mappedBy = "album", fetch = FetchType.EAGER)
    private List<Favorite> favorites;

    @ManyToOne
    private User user;



}
