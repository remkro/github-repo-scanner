# GitHub Repository Scanner

This application provides an API to fetch GitHub repositories for a given user, excluding forks. It includes detailed information about each repository and its branches.

## Features

- Fetch non-forked GitHub repositories for a specific user
- Provide repository details including name, owner, and branch information
- Pagination support
- Error handling for non-existent users and GitHub API issues
- API key authentication

## Build With
- Java 21: Programming language used.
- Spring Boot 3: Framework for building the API.
- Gradle: Build automation tool.
- Lombok: Library for minimizing boilerplate code.
- RestTemplate: HTTP client for making API calls.
- SLF4J: Logging framework.

## API Endpoints

### Get User Repositories

`GET api/v1/github/repos/{username}`

#### Parameters

- `username` (path parameter): GitHub username
- `page` (query parameter, optional): Page number (default: 0)
- `size` (query parameter, optional): Page size (default: 5)

#### Headers

- `Accept: application/json`
- `x-api-key: your-api-key`

Please use the following api key: `8d473e8f-174d-4626-839d-cf01c44f649e`

#### Success Response

```json
{
  "username": "string",
  "repositories": [
    {
      "repositoryName": "string",
      "ownerLogin": "string",
      "branches": [
        {
          "name": "string",
          "lastCommitSha": "string"
        }
      ]
    }
  ],
  "elements": 1,
  "pageNumber": 0,
  "pageSize": 5
}
````

#### Error Response
For non-existent users:

```json
{
  "status": 404,
  "message": "User doesn't exist: {username}"
}
````

## Error Handling

The application handles two specific errors:
1. **UserNotFoundException:** Thrown when the GitHub user does not exist. Returns a 404 status with a descriptive message.
2. **GitHubApiException:** Thrown when there is an error interacting with the GitHub API. Returns a 500 status with a descriptive message.

## How To Run

1. Clone the repository.
2. Set up your environment variables for app.github.api.base-url.
3. Build the project using Gradle: ./gradlew clean build.
4. Run the application: ./gradlew bootRun.
5. Use an API testing tool like Postman to test the endpoints.