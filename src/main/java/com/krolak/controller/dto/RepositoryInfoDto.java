package com.krolak.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class RepositoryInfoDto {
    private String username;
    private List<RepositoryDto> repositories;
    private int elements;
    private int pageNumber;
    private int pageSize;

    @Builder
    @Data
    public static class RepositoryDto {
        private String repositoryName;
        private String ownerLogin;
        private List<BranchDto> branches;
    }

    @Builder
    @Data
    public static class BranchDto {
        private String name;
        private String lastCommitSha;
    }
}
