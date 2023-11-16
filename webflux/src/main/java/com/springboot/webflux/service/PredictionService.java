package com.springboot.webflux.service;

import com.springboot.webflux.dto.PredictionRequest;
import com.springboot.webflux.repository.CommentRepository;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.repository.PostRepository;
import com.springboot.webflux.repository.entity.Comment;
import com.springboot.webflux.repository.entity.Post;
import com.springboot.webflux.security.AuthToken;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PredictionService {

    @Autowired
    private final WebClient webClient;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private JSONParser parser = new JSONParser();

    public Mono<String> sentimentAnalysis(PredictionRequest predictionRequest){

        return webClient.post()
                .uri("/api/tospring")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, AuthToken.token)
                .bodyValue(predictionRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getToken(){
        return webClient.get()
                .uri("/auth/token")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(token -> AuthToken.token = token);
    }

    public Mono<String> callApi(PredictionRequest predictionRequest) {

        if(AuthToken.token == null){
            return getToken().doOnNext(token ->{})
                    .flatMap(token -> sentimentAnalysis(predictionRequest));
        } else {
            return sentimentAnalysis(predictionRequest);
        }
    }

    @Service
    public class PostPredict{

        public Mono<Post> updatePositiveRate(Post updatedPost){
            return memberRepository.findById(updatedPost.getMemberId())
                    .flatMap(member ->
                            postRepository.countByMemberId(member.getMemberId())
                                .flatMap(count -> {
                                    Float positiveRate = member.getPositiveRate();
                                    String predictResult = updatedPost.getPredictResult();
                                    Float newRate;

                                    if(positiveRate == null){
                                        if("positive".equals(predictResult)){
                                            newRate = 1f;
                                        }else {
                                            newRate = 0f;
                                        }
                                    } else {
                                        if("positive".equals(predictResult)){
                                            newRate = (positiveRate * (count - 1) + 1) / count;
                                        }else {
                                            newRate = (positiveRate * (count - 1)) / count;
                                        }
                                    }

                                    member.updatePositiveRate(newRate);
                                    memberRepository.save(member).subscribe();
                                    return Mono.just(updatedPost);
                                })
                    );
        }

        public Mono<Post> callApiAndSave(Post savedPost){

            return postRepository.findById(savedPost.getPostId())

                    .flatMap(post -> callApi(
                                        PredictionRequest.builder()
                                                .contents(post.getContents())
                                                .build()
                                    )
                                    .flatMap(resultStr -> {
                                        try{
                                            JSONObject resultJsonObj = (JSONObject) parser.parse(resultStr);

                                            Float percentage = Float.valueOf(String.valueOf(resultJsonObj.get("percentage")));
                                            String result = String.valueOf(resultJsonObj.get("predict"));

                                            post.updatePrediction(result, percentage);

                                            postRepository.findByMemberId(post.getMemberId());

                                            return postRepository.save(post);

                                        } catch (Exception e){
                                            return Mono.error(new RuntimeException("JSON 파싱 실패"));
                                        }
                                    })
                                    .flatMap(updatedPost -> updatePositiveRate(updatedPost))
                    );
        }
    }

    @Service
    public class CommentPredict{

        public Mono<Comment> updatePositiveRate(Comment updatedComment){
            return memberRepository.findById(updatedComment.getMemberId())
                    .flatMap(member ->
                            postRepository.countByMemberId(member.getMemberId())
                                .flatMap(count -> {
                                    Float positiveRate = member.getPositiveRate();
                                    String predictResult = updatedComment.getPredictResult();
                                    Float newRate;

                                    if(positiveRate == null){
                                        if("positive".equals(predictResult)){
                                            newRate = 1f;
                                        }else {
                                            newRate = 0f;
                                        }
                                    } else {
                                        if("positive".equals(predictResult)){
                                            newRate = (positiveRate * (count - 1) + 1) / count;
                                        }else {
                                            newRate = (positiveRate * (count - 1)) / count;
                                        }
                                    }

                                    member.updatePositiveRate(newRate);
                                    memberRepository.save(member).subscribe();
                                    return Mono.just(updatedComment);
                                })
                    );
        }

        public Mono<Comment> callApiAndSave(Comment savedComment){

            return commentRepository.findById(savedComment.getCommentId())
                            .flatMap(comment ->
                                    callApi(
                                        PredictionRequest.builder()
                                                .contents(comment.getContents())
                                                .build()
                                    )
                                    .flatMap(resultStr -> {
                                        try{
                                            JSONObject resultJsonObj = (JSONObject) parser.parse(resultStr);

                                            Float percentage = Float.valueOf(String.valueOf(resultJsonObj.get("percentage")));
                                            String result = String.valueOf(resultJsonObj.get("predict"));

                                            comment.updatePrediction(result, percentage);

                                            return commentRepository.save(comment);

                                        } catch (Exception e){
                                            return Mono.error(new RuntimeException("JSON 파싱 실패"));
                                        }
                                    })
                                    .flatMap(updatedPost -> updatePositiveRate(updatedPost))
                    );
        }
    }
}
