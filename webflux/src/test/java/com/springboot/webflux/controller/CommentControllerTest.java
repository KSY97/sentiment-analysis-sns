package com.springboot.webflux.controller;

import com.springboot.webflux.dto.CommentEditRequest;
import com.springboot.webflux.dto.CommentRegisterRequest;
import com.springboot.webflux.dto.CommentResponse;
import com.springboot.webflux.entity.Comment;
import com.springboot.webflux.service.CommentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CommentController.class)
public class CommentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CommentService commentService;

    private CommentRegisterRequest createRegisterRequest(){
        return CommentRegisterRequest.builder()
                .memberId(1L)
                .postId(1L)
                .contents("contents")
                .build();
    }

    private CommentEditRequest commentEditRequest(){
        return CommentEditRequest.builder()
                .commentId(1L)
                .contents("edited contents")
                .build();
    }

    private Comment createComment(String contents){
        return Comment.builder()
                .commentId(1L)
                .memberId(1L)
                .postId(1L)
                .contents(contents)
                .predictResult("positive")
                .predictPercent(0.7f)
                .wroteAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @WithMockUser
    @DisplayName("comment 등록")
    class WriteComment{

        @Test
        @DisplayName("comment 등록 성공")
        void successWrite(){
            Comment comment = createComment("contents");
            CommentRegisterRequest commentRegisterRequest = createRegisterRequest();

            when(commentService.write(any(CommentRegisterRequest.class), anyString()))
                    .thenReturn(Mono.just(comment));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/comment")
                    .bodyValue(commentRegisterRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CommentResponse.class)
                    .consumeWith(result -> {
                        CommentResponse commentResult = result.getResponseBody();

                        assert commentResult != null;
                        Assertions.assertEquals(comment.getCommentId(), commentResult.getCommentId());
                        Assertions.assertEquals(comment.getPostId(), commentResult.getPostId());
                        Assertions.assertEquals(comment.getMemberId(), commentResult.getMemberId());
                        Assertions.assertEquals(comment.getContents(), commentResult.getContents());
                        Assertions.assertEquals(comment.getPredictResult(), commentResult.getPredictResult());
                        Assertions.assertEquals(comment.getPredictPercent(), commentResult.getPredictPercent());
                        Assertions.assertEquals(comment.getWroteAt(), commentResult.getWroteAt());
                        Assertions.assertEquals(comment.getEditedAt(), commentResult.getEditedAt());
                    });

            verify(commentService, times(1)).write(any(CommentRegisterRequest.class), anyString());

        }

        @Test
        @DisplayName("comment 등록 실패 - 유효하지 않은 요청")
        void failWrite_InvalidRequest(){
            CommentRegisterRequest commentRegisterRequest = createRegisterRequest();

            when(commentService.write(any(CommentRegisterRequest.class), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/comment")
                    .bodyValue(commentRegisterRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).write(any(CommentRegisterRequest.class), anyString());

        }

        @Test
        @DisplayName("comment 등록 실패 - 게시글을 찾지 못함")
        void failWrite_PostNotFound(){
            CommentRegisterRequest commentRegisterRequest = createRegisterRequest();

            when(commentService.write(any(CommentRegisterRequest.class), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(POST_NOT_FOUND.getMessage())));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/comment")
                    .bodyValue(commentRegisterRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).write(any(CommentRegisterRequest.class), anyString());

        }
    }

    @Nested
    @WithMockUser
    @DisplayName("comment 수정")
    class EditComment{

        @Test
        @DisplayName("comment 수정 성공")
        void successEdit(){
            Comment comment = createComment("edited contents");
            CommentEditRequest commentEditRequest = commentEditRequest();

            when(commentService.edit(any(CommentEditRequest.class), anyString()))
                    .thenReturn(Mono.just(comment));

            webTestClient.mutateWith(csrf()).patch()
                    .uri("/comment")
                    .bodyValue(commentEditRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CommentResponse.class)
                    .consumeWith(result -> {
                        CommentResponse commentResult = result.getResponseBody();

                        assert commentResult != null;
                        Assertions.assertEquals(comment.getCommentId(), commentResult.getCommentId());
                        Assertions.assertEquals(comment.getPostId(), commentResult.getPostId());
                        Assertions.assertEquals(comment.getMemberId(), commentResult.getMemberId());
                        Assertions.assertEquals(comment.getContents(), commentResult.getContents());
                        Assertions.assertEquals(comment.getPredictResult(), commentResult.getPredictResult());
                        Assertions.assertEquals(comment.getPredictPercent(), commentResult.getPredictPercent());
                        Assertions.assertEquals(comment.getWroteAt(), commentResult.getWroteAt());
                        Assertions.assertEquals(comment.getEditedAt(), commentResult.getEditedAt());
                    });

            verify(commentService, times(1)).edit(any(CommentEditRequest.class), anyString());



        }

        @Test
        @DisplayName("comment 수정 실패 - 유효하지 않은 요청")
        void failEdit_InvalidRequest(){
            CommentEditRequest commentEditRequest = commentEditRequest();

            when(commentService.edit(any(CommentEditRequest.class), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).patch()
                    .uri("/comment")
                    .bodyValue(commentEditRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).edit(any(CommentEditRequest.class), anyString());


        }
    }

    @Nested
    @WithMockUser
    @DisplayName("comment 삭제")
    class DeleteComment{

        @Test
        @DisplayName("comment 삭제 성공 ")
        void successDelete(){
            when(commentService.delete(anyLong(), anyString()))
                    .thenReturn(Mono.empty());

            webTestClient.mutateWith(csrf()).delete()
                    .uri("/comment/1")
                    .exchange()
                    .expectStatus().isOk();

            verify(commentService, times(1)).delete(anyLong(), anyString());
        }

        @Test
        @DisplayName("comment 삭제 실패 - 유효하지 않은 요청")
        void failDelete_InvalidRequest(){
            when(commentService.delete(anyLong(), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).delete()
                    .uri("/comment/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).delete(anyLong(), anyString());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("comment 조회")
    class ViewComment{

        @Test
        @DisplayName("comment 조회 성공")
        void successView(){
            Comment comment = createComment("contents");

            when(commentService.findById(anyLong()))
                    .thenReturn(Mono.just(comment));

            webTestClient.get()
                    .uri("/comment/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CommentResponse.class)
                    .consumeWith(result -> {
                        CommentResponse commentResult = result.getResponseBody();

                        assert commentResult != null;
                        Assertions.assertEquals(comment.getCommentId(), commentResult.getCommentId());
                        Assertions.assertEquals(comment.getPostId(), commentResult.getPostId());
                        Assertions.assertEquals(comment.getMemberId(), commentResult.getMemberId());
                        Assertions.assertEquals(comment.getContents(), commentResult.getContents());
                        Assertions.assertEquals(comment.getPredictResult(), commentResult.getPredictResult());
                        Assertions.assertEquals(comment.getPredictPercent(), commentResult.getPredictPercent());
                        Assertions.assertEquals(comment.getWroteAt(), commentResult.getWroteAt());
                        Assertions.assertEquals(comment.getEditedAt(), commentResult.getEditedAt());
                    });

            verify(commentService, times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("comment 조회 실패 - 댓글을 찾지 못함")
        void failView_CommentNotFound(){
            when(commentService.findById(1L))
                    .thenReturn(Mono.error(new RuntimeException(COMMENT_NOT_FOUND.getMessage())));

            webTestClient.get()
                    .uri("/comment/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).findById(anyLong());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("memberId로 comment 조회")
    class FindByMemberIdComment{

        @Test
        @DisplayName("memberId로 comment 조회 성공")
        void successFind(){
            Comment comment1 = createComment("contents1");
            Comment comment2 = createComment("contents2");

            List<Comment> comments = List.of(comment1, comment2);

            when(commentService.findByMemberId(anyLong()))
                    .thenReturn(Flux.fromIterable(comments));

            webTestClient.get()
                    .uri("/comment/search/member/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(CommentResponse.class)
                    .hasSize(2)
                    .consumeWith(result -> {
                        List<CommentResponse> commentResult = result.getResponseBody();

                        assert commentResult.get(0) != null;
                        assert commentResult.get(1) != null;
                        Assertions.assertEquals(comment1.getCommentId(), commentResult.get(0).getCommentId());
                        Assertions.assertEquals(comment1.getPostId(), commentResult.get(0).getPostId());
                        Assertions.assertEquals(comment1.getMemberId(), commentResult.get(0).getMemberId());
                        Assertions.assertEquals(comment1.getContents(), commentResult.get(0).getContents());
                        Assertions.assertEquals(comment1.getPredictResult(), commentResult.get(0).getPredictResult());
                        Assertions.assertEquals(comment1.getPredictPercent(), commentResult.get(0).getPredictPercent());
                        Assertions.assertEquals(comment1.getWroteAt(), commentResult.get(0).getWroteAt());
                        Assertions.assertEquals(comment1.getEditedAt(), commentResult.get(0).getEditedAt());
                        Assertions.assertEquals(comment2.getCommentId(), commentResult.get(1).getCommentId());
                        Assertions.assertEquals(comment2.getPostId(), commentResult.get(1).getPostId());
                        Assertions.assertEquals(comment2.getMemberId(), commentResult.get(1).getMemberId());
                        Assertions.assertEquals(comment2.getContents(), commentResult.get(1).getContents());
                        Assertions.assertEquals(comment2.getPredictResult(), commentResult.get(1).getPredictResult());
                        Assertions.assertEquals(comment2.getPredictPercent(), commentResult.get(1).getPredictPercent());
                        Assertions.assertEquals(comment2.getWroteAt(), commentResult.get(1).getWroteAt());
                        Assertions.assertEquals(comment2.getEditedAt(), commentResult.get(1).getEditedAt());
                    });

            verify(commentService, times(1)).findByMemberId(anyLong());
        }

        @Test
        @DisplayName("memberId로 comment 조회 실패 - 멤버를 찾지 못함")
        void failFind_MemberNotFound(){

            when(commentService.findByMemberId(anyLong()))
                    .thenReturn(Flux.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())));

            webTestClient.get()
                    .uri("/comment/search/member/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).findByMemberId(anyLong());
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("postId로 comment 검색")
    class FindByPostIdComment{

        @Test
        @DisplayName("postId로 comment 검색 성공")
        void successFind(){
            Comment comment1 = createComment("contents1");
            Comment comment2 = createComment("contents2");

            List<Comment> comments = List.of(comment1, comment2);

            when(commentService.findByPostId(anyLong()))
                    .thenReturn(Flux.fromIterable(comments));

            webTestClient.get()
                    .uri("/comment/search/posts/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(CommentResponse.class)
                    .hasSize(2)
                    .consumeWith(result -> {
                        List<CommentResponse> commentResult = result.getResponseBody();

                        assert commentResult.get(0) != null;
                        assert commentResult.get(1) != null;
                        Assertions.assertEquals(comment1.getCommentId(), commentResult.get(0).getCommentId());
                        Assertions.assertEquals(comment1.getPostId(), commentResult.get(0).getPostId());
                        Assertions.assertEquals(comment1.getMemberId(), commentResult.get(0).getMemberId());
                        Assertions.assertEquals(comment1.getContents(), commentResult.get(0).getContents());
                        Assertions.assertEquals(comment1.getPredictResult(), commentResult.get(0).getPredictResult());
                        Assertions.assertEquals(comment1.getPredictPercent(), commentResult.get(0).getPredictPercent());
                        Assertions.assertEquals(comment1.getWroteAt(), commentResult.get(0).getWroteAt());
                        Assertions.assertEquals(comment1.getEditedAt(), commentResult.get(0).getEditedAt());
                        Assertions.assertEquals(comment2.getCommentId(), commentResult.get(1).getCommentId());
                        Assertions.assertEquals(comment2.getPostId(), commentResult.get(1).getPostId());
                        Assertions.assertEquals(comment2.getMemberId(), commentResult.get(1).getMemberId());
                        Assertions.assertEquals(comment2.getContents(), commentResult.get(1).getContents());
                        Assertions.assertEquals(comment2.getPredictResult(), commentResult.get(1).getPredictResult());
                        Assertions.assertEquals(comment2.getPredictPercent(), commentResult.get(1).getPredictPercent());
                        Assertions.assertEquals(comment2.getWroteAt(), commentResult.get(1).getWroteAt());
                        Assertions.assertEquals(comment2.getEditedAt(), commentResult.get(1).getEditedAt());
                    });

            verify(commentService, times(1)).findByPostId(anyLong());
        }

        @Test
        @DisplayName("postId로 comment 검색 실패 - 게시글을 찾지 못함")
        void failFind_MemberNotFound(){

            when(commentService.findByPostId(anyLong()))
                    .thenReturn(Flux.error(new RuntimeException(POST_NOT_FOUND.getMessage())));

            webTestClient.get()
                    .uri("/comment/search/posts/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(commentService, times(1)).findByPostId(anyLong());
        }
    }


}
