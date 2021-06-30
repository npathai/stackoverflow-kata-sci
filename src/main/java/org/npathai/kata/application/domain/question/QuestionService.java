package org.npathai.kata.application.domain.question;

import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.TagService;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.user.UserId;

import java.time.Clock;
import java.util.List;

public class QuestionService {

    private final TagService tagService;
    private final QuestionRepository questionRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public QuestionService(TagService tagService, QuestionRepository questionRepository, IdGenerator idGenerator, Clock clock) {
        this.tagService = tagService;
        this.questionRepository = questionRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public Question post(UserId userId, PostQuestionRequest validRequest) {
        Question question = new Question();
        question.setId(idGenerator.get());
        question.setAuthorId(userId.getId());
        question.setTitle(validRequest.getTitle());
        question.setBody(validRequest.getBody());
        question.setCreatedAt(clock.millis());

        List<Tag> tags = tagService.getOrCreateTags(validRequest.getTags());
        question.setTags(tags);

        questionRepository.save(question);

        return question;
    }
}
