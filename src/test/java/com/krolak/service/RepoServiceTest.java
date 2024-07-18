package com.krolak.service;

import com.krolak.controller.dto.RepositoryInfoDto;
import com.krolak.model.Repository;
import com.krolak.service.exception.GitHubApiException;
import com.krolak.service.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RepoService repoService;

    @Test
    void getUserRepositories_ShouldReturnRepositoryInfo() {
        Repository[] repositories = new Repository[]{
                createRepository("repo1", "testUser", false),
                createRepository("repo2", "testUser", true)
        };
        when(restTemplate.getForObject(anyString(), eq(Repository[].class))).thenReturn(repositories);

        Repository.Branch[] branches = new Repository.Branch[]{
                createBranch("main", "abc123")
        };
        when(restTemplate.getForObject(anyString(), eq(Repository.Branch[].class))).thenReturn(branches);

        RepositoryInfoDto result = repoService.getUserRepositories("testUser", 0, 5);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals(1, result.getRepositories().size());
        assertEquals("repo1", result.getRepositories().getFirst().getRepositoryName());
        assertEquals(1, result.getRepositories().getFirst().getBranches().size());
        assertEquals("main", result.getRepositories().getFirst().getBranches().getFirst().getName());
        assertEquals("abc123", result.getRepositories().getFirst().getBranches().getFirst().getLastCommitSha());
        assertEquals(1, result.getElements());
        assertEquals(0, result.getPageNumber());
        assertEquals(5, result.getPageSize());
    }

    @Test
    void getUserRepositories_ShouldThrowUserNotFoundException() {
        when(restTemplate.getForObject(anyString(), eq(Repository[].class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(UserNotFoundException.class, () -> repoService.getUserRepositories("nonExistentUser", 0, 5));
    }

    @Test
    void getUserRepositories_ShouldThrowGitHubApiException() {
        when(restTemplate.getForObject(anyString(), eq(Repository[].class)))
                .thenThrow(new RestClientException("API Error"));

        assertThrows(GitHubApiException.class, () -> repoService.getUserRepositories("testUser", 0, 5));
    }

    private Repository createRepository(String name, String ownerLogin, boolean fork) {
        Repository repo = new Repository();
        repo.setName(name);
        repo.setFork(fork);
        Repository.Owner owner = new Repository.Owner();
        owner.setLogin(ownerLogin);
        repo.setOwner(owner);
        return repo;
    }

    private Repository.Branch createBranch(String name, String commitSha) {
        Repository.Branch branch = new Repository.Branch();
        branch.setName(name);
        Repository.Commit commit = new Repository.Commit();
        commit.setSha(commitSha);
        branch.setCommit(commit);
        return branch;
    }
}