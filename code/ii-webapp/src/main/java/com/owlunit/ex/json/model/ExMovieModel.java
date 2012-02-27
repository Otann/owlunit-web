package com.owlunit.ex.json.model;

import org.springframework.core.style.ToStringCreator;

import java.util.UUID;

public class ExMovieModel {

    /**
     * id in Owls database
     */
    private Long id;

    /**
     * DTO used to provide id in external service (f. e. YotaPlay)
     */
    private Long externalId;

    /**
     * Full movie name in original language (we are utf8 encoded)
     */
    private String name;

    public ExMovieModel(Long id, Long externalId, String name) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setUuid(Long id) {
        this.id = id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /* will be used later for obtaining unique versions of the same object in runtime */
//    private static final AtomicLong idSequence = new AtomicLong();

    public String toString() {
        return new ToStringCreator(this)
                .append("id", id)
                .append("externalId", externalId)
                .append("name", name)
                .toString();
    }
}