package org.npathai.kata.application.domain.question;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QuestionServiceShould {

    private static final String TITLE = "First Question";
    private static final String BODY = "First question body";
    private static final List<String> TAGS = List.of("java", "kata");

    @Mock
    QuestionRepository questionRepository;

    @InjectMocks
    QuestionService questionService;

    PostQuestionRequest request;

    @BeforeEach
    public void setUp() {
        request = PostQuestionRequest.valid(TITLE, BODY, TAGS);
    }

    @Nested
    public class PostQuestionShould {

        private Question question;

        @BeforeEach
        public void setUp() {
            question = questionService.post(request);
            assertThat(question).isNotNull();
        }

        @Test
        public void createQuestionWithGivenDetails() {
            assertThat(question.getTitle()).isEqualTo(TITLE);
            assertThat(question.getBody()).isEqualTo(BODY);
        }
    }
}