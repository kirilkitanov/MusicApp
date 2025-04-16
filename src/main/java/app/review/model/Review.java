package app.review.model;

import app.album.model.Album;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private boolean reported = false;

    @Enumerated(EnumType.STRING)
    private ReportReason reportReason;

    @ManyToOne
    private User user;

    @ManyToOne
    private Album album;
}
