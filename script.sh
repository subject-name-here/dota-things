#!/bin/bash

rm -rf replayz
mkdir replayz

cd replay_getter

> match_id.txt
> match_links.txt

python3 scrapper.py $1
python3.5 get_links.py
python3.5 downloader.py

cd ..




