
# Tests for Fastly

## Prerequisites
- [Heroku account](https://signup.heroku.com)
- [Heroku toolbelt installed](https://toolbelt.heroku.com)
- [Fastly account](https://www.fastly.com/signup)
    
## To Run
FASTLY_API_KEY=<your_key> mvn test

This is slow as it creates (and destroys) the origin app on Heroku and the Fastly service.

If you specify a specific app name then subsequent runs will be faster, but you will be responsible for cleaning up:

APP_NAME=my-wonderful-name FASTLY_API_KEY=<your_key> mvn test
