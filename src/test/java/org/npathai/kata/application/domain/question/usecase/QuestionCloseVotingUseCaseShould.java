package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.CloseVote;
import org.npathai.kata.application.domain.question.dto.CloseVoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.UserBuilder;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class QuestionCloseVotingUseCaseShould {

    private static final String VOTER_ID_1 = "U1";
    private static final String VOTER_ID_2 = "U2";
    private static final String VOTER_ID_3 = "U3";
    private static final String VOTER_ID_4 = "U4";

    private static final String QUESTION_ID = "Q1";

    @Mock
    CloseVoteRepository closeVoteRepository;

    @Mock
    QuestionRepository questionRepository;
    
    @Mock
    UserService userService;

    @Mock
    IdGenerator closeVoteIdGenerator;

    QuestionCloseVotingUseCase useCase;
    Question question;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        useCase = new QuestionCloseVotingUseCase(closeVoteIdGenerator, closeVoteRepository);
        question = QuestionBuilder.aQuestion().build();
//        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
//        given(userService.getUserById(UserId.validated(VOTER_ID_1)))
//                .willReturn(UserBuilder.anUser().withId(VOTER_ID_1).build());
    }

    @Test
    @SneakyThrows
    public void recordCloseVote() {
        useCase.closeVote(UserId.validated(VOTER_ID_1), QuestionId.validated(QUESTION_ID));

        CloseVote closeVote = new CloseVote();
        closeVote.setId(closeVoteIdGenerator.get());
        closeVote.setVoterId(VOTER_ID_1);
        closeVote.setQuestionId(QUESTION_ID);

        verify(closeVoteRepository).save(closeVote);
    }

    @Test
    @SneakyThrows
    public void returnCloseVoteSummary() {
        CloseVoteSummary summary1 = useCase.closeVote(UserId.validated(VOTER_ID_1), QuestionId.validated(QUESTION_ID));
        assertThat(summary1.getCastVotes()).isEqualTo(1);
        assertThat(summary1.getRemainingVotes()).isEqualTo(3);

        CloseVote closeVote = new CloseVote();
        closeVote.setId(closeVoteIdGenerator.get());
        closeVote.setVoterId(VOTER_ID_1);
        closeVote.setQuestionId(QUESTION_ID);

        given(closeVoteRepository.findByQuestionId(QUESTION_ID)).willReturn(List.of(closeVote));

        CloseVoteSummary summary2 = useCase.closeVote(UserId.validated(VOTER_ID_2), QuestionId.validated(QUESTION_ID));
        assertThat(summary2.getCastVotes()).isEqualTo(2);
        assertThat(summary2.getRemainingVotes()).isEqualTo(2);
    }
}
