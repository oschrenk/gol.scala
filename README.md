# Game of Life

```
sbt docker
docker run -p 8080:8080 oschrenk/gameoflife
open "http://localhost:8080"
```

##  Deploy

```
brew install heroku/brew/heroku

heroku login
heroku container:login
heroku create

sbt docker
cd target/docker
heroku container:push web
heroku container:release web
heroku open
```
