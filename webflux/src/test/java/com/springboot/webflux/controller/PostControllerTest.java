package com.springboot.webflux.controller;

import com.springboot.webflux.dto.PostEditRequest;
import com.springboot.webflux.dto.PostRegisterRequest;
import com.springboot.webflux.dto.PostResponse;
import com.springboot.webflux.entity.Post;
import com.springboot.webflux.service.PostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static com.springboot.webflux.constants.ExceptionStatus.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PostService postService;

    private Post createPost(){
        return Post.builder()
                .postId(1L)
                .memberId(1L)
                .contents("contents")
                .predictResult("positive")
                .predictPercent(0.8f)
                .wroteAt(LocalDateTime.now())
                .build();
    }

    private Post createPost(String contents){
        return Post.builder()
                .postId(1L)
                .memberId(1L)
                .contents(contents)
                .predictResult("positive")
                .predictPercent(0.8f)
                .wroteAt(LocalDateTime.now())
                .build();
    }

    private PostRegisterRequest createRegisterRequest(){
        return PostRegisterRequest.builder()
                .memberId(1L)
                .contents("contents")
                .build();
    }

    private PostEditRequest createEditRequest(){
        return PostEditRequest.builder()
                .postId(1L)
                .contents("editedContents")
                .build();
    }

    @Nested
    @WithMockUser
    @DisplayName("post 등록")
    class WritePost{

        @Test
        @DisplayName("post 등록 성공")
        void successWrite(){
            Post post = createPost();
            PostRegisterRequest postRegisterRequest = createRegisterRequest();

            when(postService.write(any(PostRegisterRequest.class), anyString()))
                    .thenReturn(Mono.just(post));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/posts")
                    .bodyValue(postRegisterRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PostResponse.class)
                    .consumeWith(result -> {
                        PostResponse postResult = result.getResponseBody();

                        assert postResult != null;
                        Assertions.assertEquals(post.getPostId(), postResult.getPostId());
                        Assertions.assertEquals(post.getMemberId(), postResult.getMemberId());
                        Assertions.assertEquals(post.getPredictResult(), postResult.getPredictResult());
                        Assertions.assertEquals(post.getPredictPercent(), postResult.getPredictPercent());
                        Assertions.assertEquals(post.getWroteAt(), postResult.getWroteAt());
                    });

            verify(postService, times(1)).write(any(PostRegisterRequest.class), anyString());
        }

        @Test
        @DisplayName("post 등록 실패 - 유효하지 않은 요청")
        void failWrite_InvalidRequest(){
            PostRegisterRequest postRegisterRequest = createRegisterRequest();

            when(postService.write(any(PostRegisterRequest.class), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/posts")
                    .bodyValue(postRegisterRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(postService, times(1)).write(any(PostRegisterRequest.class), anyString());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("post 수정")
    class EditPost{

        @Test
        @DisplayName("post 수정 성공")
        void successEdit(){
            Post post = createPost("editedPost");
            PostEditRequest postEditRequest = createEditRequest();

            when(postService.edit(any(PostEditRequest.class), anyString()))
                    .thenReturn(Mono.just(post));

            webTestClient.mutateWith(csrf()).patch()
                    .uri("/posts")
                    .bodyValue(postEditRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PostResponse.class)
                    .consumeWith(result -> {
                        PostResponse postResult = result.getResponseBody();

                        assert postResult != null;
                        Assertions.assertEquals(post.getPostId(), postResult.getPostId());
                        Assertions.assertEquals(post.getMemberId(), postResult.getMemberId());
                        Assertions.assertEquals(post.getPredictResult(), postResult.getPredictResult());
                        Assertions.assertEquals(post.getPredictPercent(), postResult.getPredictPercent());
                        Assertions.assertEquals(post.getWroteAt(), postResult.getWroteAt());
                    });

            verify(postService, times(1)).edit(any(PostEditRequest.class), anyString());
        }

        @Test
        @DisplayName("post 수정 실패 - 유효하지 않은 요청")
        void failEdit_InvalidRequest(){
            PostEditRequest postEditRequest = createEditRequest();

            when(postService.edit(any(PostEditRequest.class), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).patch()
                    .uri("/posts")
                    .bodyValue(postEditRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(postService, times(1)).edit(any(PostEditRequest.class), anyString());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("post 삭제")
    class DeletePost{

        @Test
        @DisplayName("post 삭제 성공")
        void successDelete(){
            when(postService.delete(anyLong(), anyString()))
                    .thenReturn(Mono.empty());

            webTestClient.mutateWith(csrf()).delete()
                    .uri("/posts/1")
                    .exchange()
                    .expectStatus().isOk();

            verify(postService, times(1)).delete(anyLong(), anyString());
        }

        @Test
        @DisplayName("post 삭제 실패 - 유효하지 않은 요청")
        void failDelete_InvalidRequest(){
            when(postService.delete(anyLong(), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).delete()
                    .uri("/posts/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(postService, times(1)).delete(anyLong(), anyString());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("post 조회")
    class ViewPost{

        @Test
        @DisplayName("post 조회 성공")
        void successView(){
            Post post = createPost();

            when(postService.findById(anyLong()))
                    .thenReturn(Mono.just(post));

            webTestClient.get()
                    .uri("/posts/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(PostResponse.class)
                    .consumeWith(result -> {
                        PostResponse postResult = result.getResponseBody();

                        assert postResult != null;
                        Assertions.assertEquals(post.getPostId(), postResult.getPostId());
                        Assertions.assertEquals(post.getMemberId(), postResult.getMemberId());
                        Assertions.assertEquals(post.getPredictResult(), postResult.getPredictResult());
                        Assertions.assertEquals(post.getPredictPercent(), postResult.getPredictPercent());
                        Assertions.assertEquals(post.getWroteAt(), postResult.getWroteAt());
                    });

            verify(postService, times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("post 조회 실패 - 게시글을 찾지 못함")
        void failView_PostNotFound(){
            when(postService.findById(anyLong()))
                    .thenReturn(Mono.error(new RuntimeException(POST_NOT_FOUND.getMessage())));

            webTestClient.get()
                    .uri("/posts/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(postService, times(1)).findById(anyLong());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("memberId로 post 조회")
    class FindByMemberIdPost{

        @Test
        @DisplayName("memberId로 post 조회 성공")
        void successFindByMemberId(){
            Post post1 = createPost("contents1");
            Post post2 = createPost("contents2");

            List<Post> posts = List.of(post1, post2);

            when(postService.findByMemberId(anyLong()))
                    .thenReturn(Flux.fromIterable(posts));

            webTestClient.get()
                    .uri("/posts/search/member/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(PostResponse.class)
                    .hasSize(2)
                    .consumeWith(result -> {
                        List<PostResponse> postResult = result.getResponseBody();

                        assert postResult != null;
                        Assertions.assertEquals(post1.getPostId(), postResult.get(0).getPostId());
                        Assertions.assertEquals(post1.getMemberId(), postResult.get(0).getMemberId());
                        Assertions.assertEquals(post1.getPredictResult(), postResult.get(0).getPredictResult());
                        Assertions.assertEquals(post1.getPredictPercent(), postResult.get(0).getPredictPercent());
                        Assertions.assertEquals(post1.getWroteAt(), postResult.get(0).getWroteAt());
                        Assertions.assertEquals(post2.getPostId(), postResult.get(1).getPostId());
                        Assertions.assertEquals(post2.getMemberId(), postResult.get(1).getMemberId());
                        Assertions.assertEquals(post2.getPredictResult(), postResult.get(1).getPredictResult());
                        Assertions.assertEquals(post2.getPredictPercent(), postResult.get(1).getPredictPercent());
                        Assertions.assertEquals(post2.getWroteAt(), postResult.get(1).getWroteAt());
                    });

            verify(postService, times(1)).findByMemberId(anyLong());
        }

        @Test
        @DisplayName("memberId로 post 조회 실패 - 멤버를 찾지 못함")
        void failFindByMemberId_MemberNotFound(){
            when(postService.findByMemberId(anyLong()))
                    .thenReturn(Flux.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())));

            webTestClient.get()
                    .uri("/posts/search/member/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(postService, times(1)).findByMemberId(anyLong());
        }
    }
}
