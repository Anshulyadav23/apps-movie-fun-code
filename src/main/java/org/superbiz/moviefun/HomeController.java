package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final TransactionTemplate albumsTransactionTemplate;
    private final TransactionTemplate moviesTransactionTemplate;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures, TransactionTemplate albumsTransactionTemplate, TransactionTemplate moviesTransactionTemplate) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.albumsTransactionTemplate = albumsTransactionTemplate;
        this.moviesTransactionTemplate = moviesTransactionTemplate;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {
        moviesTransactionTemplate.execute(status -> {
            for (Movie movie : movieFixtures.load()) {
                moviesBean.addMovie(movie);
            }
            return null;
        });

        albumsTransactionTemplate.execute(status -> {
            for (Album album : albumFixtures.load()) {
                System.out.println("Album" + album);
                albumsBean.addAlbum(album);
            }
            return null;
        });

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
