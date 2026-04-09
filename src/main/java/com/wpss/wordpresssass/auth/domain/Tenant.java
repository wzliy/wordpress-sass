package com.wpss.wordpresssass.auth.domain;

public class Tenant {

    private final Long id;
    private final String name;

    public Tenant(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
