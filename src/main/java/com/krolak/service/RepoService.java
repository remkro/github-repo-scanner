package com.krolak.service;

import com.krolak.controller.dto.RepositoryInfoDto;
import com.krolak.model.Repository;
import com.krolak.service.exception.GitHubApiException;
import com.krolak.service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.krolak.service.utils.UrlUtils.prepareRepoBranchesUrl;
import static com.krolak.service.utils.UrlUtils.prepareUserReposUrl;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoService {
    @Value("${app.github.api.base-url}")
    private String gitHubApiBaseUrl;

    private final RestTemplate restTemplate;

    public RepositoryInfoDto getUserRepositories(String username, int page, int perPage) {
        String url = prepareUserReposUrl(gitHubApiBaseUrl, username, page, perPage);
        try {
            log.info("Attempting fetching repositories from GitHub API for username: {}", username);
            Repository[] repos = restTemplate.getForObject(url, Repository[].class);

            List<Repository> nonForkedRepos = repos == null ? Collections.emptyList() :
                    Stream.of(repos)
                            .filter(repo -> !repo.isFork())
                            .peek(repo -> repo.setBranches(getRepositoryBranches(username, repo.getName())))
                            .toList();

            return RepositoryInfoDto.builder()
                    .username(username)
                    .repositories(mapRepos(nonForkedRepos))
                    .elements(nonForkedRepos.size())
                    .pageNumber(page)
                    .pageSize(perPage)
                    .build();

        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("User doesn't exists: " + username);
        } catch (RestClientException e) {
            throw new GitHubApiException("GitHub API failed: " + e.getMessage());
        }
    }

    private List<Repository.Branch> getRepositoryBranches(String username, String repositoryName) {
        log.info("Attempting fetching branches from GitHub API for repository name: {}, username: {}", repositoryName, username);
        String url = prepareRepoBranchesUrl(gitHubApiBaseUrl, username, repositoryName);
        Repository.Branch[] branches = restTemplate.getForObject(url, Repository.Branch[].class);
        return branches == null ? List.of() : Arrays.asList(branches);
    }

    private List<RepositoryInfoDto.RepositoryDto> mapRepos(List<Repository> repos) {
        return repos.stream()
                .map(repo -> RepositoryInfoDto.RepositoryDto.builder()
                        .repositoryName(repo.getName())
                        .ownerLogin(repo.getOwner().getLogin())
                        .branches(mapBranches(repo.getBranches()))
                        .build())
                .toList();
    }

    private List<RepositoryInfoDto.BranchDto> mapBranches(List<Repository.Branch> branches) {
        return branches.stream()
                .map(branch -> RepositoryInfoDto.BranchDto.builder()
                        .name(branch.getName())
                        .lastCommitSha(branch.getCommit().getSha())
                        .build())
                .toList();
    }
}
