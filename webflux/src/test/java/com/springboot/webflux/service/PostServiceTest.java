package com.springboot.webflux.service;

import com.springboot.webflux.dto.PostEditRequest;
import com.springboot.webflux.dto.PostRegisterRequest;
import com.springboot.webflux.entity.Member;
import com.springboot.webflux.entity.Post;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;
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
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostPredictionService postPredictionService;

    private Member createMember(){
        return Member.builder()
                .memberId(1L)
                .username("user")
                .build();
    }

    private Post createPost(){
        return Post.builder()
                .postId(1L)
                .memberId(1L)
                .contents("contents")
                .wroteAt(LocalDateTime.now())
                .build();
    }

    private Post createPost(Long postId){
        return Post.builder()
                .postId(postId)
                .memberId(1L)
                .contents("contents")
                .wroteAt(LocalDateTime.now())
                .build();
    }

    private Post createEditedPost(){
        return Post.builder()
                .postId(1L)
                .memberId(1L)
                .contents("edited contents")
                .wroteAt(LocalDateTime.now())
                .editedAt(LocalDateTime.now())
                .build();
    }

    private PostRegisterRequest createRegisterRequest(){
        return PostRegisterRequest.builder()
                .memberId(1L)
                .contents("contents")
                .build();
    }

    private PostEditRequest postEditRequest(){
        return PostEditRequest.builder()
                .postId(1L)
                .contents("edited contents")
                .build();
    }

    @Nested
    @WithMockUser
    @DisplayName("post 등록")
    class WritePost{

        @Test
        @DisplayName("post 등록 성공")
        void successWrite(){
            //given
            Member member = createMember();
            Post post = createPost();
            PostRegisterRequest postRegisterRequest = createRegisterRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.save(any(Post.class)))
                    .willReturn(Mono.just(post));
            given(postPredictionService.callSentimentAnalysisApiAndSaveResultForRegister(any(Post.class)))
                    .willReturn(Mono.just(post));

            //when
            Mono<Post> fetchedPost = postService.write(postRegisterRequest, "user");

            //then
            StepVerifier.create(fetchedPost)
                    .assertNext(postResult -> {
                        assertThat(postResult.getPostId(), equalTo(1L));
                        assertThat(postResult.getMemberId(), equalTo(1L));
                        assertThat(postResult.getContents(), equalTo("contents"));
                        assertThat(postResult.getWroteAt(), is(notNullValue()));
                        assertThat(postResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("post 등록 실패 - 유효하지 않은 요청")
        void failWrite_InvalidRequest(){
            //given
            Member member = createMember();
            PostRegisterRequest postRegisterRequest = createRegisterRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));

            //when
            Mono<Post> fetchedPost = postService.write(postRegisterRequest, "other user");

            //then
            StepVerifier.create(fetchedPost)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()))
                    .log()
                    .verify();
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("post 수정")
    class EditPost{

        @Test
        @DisplayName("post 수정 성공")
        void successEdit(){
            //given
            Member member = createMember();
            Post post = createPost();
            Post editedPost = createEditedPost();
            PostEditRequest postRegisterRequest = postEditRequest();

            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.save(any(Post.class)))
                    .willReturn(Mono.just(editedPost));
            given(postPredictionService.callSentimentAnalysisApiAndSaveResultForEdit(any(Post.class)))
                    .willReturn(Mono.just(editedPost));

            //when
            Mono<Post> fetchedPost = postService.edit(postRegisterRequest, "user");

            //then
            StepVerifier.create(fetchedPost)
                    .assertNext(postResult -> {
                        assertThat(postResult.getPostId(), equalTo(1L));
                        assertThat(postResult.getMemberId(), equalTo(1L));
                        assertThat(postResult.getContents(), equalTo("edited contents"));
                        assertThat(postResult.getWroteAt(), is(notNullValue()));
                        assertThat(postResult.getEditedAt(), is(notNullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("post 수정 실패 - 유효하지 않은 요청")
        void failEdit_InvalidRequest(){
            //given
            Member member = createMember();
            Post post = createPost();
            PostEditRequest postRegisterRequest = postEditRequest();

            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));

            //when
            Mono<Post> fetchedPost = postService.edit(postRegisterRequest, "other user");

            //then
            StepVerifier.create(fetchedPost)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()))
                    .verify();
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("post 삭제")
    class DeletePost{

        @Test
        @DisplayName("post 삭제 성공")
        void successDelete(){
            //given
            Post post = createPost();
            Member member = createMember();

            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.delete(any(Post.class)))
                    .willReturn(Mono.empty());

            //when
            Mono<Void> fetchedPost = postService.delete(1L, "user");

            //then
            StepVerifier.create(fetchedPost)
                    .verifyComplete();
        }

        @Test
        @DisplayName("post 삭제 실패 - 유효하지 않은 요청")
        void failDelete_InvalidRequest(){
            //given
            Post post = createPost();
            Member member = createMember();

            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.delete(any(Post.class)))
                    .willReturn(Mono.empty());

            //when
            Mono<Void> fetchedPost = postService.delete(1L, "other user");

            //then
            StepVerifier.create(fetchedPost)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()))
                    .verify();
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("postId로 post 조회")
    class FindByIdPost{

        @Test
        @DisplayName("postId로 post 조회 성공")
        void successFindById(){
            //given
            Post post = createPost();

            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.just(post));

            //when
            Mono<Post> fetchedPost = postService.findById(1L);

            //then
            StepVerifier.create(fetchedPost)
                    .assertNext(postResult -> {
                        assertThat(postResult.getPostId(), equalTo(1L));
                        assertThat(postResult.getMemberId(), equalTo(1L));
                        assertThat(postResult.getContents(), equalTo("contents"));
                        assertThat(postResult.getWroteAt(), is(notNullValue()));
                        assertThat(postResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("postId로 post 조회 실패 - 게시글을 찾지 못함")
        void failFindById_PostNotFound(){
            //given
            given(postRepository.findById(anyLong()))
                    .willReturn(Mono.empty());

            //when
            Mono<Post> fetchedPost = postService.findById(1L);

            //then
            StepVerifier.create(fetchedPost)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(POST_NOT_FOUND.getMessage()))
                    .verify();
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("memberId로 post 조회")
    class FindByMemberIdPost{

        @Test
        @DisplayName("memberId로 post 조회 성공")
        void successFindByMemberId(){
            //given
            Member member = createMember();
            Post post1 = createPost(1L);
            Post post2 = createPost(2L);
            List<Post> posts = List.of(post1, post2);

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(postRepository.findByMemberId(anyLong()))
                    .willReturn(Flux.fromIterable(posts));

            //when
            Flux<Post> fetchedPosts = postService.findByMemberId(1L);

            //then
            StepVerifier.create(fetchedPosts)
                    .assertNext(postResult -> {
                        assertThat(postResult.getPostId(), equalTo(1L));
                        assertThat(postResult.getMemberId(), equalTo(1L));
                        assertThat(postResult.getContents(), equalTo("contents"));
                        assertThat(postResult.getWroteAt(), is(notNullValue()));
                        assertThat(postResult.getEditedAt(), is(nullValue()));
                    })
                    .assertNext(postResult -> {
                        assertThat(postResult.getPostId(), equalTo(2L));
                        assertThat(postResult.getMemberId(), equalTo(1L));
                        assertThat(postResult.getContents(), equalTo("contents"));
                        assertThat(postResult.getWroteAt(), is(notNullValue()));
                        assertThat(postResult.getEditedAt(), is(nullValue()));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("memberId로 post 조회 실패 - 멤버를 찾지 못함")
        void failFindByMemberId_MemberNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.empty());

            //when
            Flux<Post> fetchedPost = postService.findByMemberId(1L);

            //then
            StepVerifier.create(fetchedPost)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(MEMBER_NOT_FOUND.getMessage()))
                    .verify();
        }
    }

}
