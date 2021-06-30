package org.npathai.kata.acceptance.question.testview;

import lombok.Data;
import org.npathai.kata.acceptance.tag.testview.Tag;

import java.util.List;

@Data
public class Question {
    private String id;
    private String title;
    private String body;
    private List<Tag> tags;
    private String status;
    private String reason;
    private String createdAt;
    private String authorId;
}
