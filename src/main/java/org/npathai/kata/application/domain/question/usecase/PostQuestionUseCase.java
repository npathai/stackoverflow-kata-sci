package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.tag.persistence.TagRepository;
import org.npathai.kata.application.domain.user.UserId;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostQuestionUseCase {
    private final TagRepository tagRepository;
    private final IdGenerator questionIdGenerator;
    private final IdGenerator tagIdGenerator;
    private final Clock clock;
    private final QuestionRepository questionRepository;

    public PostQuestionUseCase(QuestionRepository questionRepository,
                               TagRepository tagRepository,
                               IdGenerator questionIdGenerator,
                               IdGenerator tagIdGenerator,
                               Clock clock) {
        this.questionRepository = questionRepository;
        this.tagRepository = tagRepository;
        this.questionIdGenerator = questionIdGenerator;
        this.tagIdGenerator = tagIdGenerator;
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
}
