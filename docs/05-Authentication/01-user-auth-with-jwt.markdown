# User Authentication With JWT [](id=user-auth-with-jwt)

The default logic of the framework is to expose an initial authentication endpoint via JWT.

The endpoint made available is /authentication/login passing in POST username and password.

The login response also contains a set of useful information for profiling the front-end user.

```
{
    "token": "...",
    "authenticable": {
        "id": 25,
        "entityVersion": 5,
        "entityCreateDate": 1612942521721,
        "entityModifyDate": 1612944804118,
        "categoryIds": null,
        "tagIds": null,
        "name": .....",
        "lastname": "....",
        "username": "....",
        "admin": false,
        "email": ".....",
        "roles": [
            {
                "id": 18,
                "entityVersion": 1,
                "entityCreateDate": 1612880352019,
                "entityModifyDate": 1612880352019,
                "categoryIds": null,
                "tagIds": null,
                "name": "product-activator",
                "description": "Role related to product activator"
            }
        ]
    },
    "profile": {
        "it.acsoftware.licensemanager.activationcode.model.ActivationCode": {
            "permissions": [
                "upgrade_plan",
                "find",
                "save"
            ]
        }
    }
}
```

