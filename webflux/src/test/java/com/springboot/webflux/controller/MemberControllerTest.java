package com.springboot.webflux.controller;

import com.springboot.webflux.dto.MemberEditRequest;
import com.springboot.webflux.dto.MemberResponse;
import com.springboot.webflux.dto.MemberSignInRequest;
import com.springboot.webflux.dto.MemberSignUpRequest;
import com.springboot.webflux.entity.Member;
import com.springboot.webflux.service.MemberService;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.springboot.webflux.constants.ExceptionStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@ExtendWith(SpringExtension.class)
@WebFluxTest(MemberController.class)
public class MemberControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MemberService memberService;

    private Member createMember(){
        return Member.builder()
                .memberId(1L)
                .username("user")
                .activityCount(0L)
                .positiveRate(0.5f)
                .registeredAt(LocalDateTime.now())
                .build();
    }

    private Member createMember(String username){
        return Member.builder()
                .memberId(1L)
                .username(username)
                .activityCount(0L)
                .positiveRate(0.5f)
                .registeredAt(LocalDateTime.now())
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
                .username("editedUsername")
                .password("editedPassword")
                .build();
    }

    @Nested
    @WithMockUser
    @DisplayName("memberId로 member 조회")
    class findByIdMember{

        @Test
        @DisplayName("memberId로 member 조회 성공")
        void successFind(){
            Member member = createMember();

            when(memberService.findById(anyLong()))
                    .thenReturn(Mono.just(member));

            webTestClient.get()
                    .uri("/member/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(MemberResponse.class)
                    .consumeWith(result -> {
                        MemberResponse memberResult = result.getResponseBody();

                        assert memberResult != null;
                        Assertions.assertEquals(member.getMemberId(), memberResult.getMemberId());
                        Assertions.assertEquals(member.getUsername(), memberResult.getUsername());
                        Assertions.assertEquals(member.getActivityCount(), memberResult.getActivityCount());
                        Assertions.assertEquals(member.getPositiveRate(), memberResult.getPositiveRate());
                        Assertions.assertEquals(member.getRegisteredAt(), memberResult.getRegisteredAt());
                    });

            verify(memberService, times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("memberId로 member 조회 실패 - 멤버를 찾지 못함")
        void failFindMemberNotFound(){

            when(memberService.findById(anyLong()))
                    .thenReturn(Mono.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())));

            webTestClient.get()
                    .uri("/member/1")
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(memberService, times(1)).findById(anyLong());
        }

    }

    @Nested
    @WithMockUser
    @DisplayName("member 회원가입")
    class signUpMember {

        @Test
        @DisplayName("member 회원가입 성공")
        void successSignUp() {
            Member member = createMember();
            MemberSignUpRequest memberSignUpRequest = createSignUpRequest();

            when(memberService.signUp(any(MemberSignUpRequest.class)))
                    .thenReturn(Mono.just(member));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/member/signup")
                    .bodyValue(memberSignUpRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(MemberResponse.class)
                    .consumeWith(result -> {
                        MemberResponse memberResult = result.getResponseBody();

                        assert memberResult != null;
                        Assertions.assertEquals(member.getMemberId(), memberResult.getMemberId());
                        Assertions.assertEquals(member.getUsername(), memberResult.getUsername());
                        Assertions.assertEquals(member.getActivityCount(), memberResult.getActivityCount());
                        Assertions.assertEquals(member.getPositiveRate(), memberResult.getPositiveRate());
                        Assertions.assertEquals(member.getRegisteredAt(), memberResult.getRegisteredAt());
                    });

            verify(memberService, times(1)).signUp(any(MemberSignUpRequest.class));
        }

        @Test
        @DisplayName("member 회원가입 실패 - 이미 존재하는 사용자명")
        void failSignUpMemberAlreadyExists() {
            MemberSignUpRequest memberSignUpRequest = createSignUpRequest();

            when(memberService.signUp(any(MemberSignUpRequest.class)))
                    .thenReturn(Mono.error(new RuntimeException(MEMBER_ALREADY_EXISTS.getMessage())));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/member/signup")
                    .bodyValue(memberSignUpRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(memberService, times(1)).signUp(any(MemberSignUpRequest.class));
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("member 로그인")
    class signInMember {

        @Test
        @DisplayName("member 로그인 성공")
        void successSignIn() {
            MemberSignInRequest memberSignInRequest = createSignInRequest();

            when(memberService.signIn(any(MemberSignInRequest.class)))
                    .thenReturn(Mono.just("anyToken"));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/member/signin")
                    .bodyValue(memberSignInRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class);

            verify(memberService, times(1)).signIn(any(MemberSignInRequest.class));
        }

        @Test
        @DisplayName("member 로그인 실패 - 멤버를 찾지 못함")
        void failSignInMemberNotFound() {
            MemberSignInRequest memberSignInRequest = createSignInRequest();

            when(memberService.signIn(any(MemberSignInRequest.class)))
                    .thenReturn(Mono.error(new RuntimeException(MEMBER_NOT_FOUND.getMessage())));

            webTestClient.mutateWith(csrf()).post()
                    .uri("/member/signin")
                    .bodyValue(memberSignInRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(memberService, times(1)).signIn(any(MemberSignInRequest.class));
        }
    }

    @Nested
    @WithMockUser
    @DisplayName("member 정보 수정")
    class editMember {

        @Test
        @DisplayName("member 정보 수정 성공")
        void successEdit() {
            Member member = createMember("editedMember");
            MemberEditRequest memberEditRequest = createEditRequest();

            when(memberService.edit(any(MemberEditRequest.class), anyString()))
                    .thenReturn(Mono.just(member));

            webTestClient.mutateWith(csrf()).patch()
                    .uri("/member")
                    .bodyValue(memberEditRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(MemberResponse.class)
                    .consumeWith(result -> {
                        MemberResponse memberResult = result.getResponseBody();

                        assert memberResult != null;
                        Assertions.assertEquals(member.getMemberId(), memberResult.getMemberId());
                        Assertions.assertEquals(member.getUsername(), memberResult.getUsername());
                        Assertions.assertEquals(member.getActivityCount(), memberResult.getActivityCount());
                        Assertions.assertEquals(member.getPositiveRate(), memberResult.getPositiveRate());
                        Assertions.assertEquals(member.getRegisteredAt(), memberResult.getRegisteredAt());
                    });

            verify(memberService, times(1)).edit(any(MemberEditRequest.class), anyString());
        }

        @Test
        @DisplayName("member 정보 수정 실패 - 유효하지 않은 요청")
        void failEditInvalidRequest() {
            MemberEditRequest memberEditRequest = createEditRequest();

            when(memberService.edit(any(MemberEditRequest.class), anyString()))
                    .thenReturn(Mono.error(new RuntimeException(INVALID_REQUEST.getMessage())));

            webTestClient.mutateWith(csrf()).patch()
                    .uri("/member")
                    .bodyValue(memberEditRequest)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(memberService, times(1)).edit(any(MemberEditRequest.class), anyString());
        }
    }
}
