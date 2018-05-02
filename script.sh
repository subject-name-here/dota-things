#!/bin/bash

mkdir replayz
cd replay_getter

python3 scrapper.py $1
python3.5 get_links.py
python3.5 downloader.py

> match_id.txt
> match_links.txt

cd ..

rm -rf replayz


