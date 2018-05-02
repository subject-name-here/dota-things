#!/bin/bash

python3 scrapper.py $1
python3.5 get_links.py
python3.5 downloader.py

rm -rf ../replayz
mkdir ../replayz
> match_id.txt
> match_links.txt
