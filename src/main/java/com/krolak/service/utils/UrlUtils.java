package com.krolak.service.utils;

public class UrlUtils {
    public static String prepareUserReposUrl(String baseUrl, String username, int page, int perPage) {
        return String.format("%s/users/%s/repos?per_page=%d&page=%d", baseUrl, username, perPage, page);
    }

    public static String prepareRepoBranchesUrl(String baseUrl, String username, String repositoryName) {
        return String.format("%s/repos/%s/%s/branches", baseUrl, username, repositoryName);
    }
}
