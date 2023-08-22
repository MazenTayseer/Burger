# Burger Discord Bot

Burger is a multifunctional Discord bot built using Java and powered by the Discord JDA. The bot specializes in providing music playback, casino games, lo-fi channels, and other utility commands for enhancing your Discord experience.

## 📝 Note
Sadly, the bot encountered limitations and can no longer join additional servers beyond 100. It currently resides in 97 servers. Instead of letting my efforts go in vain, I've decided to share the building process in hopes of helping and inspiring others in the community.

## Features

### 🎵 Music
- **Play**: Play a song from a given URL or search term.
- **Stop**: Stop the currently playing song and clear the queue.
- **Pause**: Pause the current song.
- **Resume**: Resume the paused song.
- **Queue**: Display the current song queue.
- **Loop**: Loop the current song or the entire queue.
- **SkipTo**: Skip to a particular song in the queue by index.
- **Skip**: Skip the current song.
- **Seek**: Jump to a specific time in the current song.
- **Clear**: Clear the current song queue.
- **NowPlaying**: Display information about the currently playing song.
- **Leave**: Exit the voice channel.
- **Remove**: Remove a song from the queue by index.
- **Volume**: Adjust the playback volume.
- **Shuffle**: Shuffle the current song queue.
- **Lyrics**: Fetch and display the lyrics for a song.

### 🎰 Casino Games
- **blackjack**: Play a game of blackjack.
- **crash**: Test your luck with the crash game.
- **dice**: Roll a dice.
- **highlow**: Guess if the next card will be higher or lower.
- **hr**: Bet on horses in a race.
- **trivia**: Test your knowledge with trivia questions.
- **bal**: Check your in-bot currency balance.
- **wage**: Wager in-bot currency.
- **vote**: Vote and potentially win in-bot currency.
- **leaderboard**: Display the top users by in-bot currency.

### 🎧 Lo-Fi
- **Lofi**: Start a lo-fi channel to relax and focus.
- **Leave**: Exit the lo-fi channel.
- **NowPlaying**: Display information about the current lo-fi track.
- **Skip**: Skip to the next lo-fi track.
- **Pause**: Pause the lo-fi playback.
- **Resume**: Resume the paused lo-fi track.
- **Volume**: Adjust the lo-fi playback volume.

### 🍔 Other
- **Burger**: Gives you a burger!
- **ServerInfo**: Display information about the current server.
- **Delete**: Deletes a number of messages specified by the user.
- **CoinFlip**: Flip a coin and get heads or tails.
- **Help**: Display a list of available commands and their descriptions.
- **Meme**: Fetch and display a random meme.
- **Stats**: Display bot statistics.

## Getting Started

### Prerequisites:

1. **Java Development Kit (JDK) 18**: Download from [Oracle's official site](https://www.oracle.com/java/technologies/javase-jdk18-downloads.html) or your preferred distribution.
  
2. **Maven**: Obtain it from [Maven's official website](https://maven.apache.org/download.cgi).
  
3. **Database**: Ensure you have both:
   - **PostgreSQL**: [Download and install](https://www.postgresql.org/download/).
   - **SQLite**: Check [here](https://www.sqlite.org/download.html) if it's not already on your system.

4. **Environment Variables**: Ensure a `.env` file with all necessary configurations is in the root directory.

### Installation Steps:

1. **Clone the Repository**: 
   ```
   git clone https://github.com/MazenTayseer/Burger
   ```

2. **Navigate to the project directory**:
   ```
   cd Burger
   ```

3. **Install Dependencies with Maven**:
   ```
   mvn install
   ```

4. **Database Setup**:
   - For PostgreSQL, set up a new database and update the connection details in your `.env` file.
   - Ensure SQLite database has write access in its directory.

5. **Compile and Package**:
   ```
   mvn package
   ```

6. **Run the Bot**:
   ```
   ./target/mazenburgerbot
   ```

### Troubleshooting:

- Check repositories in `pom.xml` if there are dependency issues.
  
- Ensure the database is running and accessible.
  
- Confirm you're using the correct Java version.

## Contribution & Development

Contribute to our efforts! For developers aiming to innovate or build atop Burger, our GitHub repository is available [here](https://github.com/MazenTayseer/Burger).

## License

This project is licensed under the MIT License. By using this software, you acknowledge that it comes without any warranty.

For detailed information, please see the `LICENSE` file in the project's root directory.

---

*Remember: every challenge is a chance to grow. Happy coding!* 🍔🎵🎰🎧
