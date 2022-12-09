<p align="center">
    <img src=".github/assets/Carbon_Banner.png" alt="Carbon plugin banner." width="500" height="auto" /><br>
<b>Carbon</b> is a modern chat Java Edition plugin built on channels, with just about every single setting and format configurable.
</p>

## Support

Support is given through [GitHub Issues](https://github.com/Hexaoxide/Carbon/issues)
and [Discord](https://discord.gg/S8s75Yf).  
Please use the discord for help setting up the plugin, and use issues for bug reports.

## Checkstyle

Carbon uses (a fork of) checkstyle to ensure code style is consistent across the entire project.  
For checkstyle support in IDEA:

1. Install the [checkstyle plugin](https://github.com/jshiell/checkstyle-idea).
2. Compile https://gitlab.com/stellardrift/stylecheck
3. `Settings` -> `Tools` -> `Checkstyle` `Third-Party Checks`, add the compiled stylecheck jar
4. While still in the `Checkstyle` tab, go to `Configuration File`, add `.checkstyle/checkstyle.xml` and tick the check
   box.
