package org.npathai.kata.application.domain.tag;

import org.npathai.kata.application.domain.tag.dto.Tag;

import java.util.Random;

public final class TagBuilder {
    private String id;
    private String name;

    private TagBuilder() {
        int random = new Random().nextInt(Integer.MAX_VALUE);
        id = "Tag" + random;
        name = id;
    }

    public static TagBuilder aTag() {
        return new TagBuilder();
    }

    public TagBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public TagBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Tag build() {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}
