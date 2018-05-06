package ru.axel.yamobdev2018;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.axel.yamobdev2018.imgurmodel.Gallery;

public interface ImgurApi {

    // https://api.imgur.com/3/gallery/search/{{sort}}/{{window}}/{{page}}?q=cats

    /**
     * Search the gallery with a given query string.
     * @param sort Sort by: time | viral | top - defaults to time
     * @param window Range of day if sort is top: day | week | month | year | all, defaults to all.
     * @param page the data paging number
     * @param query search query
     * @return
     */
    @GET("gallery/search/{sort}/{window}/{page}")
    Call<Gallery> searchGallery(@Path("sort") String sort, @Path("window") String window, @Path("page") Integer page, @Query("q") String query);

    @GET("gallery/{{section}}/{{sort}}/{{window}}/{{page}}")
    Call<Gallery> getGallery(@Path("section") String section, @Path("sort") String sort, @Path("page") Integer page);
}
