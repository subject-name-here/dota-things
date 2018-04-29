import bz2
import urllib.request
import os

with open('match_links.txt', 'r') as file:
    for link in file.readlines():
        if link.endswith('.dem.bz2\n'):
            try:
                print("Started download " + link)
                ind = link.rfind('/')
                filename = link[ind + 1:].replace('\n', '')
                urllib.request.urlretrieve(link, "../replayz/" + filename)
                print("Success!")
            except BaseException as e:
                print("Failed to download: " + str(e))

print("Finished download!")

for filename in os.listdir("../replayz"):
    print("Unzip " + filename)
    zipfile = bz2.BZ2File("../replayz/" + filename)
    data = zipfile.read()
    newfilepath = "../replays/" + filename[:-4]
    open(newfilepath, 'wb').write(data)

print("Finished unzipping!")
