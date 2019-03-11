package musicofspheres.cms.controllers.music;

import musicofspheres.cms.database.music.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@CrossOrigin
public class MusicHandler {

    private MusicService service;
    @Value("${files.root}")
    String root;

    @Autowired
    public MusicHandler(MusicService service) {
        this.service = service;
    }

    Mono<ServerResponse> getAllMusic(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
                .body(BodyInserters
                        .fromObject(service.getAllMusic()));
    }

    Mono<ServerResponse> getArtist(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
                .body(BodyInserters
                        .fromObject(service.getArtist(request.pathVariable("artist"))));
    }

    Mono<ServerResponse> getAlbum(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
                .body(BodyInserters
                        .fromObject(service.getAlbum(request.pathVariable("artist"), request.pathVariable("album"))));
    }

    Mono<ServerResponse> getSong(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
                .body(BodyInserters
                        .fromObject(service.getSong(request.pathVariable("artist"),
                                request.pathVariable("album"),
                                request.pathVariable("song"))));
    }

    Mono<ServerResponse> addArtist(ServerRequest request) {
        Mono<Boolean> status = service.addArtist(request.pathVariable("artist"));
        return ServerResponse.ok().body(status, Boolean.class);
    }
    Mono<ServerResponse> addAlbum(ServerRequest request) {
        Mono<Boolean> status = service.addAlbum(request.pathVariable("artist"),request.pathVariable("album"));
        return ServerResponse.ok().body(status, Boolean.class);
    }

    Mono<ServerResponse> addSong(ServerRequest request) {

        return request.body(BodyExtractors.toMultipartData()).flatMap(p -> {
                p.toSingleValueMap().keySet().stream().forEach(c -> {
                    FilePart fp = (FilePart) p.toSingleValueMap().get(c);
                    service.addSong(request.queryParam("artist").get()
                                   ,request.queryParam("album").get()
                                   ,fp.filename()).subscribe();
                    fp.transferTo(new File(root+"\\"+request.queryParam("artist").get()
                                                        +"\\"+request.queryParam("album").get()
                                                        +"\\"+fp.filename()));
                });

            return ServerResponse.ok()
                    .contentType(APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .body(Mono.just("SUCCESS"), String.class);
        });
    }
}