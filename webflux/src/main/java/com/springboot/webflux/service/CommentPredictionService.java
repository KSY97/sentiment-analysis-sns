package com.springboot.webflux.service;

import com.springboot.webflux.dto.PredictionRequest;
import com.springboot.webflux.entity.Comment;
import com.springboot.webflux.repository.CommentRepository;
import com.springboot.webflux.repository.MemberRepository;
import com.springboot.webflux.security.SentimentAnalysisAuthToken;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.springboot.webflux.constants.ExceptionStatus.JSON_PARSE_FAILED;

@Service
@RequiredArgsConstructor
public class CommentPredictionService {

    @Autowired
    private final WebClient webClient;

    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    private final JSONParser parser = new JSONParser();

    private final static String PREDICTION_URI = "/api/analysis";
    private final static String TOKEN_URI = "/auth/token";
    private final static String POSITIVE = "positive";
    private final static String PERCENTAGE = "percentage";
    private final static String PREDICT = "predict";

    private Mono<String> sentimentAnalysis(String contents){

        PredictionRequest predictionRequest = PredictionRequest.builder()
                .contents(contents)
                .build();

        return webClient.post()
                .uri(PREDICTION_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, SentimentAnalysisAuthToken.token)
                .bodyValue(predictionRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    private Mono<String> getSentimentAnalysisToken(){
        return webClient.get()
                .uri(TOKEN_URI)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(token -> SentimentAnalysisAuthToken.token = token);
    }

    private Mono<String> callSentimentAnalysisApi(String contents) {

        if(SentimentAnalysisAuthToken.token == null){
            return getSentimentAnalysisToken()
                    .flatMap(token -> sentimentAnalysis(contents));
        }
        return sentimentAnalysis(contents);
    }

    private Mono<Comment> updatePositiveRate(Comment updatedComment){
        return memberRepository.findById(updatedComment.getMemberId())
                .flatMap(member -> {
                    Long count = member.getActivityCount() + 1;

                    Float positiveRate = member.getPositiveRate();
                    String predictResult = updatedComment.getPredictResult();
                    Float newRate = calculateNewPositiveRateForRegister(positiveRate, predictResult, count);

                    member.setPositiveRate(newRate);
                    member.setActivityCount(count);
                    return memberRepository.save(member).then(Mono.just(updatedComment));
                });

    }

    private Mono<Comment> getBackPositiveRate(Comment editedComment){

        return memberRepository.findById(editedComment.getMemberId())
                .flatMap(member -> {
                    Long count = member.getActivityCount() - 1;

                    Float positiveRate = member.getPositiveRate();
                    String predictResult = editedComment.getPredictResult();
                    Float newRate = calculateNewPositiveRateForEdit(positiveRate, predictResult, count);

                    member.setPositiveRate(newRate);
                    member.setActivityCount(count);
                    return memberRepository.save(member).then(Mono.just(editedComment));
                });

    }

    public Mono<Comment> callSentimentAnalysisApiAndSaveResultForRegister(Comment savedComment){

        return Mono.just(savedComment)
                .zipWhen(comment -> callSentimentAnalysisApi(comment.getContents()))
                .flatMap(tuple -> {
                    Comment comment = tuple.getT1();
                    String result = tuple.getT2();

                    return parseSentimentAnalysisResultJson(comment, result);
                })
                .flatMap(this::updatePositiveRate);
    }

    public Mono<Comment> callSentimentAnalysisApiAndSaveResultForEdit(Comment savedComment){

        return Mono.just(savedComment)
                .flatMap(this::getBackPositiveRate)
                .zipWhen(post -> callSentimentAnalysisApi(post.getContents()))
                .flatMap(tuple -> {
                    Comment comment = tuple.getT1();
                    String result = tuple.getT2();

                    return parseSentimentAnalysisResultJson(comment, result);
                })
                .flatMap(this::updatePositiveRate);
    }

    private float calculateNewPositiveRateForRegister(Float positiveRate, String predictResult, Long count){
        if(positiveRate == null){
            if(POSITIVE.equals(predictResult)){
                return 1f;
            }
            return 0f;
        } else {
            if(POSITIVE.equals(predictResult)){
                return (positiveRate * (count - 1) + 1) / count;
            }
        }
        return (positiveRate * (count - 1)) / count;
    }

    private Float calculateNewPositiveRateForEdit(Float positiveRate, String predictResult, Long count){
        if(POSITIVE.equals(predictResult)){
            return (positiveRate * (count + 1) - 1) / count;
        }
        return (positiveRate * (count + 1)) / count;
    }

    private Mono<Comment> parseSentimentAnalysisResultJson(Comment comment, String result){

        try {
            JSONObject resultJsonObj = (JSONObject) parser.parse(result);

            Float percentage = Float.valueOf(String.valueOf(resultJsonObj.get(PERCENTAGE)));
            String predict = String.valueOf(resultJsonObj.get(PREDICT));

            comment.setPredictResult(predict);
            comment.setPredictPercent(percentage);

            return commentRepository.save(comment);
        } catch (Exception e){
            return Mono.error(new RuntimeException(JSON_PARSE_FAILED.getMessage()));
        }
    }
}
