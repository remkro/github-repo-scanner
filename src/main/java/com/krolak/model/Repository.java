package com.krolak.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    private String name;
    private Owner owner;
    private boolean fork;
    private List<Branch> branches;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Branch {
        private String name;
        private Commit commit;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String sha;
    }
}

