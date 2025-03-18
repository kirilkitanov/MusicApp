package app.album.service;

import app.album.repository.AlbumRepository;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserService userService;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, UserService userService) {
        this.albumRepository = albumRepository;
        this.userService = userService;
    }




}
