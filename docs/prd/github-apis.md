## Fetch pulls
### curl
```
curl --location 'https://api.github.com/repos/chalomobility/chalo-app-kmp/pulls?per_page=2' \
--header 'Accept: application/vnd.github+json' \
--header 'Authorization: Bearer <TOKEN>' \
--header 'X-GitHub-Api-Version: 2022-11-28'
```

### Response
```
[
    {
        "url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/pulls/3221",
        "id": 3228320092,
        "issue_url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/issues/3221",
        "number": 3221,
        "state": "open",
        "locked": false,
        "title": "Booking success screen changes #3218 ",
        "user": {
            "login": "parakramsingh10",
            "id": 124552897,
            "type": "User",
        },
        "body": "Closes #3218 ",
        "created_at": "2026-01-30T15:41:33Z",
        "updated_at": "2026-01-31T08:28:58Z",
        "closed_at": null,
        "merged_at": null,
        "merge_commit_sha": "f4b0c89b41b178fab2d2e3366ded4fcf1101ce41",
        "assignees": [
            {
                "login": "kshitijkumar214",
                "id": 106008920,
                "type": "User",
            }
        ],
        "requested_reviewers": [
            {
                "login": "kshitijkumar214",
                "id": 106008920,
                "type": "User",
            }
        ],
        "labels": [],
        "milestone": null,
        "draft": false,
        "commits_url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/pulls/3221/commits",
        "review_comments_url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/pulls/3221/comments",
        "review_comment_url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/pulls/comments{/number}",
        "comments_url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/issues/3221/comments",
        "statuses_url": "https://api.github.com/repos/ChaloMobility/chalo-app-kmp/statuses/03caa361cbbcb55737b530d070807a13e6514ea2",
    },
]
```