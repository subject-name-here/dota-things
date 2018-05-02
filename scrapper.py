import sys
import requests
import re
import time

# Part of processing argument

args = sys.argv[1:]
hours = 1
if len(args) > 0:
    hours = int(args[0])

# Part where it gets ids

url = 'https://ru.dotabuff.com/matches?hero=shadow-fiend&game_mode=1v1_solo_mid&skill_bracket=very_high_skill'

headers = {
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:45.0) Gecko/20100101 Firefox/45.0'
      }


matches = set()
for it in range(36 * hours):    
    print("Iteration #", it)
    try:
        response = requests.get(url, headers=headers).text
        result = re.findall(r'<a href=\"/matches/(\d+)\">', response)
        for r in result:
            matches.add(r)
    except BaseException as e:
        print(e)
    
    print("Sleeping...")
    time.sleep(100)


with open('match_id.txt', 'w') as file:
    for match in matches:
        file.write(match + '\n')


