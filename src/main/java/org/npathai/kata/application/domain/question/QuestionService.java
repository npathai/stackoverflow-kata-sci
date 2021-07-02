package org.npathai.kata.application.domain.question;

import lombok.SneakyThrows;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.tag.persistence.TagRepository;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QuestionService {

    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final IdGenerator questionIdGenerator;
    private final IdGenerator tagIdGenerator;
    private final IdGenerator answerIdGenerator;
    private final UserService userService;
    private final Clock clock;

    public QuestionService(TagRepository tagRepository, QuestionRepository questionRepository,
                           AnswerRepository answerRepository, UserService userService, IdGenerator questionIdGenerator,
                           IdGenerator tagIdGenerator, IdGenerator answerIdGenerator, Clock clock) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userService = userService;
        this.questionIdGenerator = questionIdGenerator;
        this.tagIdGenerator = tagIdGenerator;
        this.answerIdGenerator = answerIdGenerator;
        this.clock = clock;
    }

    public Question post(UserId userId, PostQuestionRequest validRequest) {
        Question question = new Question();
        question.setId(questionIdGenerator.get());
        question.setAuthorId(userId.getId());
        question.setTitle(validRequest.getTitle());
        question.setBody(validRequest.getBody());
        question.setCreatedAt(clock.millis());

        List<Tag> tags = getOrCreateTags(validRequest.getTags());
        question.setTags(tags);

        questionRepository.save(question);

        return question;
    }

    private List<Tag> getOrCreateTags(List<String> tags) {
        List<Tag> existingTags = tagRepository.findAllByName(tags);
        if (existingTags.size() == tags.size()) {
            return existingTags;
        }

        return createNonExisting(tags, existingTags);
    }

    private List<Tag> createNonExisting(List<String> tags, List<Tag> existingTags) {
        List<Tag> allTags = new ArrayList<>(existingTags);

        Set<String> tagNamesSet = new HashSet<>(tags);
        Set<String> foundSet = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        tagNamesSet.removeAll(foundSet);

        allTags.addAll(createTags(tagNamesSet));
        return allTags;
    }

    private List<Tag> createTags(Set<String> tagNamesSet) {
        List<Tag> createdTags = new ArrayList<>();
        for (String name : tagNamesSet) {
            Tag tag = new Tag();
            tag.setId(tagIdGenerator.get());
            tag.setName(name);

            createdTags.add(tag);
            tagRepository.save(tag);
        }
        return createdTags;
    }

    public Page<Question> getRecentQuestions() {
        return questionRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Answer postAnswer(UserId authorId, QuestionId questionId, PostAnswerRequest request) {
        Question question = getQuestionExplosively(questionId);
        Answer answer = new Answer();
        answer.setId(answerIdGenerator.get());
        answer.setAuthorId(authorId.getId());
        answer.setQuestionId(questionId.getId());
        answer.setBody(request.getBody());

        answerRepository.save(answer);

        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);

        return answer;
    }

    public QuestionWithAnswers getQuestion(QuestionId questionId) {
        Question question = getQuestionExplosively(questionId);
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        QuestionWithAnswers questionWithAnswers = new QuestionWithAnswers();
        questionWithAnswers.setQuestion(question);
        questionWithAnswers.setAnswers(answers);
        return questionWithAnswers;
    }

    private Question getQuestionExplosively(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }

    @SneakyThrows
    public Score voteQuestion(UserId userId, QuestionId questionId, VoteRequest voteRequest) {
        Question question = getQuestionExplosively(questionId);
        User voter = userService.getUserById(userId);
        User author = userService.getUserById(UserId.validated(question.getAuthorId()));

        Score score = new Score();
        if (voteRequest.getType() == VoteType.UP) {
            score.setScore(question.getScore() + 1);
            voter.setCastUpVotes(voter.getCastUpVotes() + 1);
            author.setReputation(author.getReputation() + 10);
        } else {
            voter.setCastDownVotes(voter.getCastDownVotes() + 1);
            score.setScore(question.getScore() - 1);
            author.setReputation(author.getReputation() - 5);
        }

        userService.update(voter);
        userService.update(author);

        question.setScore(score.getScore());
        questionRepository.save(question);

        return score;
    }
}
