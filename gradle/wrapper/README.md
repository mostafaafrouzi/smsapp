# Gradle Wrapper JAR

This directory should contain the `gradle-wrapper.jar` file which is required for the Gradle wrapper to function.

## How to get the JAR file:

1. **Download from Gradle**: Visit https://github.com/gradle/gradle/releases and download the latest stable version
2. **Use Android Studio**: When you open the project in Android Studio, it will automatically download the wrapper JAR
3. **Run gradle wrapper**: Execute `gradle wrapper` in the project root to generate the wrapper files

## Alternative approach:

If you don't have the JAR file, you can run:
```bash
gradle wrapper
```

This will generate all the necessary wrapper files including the JAR.

## Note:

The `gradle-wrapper.jar` file is a binary file that cannot be created through text editing. It must be downloaded or generated through Gradle itself.
