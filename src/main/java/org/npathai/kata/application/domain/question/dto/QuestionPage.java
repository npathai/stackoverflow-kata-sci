package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class QuestionPage {

    private List<Question> questions;
}
