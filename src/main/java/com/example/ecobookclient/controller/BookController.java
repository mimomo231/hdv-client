package com.example.ecobookclient.controller;

import com.example.ecobookclient.request.LoginRequest;
import com.example.ecobookclient.response.BookResponse;
import com.example.ecobookclient.response.CategoryResponse;
import com.example.ecobookclient.response.CommentResponse;
import com.example.ecobookclient.response.UserResponse;
import com.example.ecobookclient.util.GeneralPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping(path = "/shop")
public class BookController {
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/{cate-id}")
    public String getAllBook(Model model,
                             @PathVariable(name = "cate-id") Integer cateId){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:8082/api/ebook/all-book";
        String categoryUrl = "http://localhost:8082/api/category/";
        try {

            if(cateId == 0){
                // get all
                ResponseEntity<GeneralPageResponse<BookResponse>> response = restTemplate.exchange(url,HttpMethod.GET,null,
                        new ParameterizedTypeReference<GeneralPageResponse<BookResponse>>() {});
                GeneralPageResponse<BookResponse> list = response.getBody();
                model.addAttribute("book", Objects.requireNonNull(response.getBody()).getContent());
                model.addAttribute("count", (long) Objects.requireNonNull(list).getContent().size());
            }else{
                // get book by category id
                String urlBookByCate = "http://localhost:8082/api/ebook/book-cate?cate-id="+cateId;
                ResponseEntity<GeneralPageResponse<BookResponse>> response = restTemplate.exchange(urlBookByCate,HttpMethod.GET,null,
                        new ParameterizedTypeReference<GeneralPageResponse<BookResponse>>() {});
                GeneralPageResponse<BookResponse> list = response.getBody();
                if(list == null){
                    model.addAttribute("count",0);
                }
                else{
                    model.addAttribute("book", list.getContent());
                    model.addAttribute("count", (long) list.getContent().size());
                }

            }

            ResponseEntity<List<CategoryResponse>> responseCate = restTemplate.exchange(categoryUrl,HttpMethod.GET,null,
                    new ParameterizedTypeReference<List<CategoryResponse>>() {});
//            log.info("log " + response.getBody());
            List<CategoryResponse> listCategory = responseCate.getBody();
            model.addAttribute("category",listCategory);
            model.addAttribute("cate",listCategory.get(cateId-1));
            return "shop";
        } catch (Exception ex) {
//            log.info(ex.toString());
        }
        return "shop";
    }
    @GetMapping("/")
    public String searchBook(@RequestParam(name = "search") String key,
                             @RequestParam(name = "from") @DefaultValue(value = "0") Integer from,
                             @RequestParam(name = "to") @DefaultValue(value = "100") Integer to,
                             Model model){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!key.isEmpty()) {
            String url = "http://localhost:8082/api/ebook/search?key="+key+"&from="+from+"&to="+to;
            ResponseEntity<List<BookResponse>> response = restTemplate.exchange(url,HttpMethod.GET,null,
                    new ParameterizedTypeReference<List<BookResponse>>() {});
            List<BookResponse> list = response.getBody();
            model.addAttribute("book", response.getBody());
            model.addAttribute("count", (long) Objects.requireNonNull(list).size());
        }
        else {
            String url1 = "http://localhost:8082/api/ebook/all-book";
            ResponseEntity<GeneralPageResponse<BookResponse>> response = restTemplate.exchange(url1,HttpMethod.GET,null,
                    new ParameterizedTypeReference<GeneralPageResponse<BookResponse>>() {});
            GeneralPageResponse<BookResponse> list = response.getBody();
            model.addAttribute("book", Objects.requireNonNull(response.getBody()).getContent());
            model.addAttribute("count", (long) Objects.requireNonNull(list).getContent().size());
        }
        String categoryUrl = "http://localhost:8082/api/category/";
        ResponseEntity<List<CategoryResponse>> responseCate = restTemplate.exchange(categoryUrl,HttpMethod.GET,null,
                new ParameterizedTypeReference<List<CategoryResponse>>() {});
//            log.info("log " + response.getBody());
        List<CategoryResponse> listCategory = responseCate.getBody();
        model.addAttribute("category",listCategory);
        return "shop";
    }
    @GetMapping("/detail/{bookId}")
    public String getDetailBook(@PathVariable(name = "bookId") Integer bookId,
                                Model model){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:8082/api/ebook/"+bookId;
        ResponseEntity<BookResponse> response = restTemplate.exchange(url,HttpMethod.GET,null,
                BookResponse.class);
        model.addAttribute("book", response.getBody());
        String urlCom = "http://localhost:8083/api/book-review/"+bookId;
        ResponseEntity<List<CommentResponse>> response1 = restTemplate.exchange(urlCom,HttpMethod.GET,null,
                new ParameterizedTypeReference<List<CommentResponse>>() {});

        model.addAttribute("comments",response1.getBody());
        model.addAttribute("cn",response1.getBody().stream().count());

        return "single-product-details";
    }

}
