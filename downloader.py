from steam import SteamClient
from dota2 import Dota2Client, util
import logging

#logging.basicConfig(format='[%(asctime)s] %(levelname)s %(name)s: %(message)s', level=logging.DEBUG)

client = SteamClient()
dota = Dota2Client(client)


@client.on('logged_on')
def start_dota():
    dota.launch()


links = set()
@dota.on('ready')
def read_ids_from_file():
    ids = set()
    print("started")

    with open('match_id.txt', 'r') as file:
        lines = file.readlines()
        for line in lines:
            match_id = int(line)
            if match_id not in ids:
                ids.add(match_id)
                job_id = dota.request_match_details(match_id)
                dota.wait_msg(job_id, timeout=10)

    print("finished")
    with open('match_links.txt', 'a') as file:
        for link in links:
            file.write(link)
            file.write('\n')

    print("saved")


@dota.on('match_details')
def process_match_details(match_id, eresult, match):
    link = util.replay_url_from_match(match)
    links.add(link)


client.cli_login()
client.run_forever()




