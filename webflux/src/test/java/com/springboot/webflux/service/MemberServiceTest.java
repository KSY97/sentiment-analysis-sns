package com.springboot.webflux.service;

import com.springboot.webflux.dto.MemberEditRequest;
import com.springboot.webflux.dto.MemberSignInRequest;
import com.springboot.webflux.dto.MemberSignUpRequest;
import com.springboot.webflux.entity.Member;
import com.springboot.webflux.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.springboot.webflux.constants.ExceptionStatus.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtService jwtService;

    private Member createMember(){
        return Member.builder()
                .memberId(1L)
                .username("user")
                .password("password")
                .build();
    }

    private Member createEditedMember(){
        return Member.builder()
                .memberId(1L)
                .username("edited user")
                .password("password")
                .build();
    }

    private MemberSignUpRequest createSignUpRequest(){
        return MemberSignUpRequest.builder()
                .username("user")
                .password("password")
                .build();
    }

    private MemberSignInRequest createSignInRequest(){
        return MemberSignInRequest.builder()
                .username("user")
                .password("password")
                .build();
    }

    private MemberEditRequest createEditRequest(){
        return MemberEditRequest.builder()
                .memberId(1L)
                .username("edited user")
                .password("password")
                .build();
    }

    @Nested
    @DisplayName("memberId로 member 조회")
    class FindByIdMember{

        @Test
        @DisplayName("memberId로 member 조회 성공")
        void successFindById(){
            //given
            Member member = createMember();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));

            //when
            Mono<Member> fetchedMember = memberService.findById(1L);

            //then
            StepVerifier.create(fetchedMember)
                    .assertNext(memberResult -> {
                        assertThat(memberResult.getMemberId(), equalTo(1L));
                        assertThat(memberResult.getUsername(), equalTo("user"));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("memberId로 member 조회 실패 - 멤버를 찾지 못함")
        void failFindById_MemberNotFound(){
            //given
            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.empty());

            //when
            Mono<Member> fetchedMember = memberService.findById(1L);

            //then
            StepVerifier.create(fetchedMember)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(MEMBER_NOT_FOUND.getMessage()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("member 회원가입")
    class SignUpMember{

        @Test
        @DisplayName("member 회원가입 성공")
        void successSignUp(){
            //given
            Member member = createMember();
            MemberSignUpRequest memberSignUpRequest = createSignUpRequest();

            given(memberRepository.existsByUsername(anyString()))
                    .willReturn(Mono.just(false));
            given(memberRepository.save(any(Member.class)))
                    .willReturn(Mono.just(member));

            //when
            Mono<Member> fetchedMember = memberService.signUp(memberSignUpRequest);

            //then
            StepVerifier.create(fetchedMember)
                    .assertNext(memberResult -> {
                        assertThat(memberResult.getMemberId(), equalTo(1L));
                        assertThat(memberResult.getUsername(), equalTo("user"));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("member 회원가입 실패 - 이미 존재하는 사용자명")
        void failSignUp_MemberAlreadyExists(){
            //given
            MemberSignUpRequest memberSignUpRequest = createSignUpRequest();

            given(memberRepository.existsByUsername(anyString()))
                    .willReturn(Mono.just(true));

            //when
            Mono<Member> fetchedMember = memberService.signUp(memberSignUpRequest);

            //then
            StepVerifier.create(fetchedMember)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(MEMBER_ALREADY_EXISTS.getMessage()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("member 로그인")
    class SignInMember{

        @Test
        @DisplayName("member 로그인 성공")
        void successSignIn(){
            //given
            Member member = createMember();
            MemberSignInRequest memberSignInRequest = createSignInRequest();

            given(memberRepository.findByUsername(anyString()))
                    .willReturn(Mono.just(member));
            given(jwtService.generateAccessToken(anyLong()))
                    .willReturn("any token");

            //when
            Mono<String> fetchedToken = memberService.signIn(memberSignInRequest);

            //then
            StepVerifier.create(fetchedToken)
                    .assertNext(tokenResult -> {
                        assertThat(tokenResult, equalTo("any token"));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("member 로그인 실패 - 멤버를 찾지 못함")
        void failSignIn_MemberNotFound(){
            //given
            MemberSignInRequest memberSignInRequest = createSignInRequest();

            given(memberRepository.findByUsername(anyString()))
                    .willReturn(Mono.empty());

            //when
            Mono<String> fetchedToken = memberService.signIn(memberSignInRequest);

            //then
            StepVerifier.create(fetchedToken)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(MEMBER_NOT_FOUND.getMessage()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("member 수정")
    class EditMember{

        @Test
        @DisplayName("member 수정 성공")
        void successEdit(){
            //given
            Member member = createMember();
            Member editedMember = createEditedMember();
            MemberEditRequest memberEditRequest = createEditRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));
            given(memberRepository.save(any(Member.class)))
                    .willReturn(Mono.just(editedMember));

            //when
            Mono<Member> fetchedMember = memberService.edit(memberEditRequest, "user");

            //then
            StepVerifier.create(fetchedMember)
                    .assertNext(memberResult -> {
                        assertThat(memberResult.getMemberId(), equalTo(1L));
                        assertThat(memberResult.getUsername(), equalTo("edited user"));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("member 수정 실패 - 유효하지 않은 요청")
        void failEdit_InvalidRequest(){
            //given
            Member member = createMember();
            MemberEditRequest memberEditRequest = createEditRequest();

            given(memberRepository.findById(anyLong()))
                    .willReturn(Mono.just(member));

            //when
            Mono<Member> fetchedMember = memberService.edit(memberEditRequest, "other user");

            //then
            StepVerifier.create(fetchedMember)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                            throwable.getMessage().equals(INVALID_REQUEST.getMessage()))
                    .verify();
        }
    }

}
