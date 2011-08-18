package com.manymonkeys.ex.json.model;

import org.springframework.core.style.ToStringCreator;

import java.util.UUID;

public class ExMovieModel {

    /**
     * id in Owls database
     */
    private UUID uuid;

    /**
     * DTO used to provide id in external service (f. e. YotaPlay)
     */
    private Long externalId;

    /**
     * Full movie name in original language (we are utf8 encoded)
     */
    private String name;

    public ExMovieModel(UUID uuid, Long externalId, String name) {
        this.uuid = uuid;
        this.externalId = externalId;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
                .append("uuid", uuid)
                .append("externalId", externalId)
                .append("name", name)
                .toString();
    }
}