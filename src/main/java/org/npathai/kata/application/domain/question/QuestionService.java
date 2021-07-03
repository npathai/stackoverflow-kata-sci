package org.npathai.kata.application.domain.question;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.question.usecase.*;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.data.domain.Page;

public class QuestionService {

    private final PostQuestionUseCase postQuestionUseCase;
    private final GetRecentQuestionsUseCase getRecentQuestionsUseCase;
    private final PostAnswerUseCase postAnswerUseCase;
    private final GetQuestionUseCase getQuestionUseCase;
    private final QuestionVotingUseCase questionVotingUseCase;

    public QuestionService(PostQuestionUseCase postQuestionUseCase,
                           GetRecentQuestionsUseCase getRecentQuestionsUseCase,
                           PostAnswerUseCase postAnswerUseCase,
                           GetQuestionUseCase getQuestionUseCase,
                           QuestionVotingUseCase questionVotingUseCase) {
        this.postQuestionUseCase = postQuestionUseCase;
        this.getRecentQuestionsUseCase = getRecentQuestionsUseCase;
        this.postAnswerUseCase = postAnswerUseCase;
        this.getQuestionUseCase = getQuestionUseCase;
        this.questionVotingUseCase = questionVotingUseCase;
    }

    public Question post(UserId userId, PostQuestionRequest validRequest) {
        return postQuestionUseCase.post(userId, validRequest);
    }

    public Page<Question> getRecentQuestions() {
        return getRecentQuestionsUseCase.getRecentQuestions();
    }

    public Answer postAnswer(UserId authorId, QuestionId questionId, PostAnswerRequest request) {
        return postAnswerUseCase.postAnswer(authorId, questionId, request);
    }

    public QuestionWithAnswers getQuestion(QuestionId questionId) {
        return getQuestionUseCase.getQuestion(questionId);
    }

    public Score voteQuestion(UserId userId, QuestionId questionId, VoteRequest voteRequest) throws
            BadRequestParametersException, ImpermissibleOperationException, InsufficientReputationException {
        return questionVotingUseCase.voteQuestion(userId, questionId, voteRequest);
    }

    public Score cancelVote(UserId voterId, QuestionId questionId) throws BadRequestParametersException {

        return questionVotingUseCase.cancelVote(voterId, questionId);
    }
}
