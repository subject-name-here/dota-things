import json
from django.http import HttpResponse
from saver.models import *


def save(request):
    json_data = json.loads(request.body.decode("utf-8"))

    deserialized = json_data.get('action')
    a = Action()
    a.action_type = deserialized["actionType"]

    a.param = deserialized["param"]
    a.dx = deserialized["nx"]
    a.dy = deserialized["ny"]

    a.save()

    deserialized = json_data.get('state')

    s = State()
    s.action_done_id = a.id
    s.our_team = deserialized["ourTeam"]
    s.enemy_hero = deserialized["enemyName"]
    s.time = deserialized["time"]

    s.x = deserialized["ourX"]
    s.y = deserialized["ourY"]
    s.facing = deserialized["ourFacing"]

    s.lvl = deserialized["ourLvl"]
    s.damage = deserialized["ourAttackDamage"]
    s.gold = deserialized["ourGold"]

    s.hp = deserialized["ourHp"]
    s.max_hp = deserialized["ourMaxHp"]
    s.mana = deserialized["ourMana"]
    s.max_mana = deserialized["ourMaxMana"]

    s.available1 = deserialized["isOurAbility1Available"]
    s.available2 = deserialized["isOurAbility2Available"]
    s.available3 = deserialized["isOurAbility3Available"]
    s.available4 = deserialized["isOurAbility4Available"]

    s.enemy_visible = deserialized["isEnemyVisible"]
    s.enemy_x = deserialized["enemyX"]
    s.enemy_y = deserialized["enemyY"]
    s.enemy_facing = deserialized["enemyFacing"]

    s.enemy_lvl = deserialized["enemyLvl"]
    s.enemy_damage = deserialized["enemyAttackDamage"]

    s.enemy_hp = deserialized["enemyHp"]
    s.enemy_max_hp = deserialized["enemyMaxHp"]
    s.enemy_mana = deserialized["enemyMana"]
    s.enemy_max_mana = deserialized["enemyMaxMana"]

    s.tower_hp = deserialized["ourTowerHp"]
    s.enemy_tower_hp = deserialized["enemyTowerHp"]

    s.save()

    ###

    n1 = len(deserialized["ourCreeps"])
    for i, creep in zip(range(n1), deserialized["ourCreeps"]):
        c = CreepState()
        c.type = creep["type"]
        c.num = i + 1
        c.isEnemy = False

        c.hp = creep["hp"]
        c.max_hp = creep["maxHp"]
        c.x = creep["x"]
        c.y = creep["y"]
        c.state_host_id = s.id

        c.save()

    n2 = len(deserialized["enemyCreeps"])
    for i, creep in zip(range(n2), deserialized["enemyCreeps"]):
        c = CreepState()
        c.type = creep["type"]
        c.num = i + 1
        c.isEnemy = True

        c.hp = creep["hp"]
        c.max_hp = creep["maxHp"]
        c.x = creep["x"]
        c.y = creep["y"]
        c.state_host_id = s.id

        c.save()

    return HttpResponse("done")
