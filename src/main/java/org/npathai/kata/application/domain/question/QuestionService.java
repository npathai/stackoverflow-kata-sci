package org.npathai.kata.application.domain.question;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.question.usecase.GetQuestionUseCase;
import org.npathai.kata.application.domain.question.usecase.GetRecentQuestionsUseCase;
import org.npathai.kata.application.domain.question.usecase.PostAnswerUseCase;
import org.npathai.kata.application.domain.question.usecase.PostQuestionUseCase;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;
import org.springframework.data.domain.Page;

public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final UserService userService;
    private final IdGenerator voteIdGenerator;
    private final PostQuestionUseCase postQuestionUseCase;
    private final GetRecentQuestionsUseCase getRecentQuestionsUseCase;
    private final PostAnswerUseCase postAnswerUseCase;
    private final GetQuestionUseCase getQuestionUseCase;

    public QuestionService(PostQuestionUseCase postQuestionUseCase,
                           GetRecentQuestionsUseCase getRecentQuestionsUseCase,
                           PostAnswerUseCase postAnswerUseCase,
                           GetQuestionUseCase getQuestionUseCase,
                           QuestionRepository questionRepository,
                           AnswerRepository answerRepository,
                           UserService userService,
                           VoteRepository voteRepository,
                           IdGenerator voteIdGenerator) {
        this.postQuestionUseCase = postQuestionUseCase;
        this.getRecentQuestionsUseCase = getRecentQuestionsUseCase;
        this.postAnswerUseCase = postAnswerUseCase;
        this.getQuestionUseCase = getQuestionUseCase;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userService = userService;
        this.voteRepository = voteRepository;
        this.voteIdGenerator = voteIdGenerator;
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

    private Question getQuestionExplosively(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }

    public Score voteQuestion(UserId userId, QuestionId questionId, VoteRequest voteRequest) throws
            BadRequestParametersException, ImpermissibleOperationException, InsufficientReputationException {
        Question question = getQuestionExplosively(questionId);
        User voter = userService.getUserById(userId);
        User author = userService.getUserById(UserId.validated(question.getAuthorId()));

        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't cast vote on own question");
        }

        Score score = new Score();
        if (voteRequest.getType() == VoteType.UP) {
            if (voter.getReputation() < 15) {
                throw new InsufficientReputationException();
            }

            score.setScore(question.getScore() + 1);
            voter.setCastUpVotes(voter.getCastUpVotes() + 1);
            author.setReputation(author.getReputation() + 10);
        } else {
            if (voter.getReputation() < 125) {
                throw new InsufficientReputationException();
            }
            voter.setCastDownVotes(voter.getCastDownVotes() + 1);
            score.setScore(question.getScore() - 1);
            author.setReputation(author.getReputation() - 5);
        }

        userService.update(voter);
        userService.update(author);

        question.setScore(score.getScore());
        questionRepository.save(question);

        Vote vote = new Vote();
        vote.setId(voteIdGenerator.get());
        vote.setQuestionId(question.getId());
        vote.setVoterId(voter.getId());
        vote.setType(voteRequest.getType().val);

        voteRepository.save(vote);

        return score;
    }

    public Score cancelVote(UserId voterId, QuestionId questionId) throws BadRequestParametersException {
        User voter = userService.getUserById(voterId);
        Question question = getQuestionExplosively(questionId);
        Vote vote = voteRepository.findByQuestionIdAndVoterId(question.getId(), voterId.getId());
        User author = userService.getUserById(UserId.validated(question.getAuthorId()));

        if (VoteType.UP.val.equals(vote.getType())) {
            question.setScore(question.getScore() - 1);
            voter.setCastUpVotes(voter.getCastUpVotes() - 1);
            author.setReputation(author.getReputation() - 10);
        } else {
            question.setScore(question.getScore() + 1);
            voter.setCastDownVotes(voter.getCastDownVotes() - 1);
            author.setReputation(author.getReputation() + 5);
        }

        Score score = new Score();
        score.setScore(question.getScore());

        questionRepository.save(question);
        voteRepository.delete(vote);
        userService.update(voter);
        userService.update(author);

        return score;
    }

}
