### Google photos directory syncer

Based on the following example project: https://github.com/google/java-photoslibrary

### Prerequisites

Generate a working API key for your google photos API:

https://developers.google.com/photos/library/guides/get-started

### Configuration

You find all the configurable parameters in the `CONFIG` file. Feel free to tweak these.

Try it out:
```
#set config parameter
source CONFIG
#run single shot if syncer
./gradlew :folder-syncer:run```

