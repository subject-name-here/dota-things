import json
from django.http import HttpResponse, JsonResponse
from django.core import serializers

from saver.models import Action, State, CreepState


def get_action(request):
    print("entered")

    json_state = json.loads(request.body.decode("utf-8"), )["content"]["observation"]

    s = State()
    '''s.our_team = deserialized["ourTeam"]
    s.enemy_hero = deserialized["enemyName"]

    our_score = deserialized["ourScore"]
    enemy_score = deserialized["enemyScore"]
    s.score = our_score * 2 + enemy_score'''

    deserialized = json_state["self_info"]

    s.damage = deserialized[0]
    s.lvl = deserialized[1]
    s.gold = deserialized[2]

    s.hp = deserialized[3]
    s.max_hp = deserialized[4]
    s.mana = deserialized[5]
    s.max_mana = deserialized[6]

    s.facing = deserialized[7]

    s.available1 = deserialized[8]
    s.available2 = deserialized[9]
    s.available3 = deserialized[10]
    s.available4 = deserialized[11]

    s.x = deserialized[12]
    s.y = deserialized[13]

    deserialized = json_state["damage_info"]

    s.time_since_damage_by_hero = deserialized[0]
    s.time_since_damage_by_creep = deserialized[1]
    s.time_since_damage_by_tower = deserialized[2]

    deserialized = json_state["enemy_info"]

    s.enemy_visible = deserialized[0]

    s.enemy_lvl = deserialized[1]
    s.enemy_damage = deserialized[2]

    s.enemy_hp = deserialized[3]
    s.enemy_max_hp = deserialized[4]
    s.enemy_mana = deserialized[5]
    s.enemy_max_mana = deserialized[6]

    s.enemy_facing = deserialized[7]
    s.enemy_x = deserialized[8]
    s.enemy_y = deserialized[9]

    deserialized = json_state["tower_info"]

    s.tower_hp = deserialized[1]
    s.enemy_tower_hp = deserialized[0]

    ###
    '''
    n1 = len(deserialized["ourCreeps"])
    ocs = []
    for i, creep in zip(range(n1), deserialized["ourCreeps"]):
        c = CreepState()
        c.type = creep["type"]
        c.num = i + 1
        c.isEnemy = False

        c.hp = creep["hp"]
        c.max_hp = creep["maxHp"]
        c.x = creep["x"]
        c.y = creep["y"]
        c.state_host = s.id

        ocs.append(c)

    n2 = len(deserialized["enemyCreeps"])
    ecs = []
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

        ecs.append(c)'''

    list = State.objects.all()

    min = 1e10
    to_ret = 0
    for sl in list:
        d = dist(s, sl)
        if d < min:
            if sl.action_done.action_type == 0 and sl.action_done.dx != 0:
                to_ret = sl.action_done

    #to_ret = Action()
    #to_ret.action_type = 0
    #to_ret.dx = 

    print([to_ret.action_type, to_ret.param, to_ret.dx, to_ret.dy])
    return HttpResponse([to_ret.action_type, ',', to_ret.param, ',', to_ret.dx, ',', to_ret.dy])


def dist(s1, s2):
    d1 = (s1.x - s2.x) ** 2 + (s1.y - s2.y) ** 2
    d2 = 50 * (abs(s1.hp - s2.hp) + abs(s1.mana - s2.mana) + abs(s1.lvl - s2.lvl))

    d3 = 0
    '''if s1.enemy_visible ^ s2.enemy_visible:
        nonlocal d3
        d3 = 100000
    else:
        nonlocal d3
        d3 = (s1.enemy_x - s2.enemy_x) ** 2 + (s1.enemy_y - s2.enemy_y) ** 2'''

    return d1 + d2 + d3
