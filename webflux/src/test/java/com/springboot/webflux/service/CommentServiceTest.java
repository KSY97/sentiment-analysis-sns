package com.springboot.webflux.service;

import com.springboot.webflux.dto.CommentEditRequest;
import com.springboot.webflux.dto.CommentRegisterRequest;
import com.springboot.webflux.entity.Comment;
import com.springboot.webflux.entity.Member;
import com.springboot.webflux.entity.Post;
import com.springboot.webflux.repository.CommentRepository;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static com.springboot.webflux.constants.ExceptionStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentPredictionService commentPredictionService;

    private Member createMember(){
        return Member.builder()
                .memberId(11L)
                .username("user")
                .build();
    }

    private Post createPost(){
        return Post.builder()
                .postId(10L)
                .contents("post contents")
                .build();
    }

    private Comment createComment(){
        return Comment.builder()
                .commentId(1L)
                .memberId(11L)
                .postId(10L)
                .contents("comment contents")
                .wroteAt(LocalDateTime.now())
                .build();
    }

    private Comment createComment(Long commentId){
        return Comment.builder()
                .commentId(commentId)
                .memberId(11L)
                .postId(10L)
                .contents("comment contents")
                .wroteAt(LocalDateTime.now())
                .build();
    }

    private Comment createEditedComment(){
        return Comment.builder()
                .commentId(1L)
                .memberId(11L)
                .postId(10L)
                .contents("edited comment contents")
                .wroteAt(LocalDateTime.now())
                .editedAt(LocalDateTime.now())
                .build();
    }

    private CommentRegisterRequest createRegisterRequest(){
        return CommentRegisterRequest.builder()
                .memberId(11L)
                .postId(10L)
                .contents("comment contents")
                .build();
    }

    private CommentEditRequest createEditRequest(){
        return CommentEditRequest.builder()
                .commentId(1L)
                .contents("edited comment contents")
                .build();
    }

    @Nested
    @DisplayName("comment 등록")
    class WriteComment{

        @Test
        @DisplayName("comment 등록 성공")
        void successWrite(){
            //given
            Member member = createMember();
            Post post = createPost();
            Comment comment = createComment();
            CommentRegisterRequest commentRegisterRequest = createRegisterRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(commentRepository.save(any(Comment.class)))
                    .willReturn(Mono.just(comment));
            given(commentPredictionService.callSentimentAnalysisApiAndSaveResultForRegister(any(Comment.class)))
                    .willReturn(Mono.just(comment));

            //when
            Mono<Comment> fetchedComment = commentService.write(commentRegisterRequest, "user");

            //then
            StepVerifier.create(fetchedComment)
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(1L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("comment 등록 실패 - 유효하지 않은 요청")
        void failWrite_InvalidRequest(){
            //given
            Member member = createMember();
            Post post = createPost();
            CommentRegisterRequest commentRegisterRequest = createRegisterRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));

            //when
            Mono<Comment> fetchedComment = commentService.write(commentRegisterRequest, "other user");

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()));
        }

        @Test
        @DisplayName("comment 등록 실패 - 게시글을 찾을수 없음")
        void failWrite_PostNotFound(){
            //given
            Member member = createMember();
            Post post = createPost();
            CommentRegisterRequest commentRegisterRequest = createRegisterRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(commentRepository.save(any(Comment.class)))
                    .willReturn(Mono.empty());
            //when
            Mono<Comment> fetchedComment = commentService.write(commentRegisterRequest, "user");

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(POST_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("comment 수정")
    class EditComment{

        @Test
        @DisplayName("comment 수정 성공")
        void successEdit(){
            //given
            Comment comment = createComment();
            Comment editedComment = createEditedComment();
            Member member = createMember();
            CommentEditRequest commentEditRequest = createEditRequest();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Mono.just(comment));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(commentRepository.save(any(Comment.class)))
                    .willReturn(Mono.just(editedComment));
            given(commentPredictionService.callSentimentAnalysisApiAndSaveResultForEdit(any(Comment.class)))
                    .willReturn(Mono.just(editedComment));

            //when
            Mono<Comment> fetchedComment = commentService.edit(commentEditRequest, "user");

            //then
            StepVerifier.create(fetchedComment)
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(1L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("edited comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(notNullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("comment 수정 실패 - 유효하지 않은 요청")
        void failEdit_InvalidRequest(){
            //given
            Comment comment = createComment();
            Member member = createMember();
            CommentEditRequest commentEditRequest = createEditRequest();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Mono.just(comment));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));

            //when
            Mono<Comment> fetchedComment = commentService.edit(commentEditRequest, "other user");

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()));
        }
    }

    @Nested
    @DisplayName("comment 삭제")
    class DeleteComment{

        @Test
        @DisplayName("comment 삭제 성공")
        void successDelete(){
            //given
            Comment comment = createComment();
            Member member = createMember();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Mono.just(comment));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(commentRepository.delete(any(Comment.class)))
                    .willReturn(Mono.empty());

            //when
            Mono<Void> fetchedComment = commentService.delete(1L, "user");

            //then
            StepVerifier.create(fetchedComment)
                    .verifyComplete();
        }

        @Test
        @DisplayName("comment 삭제 실패 - 유효하지 않은 요청")
        void failDelete_InvalidRequest(){
            //given
            Comment comment = createComment();
            Member member = createMember();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Mono.just(comment));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));

            //when
            Mono<Void> fetchedComment = commentService.delete(1L, "other user");

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()));
        }
    }

    @Nested
    @DisplayName("commentId로 comment 조회")
    class FindByIdComment{

        @Test
        @DisplayName("commentId로 comment 조회 성공")
        void successFindById(){
            //given
            Comment comment = createComment();

            given(commentRepository.findById(anyLong()))
                    .willReturn(Mono.just(comment));

            //when
            Mono<Comment> fetchedComment = commentService.findById(1L);

            //then
            StepVerifier.create(fetchedComment)
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(1L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("commentId로 comment 조회 실패 - 댓글을 찾을수 없음")
        void failFindById_CommentNotFound(){
            //given
            given(commentRepository.findById(anyLong()))
                    .willReturn(Mono.empty());

            //when
            Mono<Comment> fetchedComment = commentService.findById(1L);

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(COMMENT_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("memberId로 comment 조회")
    class FindByMemberIdComment{

        @Test
        @DisplayName("memberId로 comment 조회 성공")
        void successFindByMemberId(){
            //given
            Member member = createMember();
            Comment comment1 = createComment(1L);
            Comment comment2 = createComment(2L);
            List<Comment> comments = List.of(comment1, comment2);

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(commentRepository.findByMemberId(anyLong()))
                    .willReturn(Flux.fromIterable(comments));

            //when
            Flux<Comment> fetchedComment = commentService.findByMemberId(11L);

            //then
            StepVerifier.create(fetchedComment)
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(1L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(nullValue()));
                    })
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(2L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("memberId로 comment 조회 실패 - 멤버를 찾을수 없음")
        void failFindByMemberId_MemberNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.empty());

            //when
            Flux<Comment> fetchedComment = commentService.findByMemberId(11L);

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(MEMBER_NOT_FOUND.getMessage()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("postId로 comment 조회")
    class FindByPostIdComment{

        @Test
        @DisplayName("postId로 comment 조회 성공")
        void successFindByPostId(){
            //given
            Post post = createPost();
            Comment comment1 = createComment(1L);
            Comment comment2 = createComment(2L);
            List<Comment> comments = List.of(comment1, comment2);

            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(commentRepository.findByPostId(anyLong()))
                    .willReturn(Flux.fromIterable(comments));

            //when
            Flux<Comment> fetchedComment = commentService.findByPostId(10L);

            //then
            StepVerifier.create(fetchedComment)
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(1L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(nullValue()));
                    })
                    .assertNext(commentResult -> {
                        assertThat(commentResult.getCommentId(), equalTo(2L));
                        assertThat(commentResult.getMemberId(), equalTo(11L));
                        assertThat(commentResult.getPostId(), equalTo(10L));
                        assertThat(commentResult.getContents(), equalTo("comment contents"));
                        assertThat(commentResult.getWroteAt(), is(notNullValue()));
                        assertThat(commentResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("postId로 comment 조회 실패 - 게시글을 찾을수 없음")
        void failFindByPostId_PostNotFound(){
            //given
            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.empty());

            //when
            Flux<Comment> fetchedComment = commentService.findByPostId(10L);

            //then
            StepVerifier.create(fetchedComment)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(POST_NOT_FOUND.getMessage()))
                    .verify();
        }
    }
}
