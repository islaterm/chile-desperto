# Chile despertó 1.1

Telegram bot that collects links to news, tweets, instagram posts, etc. about the 2019 Chilean protests.

Currently the bot get the links from ``@archivandochile`` telegram channel and from links sent to the bot via private messages and groups.
If you want to send a link to the bot, just send a message with the link to ``@noesguerrabot`` on Telegram.

## Periodic updates

**Every 5 minutes** the ``links.txt`` file is updated. 

**Every 1 hour**, the bot searches for the news with the topic _Protestas en Chile_ from the BBC and adds the links to new articles to the 
file.

Since the sources of the links are multiple and to avoid an exesive use of RAM there's no previous checking of the links added so it's 
likely to be **duplicated links** in the data.
