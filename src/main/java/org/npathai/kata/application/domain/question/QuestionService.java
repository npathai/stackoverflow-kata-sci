package org.npathai.kata.application.domain.question;

import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;

public class QuestionService {

    public Question post(PostQuestionRequest validRequest) {
        Question question = new Question();
        question.setTitle(validRequest.getTitle());
        question.setBody(validRequest.getBody());
        return question;
    }
}
