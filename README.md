# JavaMusicBot
The source code for dabBot

## Setup
1. Compile using maven

        mvn clean package        
2. Create `config.toml`

        token  = "Bot application token"
        owner  = "Owner discord user id"
        regex  = "Command parser regex pattern"
        game   = "Current playing game"
        invite = "Bot invite link"
        about  = "About text"
        join   = "Server join message"
        carbon = "carbonitex.net api key"
        dbots  = "bots.discord.pw api key"
    Sample pattern: `^\\?([a-zA-Z]+)(?:\\s+)?(.*)?` - for `?` as the prefix.
3. Run the bot

        java -jar target/JavaMusicBot.jar

## Dependencies
Dependencies are managed by Maven. See the maven [pom.xml](https://github.com/sponges/JavaMusicBot/blob/master/pom.xml) file.
```xml
<dependency>
    <groupId>net.dv8tion</groupId>
    <artifactId>JDA</artifactId>
    <version>3.0.BETA2_122</version>
</dependency>
<dependency>
    <groupId>com.sedmelluq</groupId>
    <artifactId>lavaplayer</artifactId>
    <version>1.1.42</version>
</dependency>
<dependency>
    <groupId>com.moandjiezana.toml</groupId>
    <artifactId>toml4j</artifactId>
    <version>0.7.1</version>
</dependency>
<dependency>
    <groupId>com.mashape.unirest</groupId>
    <artifactId>unirest-java</artifactId>
    <version>1.4.9</version>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.5</version>
</dependency>
```

## License
Licensed under Creative Commons Attribution NonCommercial (CC-BY-NC). See the `LICENSE` file in the root directory for 
full license text.

License summary (not legal advise, read the full license)
![](https://im.not.ovh/FfaTma29YrybOca.png)

Source: [tldrlegal.com](https://tldrlegal.com/license/creative-commons-attribution-noncommercial-(cc-nc)#summary)