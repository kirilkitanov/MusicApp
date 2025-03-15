package app.favorite.repository;

import app.favorite.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FavoriteRepository  extends JpaRepository<Favorite, UUID> {
}
